/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.extension.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.extension.factory.IPredefinedPropertySet;
import org.eclipse.team.svn.ui.extension.factory.PredefinedProperty;

/**
 * IPropertyProvider implementation
 *
 * @author Sergiy Logvin
 */
public class PredefinedPropertySet implements IPredefinedPropertySet {
	
	public List<PredefinedProperty> getPredefinedProperties() {
		
		List<PredefinedProperty> properties = new ArrayList<PredefinedProperty>();
		
		properties.add(new PredefinedProperty(SVNUIMessages.AbstractPropertyEditPanel_svn_description, "", "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("svn:eol-style", SVNUIMessages.Property_SVN_EOL, ""));		 //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("svn:executable", SVNUIMessages.Property_SVN_Executable, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("svn:externals", SVNUIMessages.Property_SVN_Externals, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("svn:ignore", SVNUIMessages.Property_SVN_Ignore, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("svn:keywords", SVNUIMessages.Property_SVN_Keywords, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("svn:mime-type", SVNUIMessages.Property_SVN_Mimetype, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("svn:mergeinfo", SVNUIMessages.Property_SVN_Mergeinfo, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("svn:needs-lock", SVNUIMessages.Property_SVN_NeedsLock, "")); //$NON-NLS-1$ //$NON-NLS-2$
		this.getBugtrackProperties(properties);
		properties.add(new PredefinedProperty(SVNUIMessages.PropertyEditPanel_tsvn_description, "", "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("tsvn:logtemplate", SVNUIMessages.Property_TSVN_LogTemplate, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("tsvn:logwidthmarker", SVNUIMessages.Property_TSVN_LogWidthMarker, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("tsvn:logminsize", SVNUIMessages.Property_TSVN_LogMinSize, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("tsvn:lockmsgminsize", SVNUIMessages.Property_TSVN_LockMsgMinSize, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("tsvn:logfilelistenglish", SVNUIMessages.Property_TSVN_LogFileListEnglish, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("tsvn:projectlanguage", SVNUIMessages.Property_TSVN_ProjectLanguage, "")); //$NON-NLS-1$ //$NON-NLS-2$
						
		return properties;
	}
	
	public Map<String, String> getPredefinedPropertiesRegexps() {
		
		HashMap<String, String> regexpmap = new HashMap<String, String>();
		
		regexpmap.put("svn:eol-style", "((native)|(LF)|(CR)|(CRLF))"); //$NON-NLS-1$ //$NON-NLS-2$
		regexpmap.put("svn:executable", null); //$NON-NLS-1$
		regexpmap.put("svn:externals", ""); //$NON-NLS-1$ //$NON-NLS-2$
		regexpmap.put("svn:ignore", "([^\\\\/\\:])+"); //$NON-NLS-1$ //$NON-NLS-2$
		regexpmap.put("svn:keywords", "((Date)|(Revision)|(Author)|(HeadURL)|(Id)|(LastChangedDate)|(Rev)|(LastChangedRevision)|(LastChangedBy)|(URL)|(\\s))+"); //$NON-NLS-1$ //$NON-NLS-2$
		regexpmap.put("svn:mime-type", null); //$NON-NLS-1$
		regexpmap.put("svn:mergeinfo", null); //$NON-NLS-1$
		regexpmap.put("svn:needs-lock", null); //$NON-NLS-1$
		this.getBugtrackRegExps(regexpmap);
		regexpmap.put("tsvn:logtemplate", null); //$NON-NLS-1$
		regexpmap.put("tsvn:logwidthmarker", "(\\d+)"); //$NON-NLS-1$ //$NON-NLS-2$
		regexpmap.put("tsvn:logminsize", "(\\d+)"); //$NON-NLS-1$ //$NON-NLS-2$
		regexpmap.put("tsvn:lockmsgminsize", "(\\d+)"); //$NON-NLS-1$ //$NON-NLS-2$
		regexpmap.put("tsvn:logfilelistenglish", "((true)|(false))"); //$NON-NLS-1$ //$NON-NLS-2$
		regexpmap.put("tsvn:projectlanguage", null); //$NON-NLS-1$
		
		return regexpmap;
	}

	/**
	 * Allow to define custom bugtraq properties, clients should override this method.
	 * @param properties
	 */
	protected void getBugtrackProperties(List<PredefinedProperty> properties) {
		properties.add(new PredefinedProperty(SVNUIMessages.PropertyEditPanel_bugtraq_description, "", "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("bugtraq:url", SVNUIMessages.Property_Bugtraq_URL, "%BUGID%")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("bugtraq:logregex", SVNUIMessages.Property_Bugtraq_LogRegex, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("bugtraq:label", SVNUIMessages.Property_Bugtraq_Label, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("bugtraq:message", SVNUIMessages.Property_Bugtraq_Message, "%BUGID%")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("bugtraq:number", SVNUIMessages.Property_Bugtraq_Number, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("bugtraq:warnifnoissue", SVNUIMessages.Property_Bugtraq_WarnIfNoIssue, "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("bugtraq:append", SVNUIMessages.Property_Bugtraq_Append, "")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Allow to define custom bugtraq properties, clients should override this method.
	 * @param name2regexp map
	 */
	protected void getBugtrackRegExps(HashMap<String, String> regexpmap) {
		regexpmap.put("bugtraq:url", "((http:\\/\\/)|(https:\\/\\/))(\\S+)?((\\%BUGID\\%))(\\S+)?"); //$NON-NLS-1$ //$NON-NLS-2$
		regexpmap.put("bugtraq:logregex", ""); //$NON-NLS-1$ //$NON-NLS-2$
		regexpmap.put("bugtraq:label", null); //$NON-NLS-1$
		regexpmap.put("bugtraq:message", ""); //$NON-NLS-1$ //$NON-NLS-2$
		regexpmap.put("bugtraq:number", "((true)|(false))"); //$NON-NLS-1$ //$NON-NLS-2$
		regexpmap.put("bugtraq:warnifnoissue", "((true)|(false))"); //$NON-NLS-1$ //$NON-NLS-2$
		regexpmap.put("bugtraq:append", "((true)|(false))"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
}
