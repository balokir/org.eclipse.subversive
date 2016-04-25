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
 * TortoiseSVN properties specification
 *
 * @author Alexander Gurov
 */
public class TortoiseSVNPropertySet extends PredefinedPropertySet {
	
	protected void init() {
		this.registerProperty(new PredefinedProperty(SVNUIMessages.PropertyEditPanel_tsvn_description, PredefinedProperty.TYPE_GROUP | PredefinedProperty.TYPE_COMMON));
		this.registerProperty(new PredefinedProperty("tsvn:logtemplate", SVNUIMessages.Property_TSVN_LogTemplate, "")); //$NON-NLS-1$ //$NON-NLS-2$
		this.registerProperty(new PredefinedProperty("tsvn:logwidthmarker", SVNUIMessages.Property_TSVN_LogWidthMarker, "", "(\\d+)")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.registerProperty(new PredefinedProperty("tsvn:logminsize", SVNUIMessages.Property_TSVN_LogMinSize, "", "(\\d+)")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.registerProperty(new PredefinedProperty("tsvn:lockmsgminsize", SVNUIMessages.Property_TSVN_LockMsgMinSize, "", "(\\d+)")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.registerProperty(new PredefinedProperty("tsvn:logfilelistenglish", SVNUIMessages.Property_TSVN_LogFileListEnglish, "", "((true)|(false))")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.registerProperty(new PredefinedProperty("tsvn:projectlanguage", SVNUIMessages.Property_TSVN_ProjectLanguage, "")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
}
