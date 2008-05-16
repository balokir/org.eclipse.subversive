/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.extension.factory.PredefinedProperty;
import org.eclipse.team.svn.ui.preferences.SVNTeamPropsPreferencePage.CustomProperty;

/**
 * Panel for editing revision properties.
 * 
 * @author Alexei Goncharov
 */
public class RevPropertiesEditPanel extends AbstractPropertyEditPanel {

	protected SVNRevision revision;
	protected IRepositoryLocation location;
	
	/**
	 * Creates a panel. 
	 * 
	 * @param revProperties - existent revision properties
	 * @param revision - the revision to edit properties for
	 * @param location - the repository location
	 */
	public RevPropertiesEditPanel(SVNProperty[] revProperties, SVNRevision revision, IRepositoryLocation location) {
		super(revProperties,
				SVNTeamUIPlugin.instance().getResource("RevisionPropertyEditPanel.Title"),
				SVNTeamUIPlugin.instance().getResource("RevisionPropertyEditPanel.Description", new String [] {String.valueOf(revision)}));
		this.revision = revision;
		this.location = location;
		ArrayList<CustomProperty> customPropList = new ArrayList<CustomProperty>(Arrays.asList(this.customProps));
		ArrayList<String> givenNames = new ArrayList<String>();
		for (CustomProperty current : this.customProps) {
			givenNames.add(current.propName);
		}
		for (PredefinedProperty current : this.predefinedProperties) {
			givenNames.add(current.name);
		}
		for (SVNProperty current : revProperties) {
			if (!givenNames.contains(current.name)) {
				customPropList.add(new CustomProperty(current.name, "", ""));
			}
		}
		this.customProps = customPropList.toArray(new CustomProperty[customPropList.size()]);
		this.fillVerifiersMap();
	}

	protected void saveChangesImpl() {
		super.saveChangesImpl();
	}
	
	protected void cancelChangesImpl() {
	}

	protected void fillVerifiersMap() {
	}

	protected List<PredefinedProperty> getPredefinedProperties() {
		ArrayList<PredefinedProperty> properties = new ArrayList<PredefinedProperty>();
		properties.add(new PredefinedProperty(SVNTeamUIPlugin.instance().getResource("AbstractPropertyEditPanel.svn_description"), "", ""));
		properties.add(new PredefinedProperty("svn:log", this.getDescription("SVN.Log"), ""));		
		properties.add(new PredefinedProperty("svn:author", this.getDescription("SVN.Author"), ""));
		properties.add(new PredefinedProperty("svn:date", this.getDescription("SVN.Date"), ""));
		properties.add(new PredefinedProperty("svn:autoversioned", this.getDescription("SVN.Autoversioned"), ""));
		return properties;
	}
	
	protected String getDescription(String id) {
		return SVNTeamUIPlugin.instance().getResource("Property." + id);
	}

	protected Map<String, String> getPredefinedPropertiesRegexps() {
		return Collections.emptyMap();
	}
	
}