/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.extension.factory.IPredefinedPropertySet;
import org.eclipse.team.svn.ui.extension.factory.PredefinedProperty;

/**
 * IPropertyProvider implementation
 *
 * @author Sergiy Logvin
 */
public class PredefinedPropertySet implements IPredefinedPropertySet {
	
	public List getPredefinedProperties(IResource []resources) {
		
		List properties = new ArrayList();		
		
		properties.add(new PredefinedProperty("svn:eol-style", this.getDescription("SVN.EOL"), ""));		
		properties.add(new PredefinedProperty("svn:executable", this.getDescription("SVN.Executable"), ""));
		properties.add(new PredefinedProperty("svn:externals", this.getDescription("SVN.Externals"), ""));
		properties.add(new PredefinedProperty("svn:ignore", this.getDescription("SVN.Ignore"), ""));
		properties.add(new PredefinedProperty("svn:keywords", this.getDescription("SVN.Keywords"), ""));
		properties.add(new PredefinedProperty("svn:needs-lock", this.getDescription("SVN.NeedsLock"), ""));
		properties.add(new PredefinedProperty("svn:mime-type", this.getDescription("SVN.Mimetype"), ""));
		this.getBugtrackProperties(properties);
		properties.add(new PredefinedProperty("tsvn:logtemplate", this.getDescription("TSVN.LogTemplate"), ""));
		properties.add(new PredefinedProperty("tsvn:logwidthmarker", this.getDescription("TSVN.LogWidthMarker"), ""));
		properties.add(new PredefinedProperty("tsvn:logminsize", this.getDescription("TSVN.LogMinSize"), ""));
		properties.add(new PredefinedProperty("tsvn:logfilelistenglish", this.getDescription("TSVN.LogFileListEnglish"), ""));
		properties.add(new PredefinedProperty("tsvn:projectlanguage", this.getDescription("TSVN.ProjectLanguage"), ""));
						
		return properties;
	}

	/**
	 * Allow to define custom bugtraq properties, clients should override this method.
	 * @param properties
	 */
	protected void getBugtrackProperties(List properties) {
		properties.add(new PredefinedProperty("bugtraq:url", this.getDescription("Bugtraq.URL"), "%BUGID%"));
		properties.add(new PredefinedProperty("bugtraq:logregex", this.getDescription("Bugtraq.LogRegex"), ""));
		properties.add(new PredefinedProperty("bugtraq:label", this.getDescription("Bugtraq.Label"), ""));
		properties.add(new PredefinedProperty("bugtraq:message", this.getDescription("Bugtraq.Message"), "%BUGID%"));
		properties.add(new PredefinedProperty("bugtraq:number", this.getDescription("Bugtraq.Number"), ""));
		properties.add(new PredefinedProperty("bugtraq:warnifnoissue", this.getDescription("Bugtraq.WarnIfNoIssue"), ""));
		properties.add(new PredefinedProperty("bugtraq:append", this.getDescription("Bugtraq.Append"), ""));
	}
	
	protected String getDescription(String id) {
		return SVNTeamUIPlugin.instance().getResource("Property." + id);
	}
	
}
