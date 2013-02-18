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
import java.util.LinkedHashMap;
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
	
	protected static Map<String, PredefinedProperty> properties = new LinkedHashMap<String, PredefinedProperty>();
	
	static
	{
		PredefinedPropertySet.registerProperty(new PredefinedProperty(SVNUIMessages.AbstractPropertyEditPanel_svn_description));
		PredefinedPropertySet.registerProperty(new PredefinedProperty("svn:eol-style", SVNUIMessages.Property_SVN_EOL, "", "((native)|(LF)|(CR)|(CRLF))"));	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("svn:executable", SVNUIMessages.Property_SVN_Executable, "")); //$NON-NLS-1$ //$NON-NLS-2$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("svn:externals", SVNUIMessages.Property_SVN_Externals, "")); //$NON-NLS-1$ //$NON-NLS-2$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("svn:ignore", SVNUIMessages.Property_SVN_Ignore, "", "([^\\\\/\\:])+")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("svn:keywords", SVNUIMessages.Property_SVN_Keywords, "", "((Date)|(Revision)|(Author)|(HeadURL)|(Id)|(LastChangedDate)|(Rev)|(LastChangedRevision)|(LastChangedBy)|(URL)|(\\s))+")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("svn:mime-type", SVNUIMessages.Property_SVN_Mimetype, "")); //$NON-NLS-1$ //$NON-NLS-2$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("svn:mergeinfo", SVNUIMessages.Property_SVN_Mergeinfo, "")); //$NON-NLS-1$ //$NON-NLS-2$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("svn:needs-lock", SVNUIMessages.Property_SVN_NeedsLock, "")); //$NON-NLS-1$ //$NON-NLS-2$
		
		PredefinedPropertySet.registerProperty(new PredefinedProperty(SVNUIMessages.PropertyEditPanel_bugtraq_description));
		PredefinedPropertySet.registerProperty(new PredefinedProperty("bugtraq:url", SVNUIMessages.Property_Bugtraq_URL, "%BUGID%", "((http:\\/\\/)|(https:\\/\\/))(\\S+)?((\\%BUGID\\%))(\\S+)?")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("bugtraq:logregex", SVNUIMessages.Property_Bugtraq_LogRegex, "")); //$NON-NLS-1$ //$NON-NLS-2$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("bugtraq:label", SVNUIMessages.Property_Bugtraq_Label, "")); //$NON-NLS-1$ //$NON-NLS-2$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("bugtraq:message", SVNUIMessages.Property_Bugtraq_Message, "%BUGID%")); //$NON-NLS-1$ //$NON-NLS-2$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("bugtraq:number", SVNUIMessages.Property_Bugtraq_Number, "", "((true)|(false))")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("bugtraq:warnifnoissue", SVNUIMessages.Property_Bugtraq_WarnIfNoIssue, "", "((true)|(false))")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("bugtraq:append", SVNUIMessages.Property_Bugtraq_Append, "", "((true)|(false))")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		PredefinedPropertySet.registerProperty(new PredefinedProperty(SVNUIMessages.PropertyEditPanel_tsvn_description));
		PredefinedPropertySet.registerProperty(new PredefinedProperty("tsvn:logtemplate", SVNUIMessages.Property_TSVN_LogTemplate, "")); //$NON-NLS-1$ //$NON-NLS-2$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("tsvn:logwidthmarker", SVNUIMessages.Property_TSVN_LogWidthMarker, "", "(\\d+)")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("tsvn:logminsize", SVNUIMessages.Property_TSVN_LogMinSize, "", "(\\d+)")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("tsvn:lockmsgminsize", SVNUIMessages.Property_TSVN_LockMsgMinSize, "", "(\\d+)")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("tsvn:logfilelistenglish", SVNUIMessages.Property_TSVN_LogFileListEnglish, "", "((true)|(false))")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("tsvn:projectlanguage", SVNUIMessages.Property_TSVN_ProjectLanguage, "")); //$NON-NLS-1$ //$NON-NLS-2$
		
		PredefinedPropertySet.registerProperty(new PredefinedProperty(SVNUIMessages.AbstractPropertyEditPanel_revprop_description, "", "", null, PredefinedProperty.TYPE_GROUP | PredefinedProperty.TYPE_REVISION)); //$NON-NLS-1$ //$NON-NLS-2$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("svn:log", SVNUIMessages.Property_SVN_Log, "", null, PredefinedProperty.TYPE_REVISION)); //$NON-NLS-1$ //$NON-NLS-2$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("svn:author", SVNUIMessages.Property_SVN_Author, "", null, PredefinedProperty.TYPE_REVISION)); //$NON-NLS-1$ //$NON-NLS-2$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("svn:date", SVNUIMessages.Property_SVN_Date, "", null, PredefinedProperty.TYPE_REVISION)); //$NON-NLS-1$ //$NON-NLS-2$
		PredefinedPropertySet.registerProperty(new PredefinedProperty("svn:autoversioned", SVNUIMessages.Property_SVN_Autoversioned, "", null, PredefinedProperty.TYPE_REVISION)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	protected static void registerProperty(PredefinedProperty property)
	{
		PredefinedPropertySet.properties.put(property.name, property);
	}
	
	public List<PredefinedProperty> getPredefinedProperties() {
		return new ArrayList<PredefinedProperty>(PredefinedPropertySet.properties.values());
	}
	
	public Map<String, String> getPredefinedPropertiesRegexps() {
		HashMap<String, String> regexpmap = new HashMap<String, String>();
		for (PredefinedProperty property : this.getPredefinedProperties()) {
			regexpmap.put(property.name, property.validationRegexp);
		}
		return regexpmap;
	}
	
}
