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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.naming.AuthenticationException;
import javax.net.ssl.SSLHandshakeException;
import javax.portlet.*;

import org.w3c.dom.*;
import org.imsglobal.basiclti.BasicLTIUtil;

import java.util.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.user.cover.UserDirectoryService;

import javax.xml.bind.DatatypeConverter;
import javax.servlet.http.HttpServletRequest;

import net.oauth.*;
import net.oauth.signature.*;

import org.sakaiproject.user.api.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.sakaiproject.mediasite.tool.WebApiConstants;
import org.sakaiproject.mediasite.tool.CommonUtilities;


public class MediasiteContentLaunch 
{
	private HttpURLConnection connection;
	
	private Map<String, URI> workspace;
	
	public String ImpersonationUsername = "MediasiteAdmin";
	public String ImpersonationPassword = "New_Password";
	
	public int ticketDuration = 5000; //TODO: add this as a setting
	
	private static String base64Encode(String str) {
		return new String(Base64.encodeBase64(str.getBytes()));
	}
	public String decode(String s) {
		// Decode data on other side, by processing encoded data
		byte[] valueDecoded= Base64.decodeBase64(s.getBytes() );
		return new String(valueDecoded).replaceAll("[^\\x20-\\x7e]", "");
		
	}
	public String createLaunch(String ResourceId, String ContentType, String EncodedUrl) {
		String postData = "Mediasite content is loading...";
		String AuthTicket = null;
		String LaunchUrl = null;
		LaunchUrl = decode(EncodedUrl);
		
		String userId = "";
		userId = UserDirectoryService.getCurrentUser().getId();

		
		try {
			AuthTicket = createAuthTicket(ResourceId, userId, "", ticketDuration);
		} catch (Exception e){
			//TODO: 
			postData = "You do not have permission to view this content.";
			CommonUtilities.WriteLog("No valid auth ticket was created. " + e);
		}
    	
    	if (AuthTicket != null && LaunchUrl != null){
    		postData = "<form name='launcherForm' action='" + LaunchUrl + "' method='post'>";
    		postData += "<input type='hidden' name='AuthTicket' value='" + AuthTicket + "'>";
    		postData += "<noscript><input type='submit' value='Click to view this Mediasite " + ContentType + "/></noscript></form>";
    		postData += "<script>window.onload = new function () { document.forms[\"launcherForm\"].submit(); };</script>";
    	}
    	else {
    		postData = "ERROR: Could not launch Mediasite content. ";
    	}
    		
    	return postData;
	}

	public String createAuthTicket(String resourceID, String userName, String clientIP, int ticketDuration) throws Exception {
		// build a POST to AuthorizationTickets
		String jsonRequest = "";
		if (clientIP != "") {
			jsonRequest = String.format("{'Username':'%s','ResourceId':'%s','ClientIpAddress':'%s','MinutesToLive':'%s'}", userName, resourceID, clientIP, ticketDuration);
		} else {
			jsonRequest = String.format("{'Username':'%s','ResourceId':'%s','MinutesToLive':'%s'}", userName, resourceID, ticketDuration);
		}
		Document doc = postJson(WebApiConstants.AUTHORIZATION_TICKETS, jsonRequest);
		NodeList nodes = doc.getElementsByTagName(WebApiConstants.TICKET_ID.value());
		if (nodes.getLength() == 1){
			// inspect the first (and only) node for a value
			String AuthTicket = nodes.item(0).getTextContent();
			CommonUtilities.WriteLog(String.format("WebApiUtilities.createAuthTicket for %s, %s AuthTicket is %s.", resourceID, userName, AuthTicket));
			return AuthTicket;
		}
		CommonUtilities.WriteLog("WebApiUtilities.createAuthTicket failed to generate a valid auth ticket.", null, true);
		return null;
	}
	private Document postJson(WebApiConstants workSpace, String payload) throws Exception {
		try {
			this.connection = getConnection(getWorkspace(workSpace), false);
		}catch(Exception e) {
			CommonUtilities.WriteLog("Connection error: " + e);
		}
		this.connection.setRequestProperty("Content-Type", "application/json");
		
		// TODO: pull these from settings
		setAuthHeader(ImpersonationUsername, ImpersonationPassword, null, this.connection, true);
		CommonUtilities.WriteLog(String.format("WebApiUtilities.postJson to %s with payload %s", this.connection.getURL().toString(), payload));
		OutputStream output = null;
		try {
			output = connection.getOutputStream();
			output.write(payload.getBytes("UTF-8"));
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e){
					CommonUtilities.WriteLog(String.format("WebApiUtilities.postJson failed for %s", payload), e, true);
				}
			}
		}
		
		return Connect();
	}
	private HttpURLConnection getConnection(URI uri, boolean usePost) throws MalformedURLException, IOException{
		HttpURLConnection conn = (HttpURLConnection)uri.toURL().openConnection();
		conn.setRequestMethod(usePost ? "POST" : "GET");
		conn.setRequestProperty("Accept", "application/atom+xml");
		conn.setRequestProperty(WebApiConstants.SONICFOUNDRY_API_KEY_NAME.value(), WebApiConstants.SONICFOUNDRY_API_KEY_VALUE.value());
		conn.setDoOutput(true);
		return conn;
	}
	private URI getWorkspace(WebApiConstants key) throws Exception {
		buildWorkspace();
		if (key == null || this.workspace == null) {
			return null;
		}

		return this.workspace.get(key.value());
	}
	private void buildWorkspace() throws Exception{
		if (this.workspace == null) {
			ConnectAsSystem();
			try {
				Document workspace = Connect();
				this.workspace = buildWorkspace(this.connection.getURL().toURI(), workspace);
			} catch (org.xml.sax.SAXParseException e) {
				// probably due to WebApi talking to Mediasite 6.x
				//CommonUtilities.WriteLog(String.format("WebApiUtilities.buildWorkspace error for %s.", this.server.getServerName(), e));
			}
		}
	}
	private static Map<String, URI> buildWorkspace(URI baseUrl, Document workspace) throws DOMException, URISyntaxException{
		Map<String, URI> result = new HashMap<String, URI>();
		NodeList nodes = workspace.getElementsByTagName(WebApiConstants.COLLECTION.value());
		for (int i = 0; i < nodes.getLength(); i++){
			Node node = nodes.item(i);
			NodeList children = node.getChildNodes();
			Node child = null;
			for (int j = 0; j < children.getLength(); j++){
				Node n = children.item(j);
				if (n.getNodeName() == WebApiConstants.ATOM_TITLE.value()) {
					child = n;
				}
			}
			NamedNodeMap attributes = node.getAttributes();
			Node href = attributes.getNamedItem(WebApiConstants.HREF.value());
			URI uri = new URI(baseUrl.toString() + "/" + href.getNodeValue());
			result.put(child.getTextContent(), uri);
		}

		return result;
	}
	private void setAuthHeader(String username, String password, String impersonationUsername, HttpURLConnection conn, boolean connectAsSystem){
		String userinfo;
		if (impersonationUsername == null) {
			userinfo = String.format("%s:%s", username, password);
			conn.setRequestProperty(WebApiConstants.AUTHORIZATION_HEADER.value(), String.format("%s %s", WebApiConstants.BASIC_AUTH_SCHEME.value(), base64Encode(userinfo)));
			CommonUtilities.WriteLog("WebApiUtilities.setAuthHeader using Basic Authorization.");
		} else {
			userinfo = String.format("%s:%s:%s", username, password, impersonationUsername);
			CommonUtilities.WriteLog(String.format("Key: %s, Value: %s", WebApiConstants.AUTHORIZATION_HEADER.value(), String.format("%s %s", WebApiConstants.IMPERSONATION_AUTH_SCHEME.value(), base64Encode(userinfo))));
			conn.setRequestProperty(WebApiConstants.AUTHORIZATION_HEADER.value(), String.format("%s %s", WebApiConstants.IMPERSONATION_AUTH_SCHEME.value(), base64Encode(userinfo)));
			CommonUtilities.WriteLog("WebApiUtilities.setAuthHeader using Impersonation.");
		}		
	}
	private Document Connect() throws AuthenticationException, ParserConfigurationException, SAXException, IOException {
		java.util.Date startDate = new java.util.Date();
	
		String responseString = "";
		String outputString = "";
		int responseStatus = -1;
				
		try {
			responseStatus = this.connection.getResponseCode();
			CommonUtilities.WriteLog(String.format("WebApiUtilities.Connect: %s returned %d.", this.connection.getURL().toString(), responseStatus));
			if (responseStatus == HttpURLConnection.HTTP_OK) {
				InputStreamReader isr = new InputStreamReader(this.connection.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				while((responseString = in.readLine()) != null) {
					outputString += responseString;
				}
			} else if (responseStatus == HttpURLConnection.HTTP_UNAUTHORIZED) {
				// bad credentials or insufficient privs
				CommonUtilities.WriteLog(String.format("WebApiUtilities.Connect: \n\tURL: %s\n\tResponse Code: %s\n\tResponse Message: %s\n\tUser: %s", this.connection.getURL().toString(), responseStatus, this.connection.getResponseMessage(), ""), null, true);
				throw new AuthenticationException();
			} else {
				String errorMessage = readStream(this.connection.getErrorStream());
				CommonUtilities.WriteLog(String.format("WebApiUtilities.Connect: \n\tURL: %s\n\tResponse Code: %s\n\tResponse Message: %s\n\t%s", this.connection.getURL().toString(), responseStatus, this.connection.getResponseMessage(), errorMessage), null, true);
				throw new AuthenticationException(); //WebApiHTTPException(responseStatus, null, errorMessage);
			}
		} catch (SSLHandshakeException e) {
			CommonUtilities.WriteLog(String.format("WebApiUtilities.Connect: An SSL/Certificate error has occurred. Response Code: %s", responseStatus), e, true);
		} catch (IOException e) {
			CommonUtilities.WriteLog(String.format("WebApiUtilities.Connect: A general error has occurred. Response Code: %s", responseStatus), e, true);
		}
		CommonUtilities.WriteLog("WebApiUtilities.Connect executed in " + (System.currentTimeMillis() - startDate.getTime()) + " milliseconds.");
		return CommonUtilities.isNullOrEmpty(outputString) ? null : parseXmlFile(outputString);
	}
	
	public boolean ConnectAsSystem() {
		try {
			this.connection = getConnection(getRootPath(), false);

			setAuthHeader(ImpersonationUsername, ImpersonationPassword, ImpersonationUsername, this.connection, true);

		} catch (MalformedURLException e) {
			//CommonUtilities.WriteLog(String.format("WebApiUtilities.ConnectAsSystem encountered an error.", this.server.toString()), e);
			return false;
		} catch (IOException e) {
			//CommonUtilities.WriteLog(String.format("WebApiUtilities.ConnectAsSystem encountered an error.", this.server.toString()), e);
			return false;
		} catch (Exception e) {
			//CommonUtilities.WriteLog(String.format("WebApiUtilities.ConnectAsSystem encountered an error.", this.server.toString()), e);
			return false;
		}
		return true;
	}
	
	public static Document parseXmlFile(String in) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(in));
        return db.parse(is);
	}
	
	private URI getRootPath() throws Exception{
		URI baseUri = new URI("http://dev70.dev.mediasite.com/Mediasite/streams");
		URI uri = getRootPath(baseUri);
		CommonUtilities.WriteLog(String.format("WebApiUtilities.getRootPath for server: %s is %s", "http://dev70.dev.mediasite.com/Mediasite/streams", uri.toString()));
		return uri;
	}
	
	public static URI getRootPath(URI baseUri) throws URISyntaxException {
		URI uri = new URI(
				baseUri.getScheme(),
				null,
				baseUri.getHost(),
				baseUri.getPort(),
				baseUri.getPath() + WebApiConstants.SONICFOUNDRY_WEB_API_PATH.value(),
				baseUri.getQuery(),
				baseUri.getFragment()
				);
		return uri;
	}
	private static String readStream(InputStream stream) {

		BufferedReader br = null;
		String result = null;
		if (stream == null){
			return result;
		}else{
			br = new BufferedReader(new InputStreamReader(stream));
			String line;
			result = "";
			try {
				while((line = br.readLine()) != null){
					result += line;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}
	}
	
}
