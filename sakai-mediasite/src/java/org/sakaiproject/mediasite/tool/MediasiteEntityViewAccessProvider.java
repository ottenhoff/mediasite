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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Does a redirect to allow basic DirectServlet access to mediasite
 *
 */

public class MediasiteEntityViewAccessProvider extends HttpServlet
  implements EntityViewAccessProvider {

	private static Log M_log = LogFactory.getLog(MediasiteEntityViewAccessProvider.class);

  private static final long serialVersionUID = 0L;
  private EntityBroker entityBroker;
  private SessionManager sessionManager;
  private EntityViewAccessProviderManager accessProviderManager;
  
  /**
   * Initialize the servlet.
   * 
   * @param config
   *        The servlet config.
   * @throws ServletException
   */
  public void init(ServletConfig config) throws ServletException {
    M_log.info("init()");
	  super.init(config);
    entityBroker = (EntityBroker) ComponentManager
        .get("org.sakaiproject.entitybroker.EntityBroker");
    sessionManager = (SessionManager) ComponentManager
    	    .get("org.sakaiproject.tool.api.SessionManager");
    accessProviderManager = (EntityViewAccessProviderManager) ComponentManager
            .get("org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager");
    if (accessProviderManager != null)
    	accessProviderManager.registerProvider("Mediasite", this);
  }

  public void handleAccess(EntityView view, HttpServletRequest req, HttpServletResponse res) {
	  M_log.debug("handleAccess()");
	  Map<String, String> props = entityBroker.getProperties(req.getPathInfo());
	  String target = props.get("url");
	  M_log.debug("handleAccess() -> " + target);
	  String user = props.get("security.user");
	  String site_function = props.get("security.site.function");
	  String site_secref = props.get("security.site.ref");	  
	  System.out.println("target: " + target);
	  System.out.println("format: " + view.getFormat());
	  try {
		  setNoCacheHeaders(res);
		  res.sendRedirect(target);		  
	  }
	  catch (IOException e) {
		  e.printStackTrace();
	  }
	  return;
  }
  
	// set standard no-cache headers
	protected void setNoCacheHeaders(HttpServletResponse resp)
	{
		resp.setContentType("text/html; charset=UTF-8");
		// some old date
		resp.addHeader("Expires", "Mon, 01 Jan 2099 00:00:00 GMT");
		// TODO: do we need this? adding a date header is expensive contention for the date formatter, ours or Tomcats.
		// resp.addDateHeader("Last-Modified", System.currentTimeMillis());
		resp.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		resp.addHeader("Pragma", "no-cache");
	}
	
  private void clearSessionAttributes(Session session) {

	  
  }

}