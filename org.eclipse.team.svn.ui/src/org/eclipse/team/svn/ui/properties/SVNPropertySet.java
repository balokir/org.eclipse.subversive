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

package org.eclipse.team.svn.ui.properties;

import org.eclipse.team.svn.core.extension.properties.PredefinedProperty;
import org.eclipse.team.svn.core.extension.properties.PredefinedPropertySet;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * SVN properties specification
 *
 * @author Alexander Gurov
 */
public class SVNPropertySet extends PredefinedPropertySet {
	
	protected void init() {
		this.registerProperty(new PredefinedProperty(SVNUIMessages.AbstractPropertyEditPanel_svn_description, PredefinedProperty.TYPE_GROUP | PredefinedProperty.TYPE_COMMON));
		this.registerProperty(new PredefinedProperty("svn:eol-style", SVNUIMessages.Property_SVN_EOL, "", "((native)|(LF)|(CR)|(CRLF))", PredefinedProperty.TYPE_FILE));	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.registerProperty(new PredefinedProperty("svn:executable", SVNUIMessages.Property_SVN_Executable, "", null, PredefinedProperty.TYPE_FILE)); //$NON-NLS-1$ //$NON-NLS-2$
		this.registerProperty(new PredefinedProperty("svn:externals", SVNUIMessages.Property_SVN_Externals, "", null, PredefinedProperty.TYPE_FOLDER)); //$NON-NLS-1$ //$NON-NLS-2$
		this.registerProperty(new PredefinedProperty("svn:ignore", SVNUIMessages.Property_SVN_Ignore, "", "([^\\\\/\\:])+", PredefinedProperty.TYPE_FOLDER)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.registerProperty(new PredefinedProperty("svn:keywords", SVNUIMessages.Property_SVN_Keywords, "", "((Date)|(Revision)|(Author)|(HeadURL)|(Id)|(LastChangedDate)|(Rev)|(LastChangedRevision)|(LastChangedBy)|(URL)|(\\s))+", PredefinedProperty.TYPE_FILE)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.registerProperty(new PredefinedProperty("svn:mime-type", SVNUIMessages.Property_SVN_Mimetype, "", null, PredefinedProperty.TYPE_FILE)); //$NON-NLS-1$ //$NON-NLS-2$
		this.registerProperty(new PredefinedProperty("svn:mergeinfo", SVNUIMessages.Property_SVN_Mergeinfo, "")); //$NON-NLS-1$ //$NON-NLS-2$
		this.registerProperty(new PredefinedProperty("svn:needs-lock", SVNUIMessages.Property_SVN_NeedsLock, "", null, PredefinedProperty.TYPE_FILE)); //$NON-NLS-1$ //$NON-NLS-2$
		
		this.registerProperty(new PredefinedProperty(SVNUIMessages.AbstractPropertyEditPanel_revprop_description, PredefinedProperty.TYPE_GROUP | PredefinedProperty.TYPE_REVISION | PredefinedProperty.TYPE_COMMON));
		this.registerProperty(new PredefinedProperty("svn:log", SVNUIMessages.Property_SVN_Log, "", null, PredefinedProperty.TYPE_REVISION)); //$NON-NLS-1$ //$NON-NLS-2$
		this.registerProperty(new PredefinedProperty("svn:author", SVNUIMessages.Property_SVN_Author, "", null, PredefinedProperty.TYPE_REVISION)); //$NON-NLS-1$ //$NON-NLS-2$
		this.registerProperty(new PredefinedProperty("svn:date", SVNUIMessages.Property_SVN_Date, "", null, PredefinedProperty.TYPE_REVISION)); //$NON-NLS-1$ //$NON-NLS-2$
		this.registerProperty(new PredefinedProperty("svn:autoversioned", SVNUIMessages.Property_SVN_Autoversioned, "", null, PredefinedProperty.TYPE_REVISION)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
}
