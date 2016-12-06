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
<!-- sample-tool-jsf list.jsp -->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<%
	response.setContentType("text/html; charset=UTF-8");
	response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
	response.addDateHeader("Last-Modified", System.currentTimeMillis());
	response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
	response.addHeader("Pragma", "no-cache");
%>

<f:view>
	<sakai:view_container title="#{msgs.list_title}">

		<h:form id="helloForm">
			<h:messages /><br />

			<h:outputText value="#{msgs.list_info}" /><br />

			<h:commandLink action="main">
				<h:outputText value="#{msgs.goto_main}"/>
			</h:commandLink>

				<h:inputText id="userName" value="#{JsfSampleBean.value}" required="true">
					<f:validateLength minimum="2" maximum="10"/>
				</h:inputText><br />

		</h:form>

	</sakai:view_container>
</f:view>
		