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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonUtilities {
	public final static String ENTITLEMENT = "course.content.CREATE";
	
	public static String formatExceptionForDisplay(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		// LogServiceFactory.getInstance().getDefaultLog().logError("MediasiteBuildingBlockException: ", e);
		return "<span style=\"color: red;\">An error occurred while searching for Mediasite content. Please contact your Blackboard administrator for more information.</span><!--<pre>" + sw.toString() + "</pre>-->";
	}

	
	public static void WriteLog(String message){
		WriteLog(message, null);
	}
	public static void WriteLog(String message, Exception e){
		boolean debugLogging = false;
		if (e != null) {
			debugLogging = true;
		} else {
			try {
				debugLogging = true; //TODO: XMLUtilities.getSettings().getEnableDebugLogging();
			} catch (Exception ex) {
				debugLogging = true;
			}
		}
		WriteLog(message, e, debugLogging);
	}	
	public static void WriteLog(String message, Exception e, boolean debugLogging){
		message = "\nMediasite:\n" + message + "\n--";
		if (IsDebug()){
			System.out.println(message);
			if (e != null){
				e.printStackTrace();
			}
		}
		else if (debugLogging || e != null) {
			//LogServiceFactory.getInstance().getDefaultLog().logError(message, e);
		} else {
			//LogServiceFactory.getInstance().getDefaultLog().logInfo(message, e);		
		}
	}

	public static boolean isNullOrEmpty(String s) {
		return null == s || s.trim().length() == 0;
	}


	private static boolean find(Map<String, String[]> list, String match){
		for(String key : list.keySet()){
			if (key.equalsIgnoreCase(match)){
				return true;
			}
		}
		return false;
	}

	private static String Stringify(String[] strings, String separator){
		String result = "";
		for(String string : strings){
			if (result != ""){
				result += separator;
			}
			result += string;
		}
		return result;
	}

	
	public static boolean IsDebug() {
		try {
			return java.lang.management.ManagementFactory.getRuntimeMXBean().
			    getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
		} catch (java.security.AccessControlException e){
			return false;
		}
	}
}
