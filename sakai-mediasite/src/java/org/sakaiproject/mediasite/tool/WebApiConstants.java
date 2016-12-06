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

public enum WebApiConstants {
	ACTION ("m:action"),
	ACTIVATED ("d:Activated"),
	APPLICATION_TICKET_HEADER ("Mediasite-Application-Ticket"),
	APPLICATION_TICKETS ("ApplicationTickets"),
	ASSOCIATIONS ("d:Associations"),
	ATOM_TITLE ("atom:title"),
	AUTHORIZATION_HEADER ("Authorization"),
	AUTHORIZATION_TICKETS ("AuthorizationTickets"),
	BASIC_AUTH_SCHEME ("Basic"),
	CATALOG_SEARCH_ORDER_BY ("Name"),
	CATALOG_URL ("d:CatalogUrl"),
	CATALOGS ("Catalogs"),
	COLLECTION ("collection"),
	CONTENT ("content"),
	COUNT ("m:count"),
	DESCRIPTION ("d:Description"),
	ELEMENT ("d:element"),
	ENTRY ("entry"),
	FOLDERS ("Folders"),
	PROPERTIES ("m:properties"),
	HOME ("Home"),
	HREF ("href"),
	ID ("d:Id"),
	IMPERSONATION_AUTH_SCHEME ("sfidentticket"),
	LAYOUT_OPTIONS ("LayoutOptions"),
	LINK ("link"),
	MODULES ("Modules"),
	NAME ("d:Name"),
	PLAY ("Play"),
	TITLE_ATTRIBUTE ("title"),
	PRESENTATIONS ("Presentations"),
	PRESENTATION_SEARCH_ORDER_BY ("Title"),
	PRESENTATION_SEARCH_SELECT ("full"), 
	PRESENTATION_RECORD_DATE ("d:RecordDate"),
	PRESENTERS ("Presenters"),
	PRIVATE ("d:Private"),
	RECYCLED ("d:Recycled"),
	ROOT_ID ("d:RootId"),
	SHOW_DATE_TIME("d:ShowDateTime"),
	SITE_VERSION ("d:SiteVersion"),
	SONICFOUNDRY_API_KEY_NAME ("sfapikey"),
	SONICFOUNDRY_API_KEY_VALUE ("ca6dc2fb-94c8-4666-b1fd-e524b1b20cb5"),
	SONICFOUNDRY_WEB_API_PATH ("/api/v1"),
	STATUS ("d:Status"),
	TARGET ("target"),
	TEMPLATES ("Templates"),
	TEMPLATES_ORDER_BY ("Name"),
	TICKET_ID ("d:TicketId"),
	TIMEZONE_ID ("d:TimeZoneId"),
	TITLE ("d:Title"),
	THUMBNAIL_CONTENT ("ThumbnailContent"),
	THUMBNAIL_URL ("d:ThumbnailUrl"),
	PRESENTER_PREFIX ("d:Prefix"),
	PRESENTER_FIRSTNAME ("d:FirstName"),
	PRESENTER_MIDDLENAME ("d:MiddleName"),
	PRESENTER_LASTNAME ("d:LastName"),
	PRESENTER_SUFFIX ("d:Suffix"),
	PRESENTER_DISPLAYNAME ("d:DisplayName"),
	USER_PROFILES ("UserProfiles"),
	VALUE ("d:Value")
	;
	
	private String value;
	
	private WebApiConstants() {
		throw new AssertionError();
	}
	
	private WebApiConstants(String value){
		this.value = value;
	}
	public String value() {
		return value;
	}
}
