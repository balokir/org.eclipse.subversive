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
 * Bugtraq properties specification
 *
 * @author Alexander Gurov
 */
public class BugtraqPropertySet extends PredefinedPropertySet {
	
	protected void init() {
		this.registerProperty(new PredefinedProperty(SVNUIMessages.PropertyEditPanel_bugtraq_description, PredefinedProperty.TYPE_GROUP | PredefinedProperty.TYPE_COMMON));
		this.registerProperty(new PredefinedProperty("bugtraq:url", SVNUIMessages.Property_Bugtraq_URL, "%BUGID%", "((http:\\/\\/)|(https:\\/\\/))(\\S+)?((\\%BUGID\\%))(\\S+)?")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.registerProperty(new PredefinedProperty("bugtraq:logregex", SVNUIMessages.Property_Bugtraq_LogRegex, "")); //$NON-NLS-1$ //$NON-NLS-2$
		this.registerProperty(new PredefinedProperty("bugtraq:label", SVNUIMessages.Property_Bugtraq_Label, "")); //$NON-NLS-1$ //$NON-NLS-2$
		this.registerProperty(new PredefinedProperty("bugtraq:message", SVNUIMessages.Property_Bugtraq_Message, "%BUGID%")); //$NON-NLS-1$ //$NON-NLS-2$
		this.registerProperty(new PredefinedProperty("bugtraq:number", SVNUIMessages.Property_Bugtraq_Number, "", "((true)|(false))")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.registerProperty(new PredefinedProperty("bugtraq:warnifnoissue", SVNUIMessages.Property_Bugtraq_WarnIfNoIssue, "", "((true)|(false))")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.registerProperty(new PredefinedProperty("bugtraq:append", SVNUIMessages.Property_Bugtraq_Append, "", "((true)|(false))")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
}
