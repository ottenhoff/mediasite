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
package org.sakaiproject.mediasite.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.portlet.*;

import org.w3c.dom.*;
import org.imsglobal.basiclti.BasicLTIUtil;

import java.util.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.cover.UserDirectoryService;

import javax.xml.bind.DatatypeConverter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.oauth.*;
import net.oauth.signature.*;

import org.xml.sax.SAXException;


/**
 * <p>
 * Example of extending the standard JsfTool for a particular application; in this case, the sample JSF tool.
 * </p>
 */
public class MediasiteLTI 
{
	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
	    this.securityService = securityService;
	}	 	
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
	    this.siteService = siteService;
	}
	
	public String runMainJspPage(String LaunchEndpoint) {
		String message = "Mediasite content is loading...";
		String sakaiHome = System.getProperty("sakai.home");
		String catalinaBase = System.getenv("CATALINA_BASE");
		String catalinaHome = System.getenv("CATALINA_HOME");
		
		String appPath = "";
		if (sakaiHome != null && sakaiHome != "") {
			appPath = sakaiHome;
		}
		else if (catalinaBase != null && catalinaBase != "") {
			appPath = catalinaBase + File.separator + "sakai";
		}
		else {
			appPath = catalinaHome + File.separator + "sakai";
		}
		
		String XmlPath = File.separator + "portlets" + File.separator + "imsblti" + File.separator + "IMSBLTIPortlet.xml";
		String newpage = "";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc = null;
		try {
			newpage = ServerConfigurationService.getString("mediasite.NewPageOverride");
		}catch(Exception e)
		{
			newpage = "";
		}
	
		String secret = "error";
        String key = "error";	
        String launch = null;		
		try {
			builder = dbf.newDocumentBuilder();
			doc = builder.parse(appPath + XmlPath);
			XPathFactory factory = XPathFactory.newInstance();
		    XPath xpath = factory.newXPath();
			
			secret = getConfigValue(doc,xpath,"com.sonicfoundry.mediasite.lti","imsti.secret");
			key = getConfigValue(doc,xpath,"com.sonicfoundry.mediasite.lti","imsti.key");
			launch = getConfigValue(doc,xpath,"com.sonicfoundry.mediasite.lti","imsti.launch");
		}catch (Exception e) {
			message += "<BR />Sakai cannot process your IMSBLTIPortlet.xml configuration file correctly. <BR />Using sakai.home: " + sakaiHome + 
					   "<BR />Using CATALINA_BASE: " + catalinaBase + "<BR />Using CATALINA_HOME: " + catalinaHome +"<br />" + e;
		}
        
		launch = launch + LaunchEndpoint;
		String userId = null;
		String siteTitle = null;
		String siteId = "012345";

		userId = UserDirectoryService.getCurrentUser().getDisplayId();
		
		if (userId == null || userId == "") {
			userId = "fake_user";
		}

	    if ( siteTitle == null ) siteTitle = "Non-Course Page";
		String org_id = "mediasite";

		String role = "Learner";
		if (securityService != null && securityService.isSuperUser() )
		{
			role = "Instructor,Administrator,urn:lti:instrole:ims/lis/Administrator,urn:lti:sysrole:ims/lis/Administrator";
		}
		else if (siteService != null && siteService.allowUpdateSite(siteId) ) 
		{
			role = "Instructor";
		}


		// launch the LTI
		if ( launch == null ) {
			System.out.println("Node not configured as an LTI Launch - need ltiurl property");
			message += "<br/>Node not configured as an LTI Launch - need ltiurl property: " + key +  "  appPath  " + appPath;
		} else {

			Properties postProp = new Properties();
			postProp.setProperty("lti_version","LTI-1p0");
			postProp.setProperty("lti_message_type","basic-lti-launch-request");
			postProp.setProperty("ext_content_intended_use","embed");
			postProp.setProperty("ext_content_return_types","sakai");
			
			if (newpage != null) {
				postProp.setProperty("custom_mediasite_newpageoverride",newpage);	
			}
		
			
			postProp.setProperty("launch_presentation_return_url", "");
			
			postProp.setProperty("tool_consumer_instance_guid",org_id);
			postProp.setProperty("tool_consumer_instance_description","sakai");
			postProp.setProperty("tool_consumer_info_product_family_code","sakai");


			
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
		    List<Map.Entry<String, String>> params = null;
            // Get the signed parameters
		    try {
		    	params = oam.getParameters();
		    } catch(IOException e) {
		    	message += "<br/>ERROR: " + e;
		    }

  
            // Convert to Properties
            Properties nextProp = new Properties();
	        for (Map.Entry<String,String> e : params) {
		    	
			    nextProp.setProperty(e.getKey(), e.getValue());
		    }
   
		    String postData = BasicLTIUtil.postLaunchHTML(nextProp, launch, false);
		    //System.out.println(postData);
		   
		    if (message.equals("Mediasite content is loading...")){
		    	message = postData;
		    }
		}
    	
    	return message;
	}
    public static String getConfigValue(Document doc, XPath xpath, String id, String name) {
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
}
