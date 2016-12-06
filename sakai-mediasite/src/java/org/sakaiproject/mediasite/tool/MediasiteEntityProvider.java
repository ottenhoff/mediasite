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

import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;

import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.component.cover.ComponentManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestInterceptor;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Redirectable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

import org.sakaiproject.mediasite.tool.MediasiteLTI;
import org.sakaiproject.mediasite.tool.MemoryDao;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MediasiteEntityProvider extends AbstractEntityProvider implements RESTful, Describeable, RequestStorable, RequestInterceptor, Outputable, Redirectable{
    private MemoryDao dao;
    public void setDao(MemoryDao dao) {
        this.dao = dao;
    }

    public static String PREFIX = "Mediasite";
    public String getEntityPrefix() {
        return PREFIX;
    }
    // INTERCEPTORS

    public void before(EntityView view, HttpServletRequest req, HttpServletResponse res) {
    	// This space intentionally left blank
    }
    public void after(EntityView view, HttpServletRequest req, HttpServletResponse res) {

    }
    
    // ACTIONS 
    
    @EntityCustomAction(action="version",viewKey=EntityView.VIEW_LIST)
    public String getVersion() {
        return "Version 0.0";
    }
    
    @EntityCustomAction(action="postLTI",viewKey = "")
    public String getMainJsp(EntityView view, Map<String, Object> params) {
    	MediasiteLTI mediasiteLTI = new MediasiteLTI();
    	String postData = mediasiteLTI.runMainJspPage("");
    	
    	System.out.println(postData);
    	return postData;
    }
    
    @EntityCustomAction(action="launchContent",viewKey = "")
    public String launchContent(EntityView view, Map<String, Object> params) {
    	MediasiteLTI mediasiteLTI = new MediasiteLTI();
    	String resourceId = (String)params.get("mediasiteId");
    	String postData = mediasiteLTI.runMainJspPage("Home/Launch?mediasiteId=" + resourceId);
    	
    	System.out.println(postData);
    	return postData;
    }
    
    @EntityCustomAction(action="configure", viewKey="")
    public Object getMediasiteSessionInfo(EntityView view, Map<String, Object> params) {
    	// GET /Mediasite/config
        Map<String, Object> info = new HashMap<String, Object>();
      	try {
    		String XmlPath = "../../tomcat/sakai/portlets/imsblti/IMSBLTIPortlet.xml";
    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    		DocumentBuilder builder;
    		Document doc = null; 
    		
			builder = dbf.newDocumentBuilder();
			doc = builder.parse(XmlPath);
			XPathFactory factory = XPathFactory.newInstance();
		    XPath xpath = factory.newXPath();
		    
			info.put("secret", getConfigValue(doc,xpath,"com.sonicfoundry.mediasite.lti","imsti.secret"));
			info.put("key", getConfigValue(doc,xpath,"com.sonicfoundry.mediasite.lti","imsti.key"));
			info.put("launch", getConfigValue(doc,xpath,"com.sonicfoundry.mediasite.lti","imsti.launch"));
		}catch (ParserConfigurationException e) {
			info.put("secret", "");
			info.put("key", "");
			info.put("launch", "");
        }catch (SAXException e) {
			info.put("secret", "");
			info.put("key", "");
			info.put("launch", "");
		}catch (IOException e) {
			info.put("secret", "");
			info.put("key", "");
			info.put("launch", "");
		}
        info.put("appVersion", "Sample version number");
        return info;
    }
    
    // STANDARD METHODS
    RequestStorage reqStore;
    public void setRequestStorage(RequestStorage requestStorage) {
        this.reqStore = requestStorage;
    }
    public String[] getHandledOutputFormats() {
        return new String[] {Formats.XML, Formats.JSON, Formats.HTML, "form", "jsp"};
    }

    
    public String[] getHandledInputFormats() {
        return new String[] {Formats.HTML, Formats.XML, Formats.JSON, "form", "jsp"};
    }
    



    public List<?> getEntities(EntityReference ref, Search search) {
        return dao.findBySearch(search);
    }

    public String createEntity(EntityReference ref, Object entity) {
        MediasiteEntity we = (MediasiteEntity) entity;
        we.setOwner(getCurrentUser());
        dao.save(we);
        return we.getId();
    }

    public Object getSampleEntity() {
        return new MediasiteEntity();
    }

    public void updateEntity(EntityReference ref, Object entity) {
        MediasiteEntity we = (MediasiteEntity) entity;
        MediasiteEntity current = dao.findById(ref.getId());
        if (current == null) {
            throw new IllegalArgumentException("Could not locate entity to update");
        }
        checkAllowed(current);
        developerHelperService.copyBean(we, current, 0, new String[] {"id", "owner"}, true);
        dao.save(current);
    }

    public void deleteEntity(EntityReference ref) {
        MediasiteEntity current = dao.findById(ref.getId());
        if (current == null) {
            throw new IllegalArgumentException("Could not locate entity to remove");
        }
        checkAllowed(current);
        dao.remove(current.getId());
    }

    // normally this should be handled in a service layer
    private void checkAllowed(MediasiteEntity current) {
        if (! current.getOwner().equals(getCurrentUser())) {
            throw new SecurityException("Only the owner can remove this entity: " + current.getOwner());
        }
    }

    // normally handled in a service layer but ok to do here
    private String getCurrentUser() {
        String userRef = developerHelperService.getCurrentUserReference();
        if (userRef == null) {
            throw new SecurityException("Must be logged in to create/update/delete entities");
        }
        return userRef;
    }
    
    public Object getEntity(EntityReference ref) {
        if (ref.getId() == null) {
            return new MediasiteEntity();
        }
        MediasiteEntity entity = dao.findById(ref.getId());
        if (entity != null) {
            return entity;
        }
        throw new IllegalArgumentException("Invalid id:" + ref.getId());
    }

    // methods below are for compatibility from 1.3.3 to newer versions of EB

    public String createEntity(EntityReference ref, Object entity,
            Map<String, Object> params) {
        return createEntity(ref, entity);
    }

    public void updateEntity(EntityReference ref, Object entity,
            Map<String, Object> params) {
        updateEntity(ref, entity);
    }

    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        deleteEntity(ref);
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
