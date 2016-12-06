<%
/*
 * Copyright 2016 Sonic Foundry, Inc. Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://opensource.org/licenses/ECL-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
%>
<%@ page import="java.io.*,
				javax.xml.parsers.*,
				javax.xml.xpath.*,
				javax.portlet.*,
				org.w3c.dom.*,
				org.imsglobal.basiclti.*,
				java.util.*,
				org.apache.commons.codec.binary.Base64,
				org.apache.commons.codec.binary.StringUtils,
				org.sakaiproject.tool.api.SessionManager,
				org.sakaiproject.component.cover.ComponentManager,
				org.sakaiproject.user.cover.UserDirectoryService,
				javax.xml.bind.DatatypeConverter,
				javax.servlet.http.HttpServletRequest,
				org.sakaiproject.authz.cover.SecurityService,
				org.sakaiproject.site.cover.SiteService,
				net.oauth.*,
				net.oauth.signature.*,
				org.sakaiproject.user.api.*" 
				%>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<body>

 <%
 	String XmlPath = "../../sakai/portlets/imsblti/IMSBLTIPortlet.xml";
	String appPath = application.getRealPath("/");
	DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
	DocumentBuilder db=dbf.newDocumentBuilder();
	Document doc=db.parse(appPath + XmlPath);
	 		
	final SessionManager sessionManager;		
	sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);

	XPathFactory factory = XPathFactory.newInstance();
	XPath xpath = factory.newXPath();
	
	String launch = "";
	String toolId = "";
	Boolean isPlugin = true;
	 

	launch = getConfigValue(doc,xpath,"com.sonicfoundry.mediasite.lti","imsti.launch");
	if (launch != "") {
		toolId = "com.sonicfoundry.mediasite.lti";
	}
		
	// If LTI launch is NOT from the plugin, a launchURL should be present in the request.
	if (request.getParameter("launchURL") != "" && request.getParameter("launchURL") != null) {
		String encodedLaunch = request.getParameter("launchURL");
		launch = decode(encodedLaunch);
		isPlugin = false;
	}		



	if (toolId == "") {
		%> 
		<p> Mediasite is not configured on this server for this purpose. </p>
		<%
    }
	else {
		%> 	 
		<p> Mediasite content is loading... </p>
		
		<%
		//Gather LTI properties
		String secret = getConfigValue(doc,xpath,toolId,"imsti.secret");
		String key = getConfigValue(doc,xpath,toolId,"imsti.key");
		
		String userId = sessionManager.getCurrentSessionUserId();	
		if (userId == null || userId == "") {
			userId = request.getParameter("eid");
			
		}
		
		if (userId == null || userId == "") {
			userId = "fake_user";
		}


		
		String siteId = "012345";
		String siteTitle = null;
		if (request != null) {
			siteId = request.getParameter("siteId");
			siteTitle = request.getParameter("siteTitle");	
		}

	    if ( siteTitle == null ) siteTitle = "Non-Course Page";
		if (siteId == null) siteId = "012345";
		String org_id = "mediasite";
		
		String role = "Learner";
		if ( SecurityService.isSuperUser() )
		{
			role = "Instructor,Administrator,urn:lti:instrole:ims/lis/Administrator,urn:lti:sysrole:ims/lis/Administrator";
		}
		else if ( SiteService.allowUpdateSite(siteId) ) 
		{
			role = "Instructor";
		}

		// launch the LTI
		if ( launch == null ) {
			out.println("Node not configured as an LTI Launch - need ltiurl property");
		} else {
			System.out.println("launchurl="+launch+" secret="+secret+" user="+userId+" role="+role);
			System.out.println("siteId="+siteId+" siteTitle="+siteTitle);

			Properties postProp = new Properties();
			postProp.setProperty("lti_version","LTI-1p0");
			postProp.setProperty("lti_message_type","basic-lti-launch-request");
			postProp.setProperty("ext_content_intended_use","embed");
			postProp.setProperty("ext_content_return_types","sakai");
			if (isPlugin) {
				postProp.setProperty("launch_presentation_return_url", request.getRequestURL().toString());	
			}
			else {
				postProp.setProperty("launch_presentation_return_url", "");
			}
			postProp.setProperty("tool_consumer_instance_guid",org_id);
			postProp.setProperty("tool_consumer_instance_description","sakai");
			postProp.setProperty("tool_consumer_info_product_family_code","sakai");

			User user = UserDirectoryService.getCurrentUser();

			
			postProp.setProperty("user_id",userId);
			

			postProp.setProperty("resource_link_id","Test_String");
			postProp.setProperty("roles",role);
			postProp.setProperty("context_id",siteId);
			postProp.setProperty("context_title",siteTitle);
			postProp.setProperty("context_label",siteTitle);

			// Must even include the Submit button in the signed material
			postProp.setProperty("ext_basiclti_submit","Continue");

			// Just to make sure we give BasicLTI a chance to remove
			// properties it will refuse to send
			postProp = BasicLTIUtil.cleanupProperties(postProp);

			OAuthMessage oam = new OAuthMessage("POST", launch, postProp.entrySet());
			OAuthConsumer cons = new OAuthConsumer(null, key, secret, null);
			OAuthAccessor acc = new OAuthAccessor(cons);

		   System.out.println("OAM="+oam+"\n");
			try {
				System.out.println("BM="+OAuthSignatureMethod.getBaseString(oam)+"\n");
			} catch (Exception e) {
				System.out.println("Yikes");
			}
			try {
				oam.addRequiredParameters(acc);
				System.out.println("BM2="+OAuthSignatureMethod.getBaseString(oam)+"\n");
			} catch (Exception e) {
				System.out.println("Yikes");
			}
		    System.out.println("OAM="+oam+"\n");
    

            // Get the signed parameters
            List<Map.Entry<String, String>> params = oam.getParameters();
	        System.out.println("params="+params);
  
            // Convert to Properties
            Properties nextProp = new Properties();
	        for (Map.Entry<String,String> e : params) {
		    	System.out.println("value= " + e);
			    nextProp.setProperty(e.getKey(), e.getValue());
		    }
   
		    String postData = BasicLTIUtil.postLaunchHTML(nextProp, launch, false);
		    out.println(postData);
	    }

	}

     %>

 </body>
</html>

<%!
    private static String getConfigValue(Document doc, XPath xpath, String id, String name) {
        String value = null;
        try {
            XPathExpression expr =
                xpath.compile("/registration/tool[@id='" + id + "']/configuration[@name='" + name + "']/@value");
            value = (String) expr.evaluate(doc, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
 
        return value;
    }
	public String decode(String s) {
		// Decode data on other side, by processing encoded data
		byte[] valueDecoded= Base64.decodeBase64(s.getBytes() );
		return new String(valueDecoded).replaceAll("[^\\x20-\\x7e]", "");
		
	}
%>