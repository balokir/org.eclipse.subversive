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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.property.IRevisionPropertiesProvider;
import org.eclipse.team.svn.core.operation.remote.SetRevisionPropertyOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.events.RevisonPropertyChangeEvent;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.extension.factory.PredefinedProperty;
import org.eclipse.team.svn.ui.preferences.SVNTeamPropsPreferencePage.CustomProperty;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.verifier.PropertyVerifier;

/**
 * Panel for editing revision properties.
 * 
 * @author Alexei Goncharov
 */
public class RevPropertiesEditPanel extends AbstractPropertyEditPanel {

	protected SVNRevision revision;
	
	/**
	 * Creates a panel. 
	 * 
	 * @param revProperties - existent revision properties
	 * @param revision - the revision to edit properties for
	 */
	public RevPropertiesEditPanel(SVNProperty[] revProperties, SVNRevision revision) {
		super(revProperties,
				SVNUIMessages.RevisionPropertyEditPanel_Title,
				SVNUIMessages.format(SVNUIMessages.RevisionPropertyEditPanel_Description, new String [] {String.valueOf(revision)}));
		this.revision = revision;
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
				customPropList.add(new CustomProperty(current.name, "", "")); //$NON-NLS-1$ //$NON-NLS-2$
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
		for (PredefinedProperty current : this.predefinedProperties) {
			this.verifiers.put(current.name, new PropertyVerifier("EditPropertiesInputField", current.name.equals("svn:autoversioned") ? null : "", current.name, null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	
	protected List<PredefinedProperty> getPredefinedProperties() {
		ArrayList<PredefinedProperty> properties = new ArrayList<PredefinedProperty>();
		properties.add(new PredefinedProperty(SVNUIMessages.AbstractPropertyEditPanel_svn_description, "", "")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.add(new PredefinedProperty("svn:log", this.getDescription("SVN_Log"), ""));		 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		properties.add(new PredefinedProperty("svn:author", this.getDescription("SVN_Author"), "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		properties.add(new PredefinedProperty("svn:date", this.getDescription("SVN_Date"), "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		properties.add(new PredefinedProperty("svn:autoversioned", this.getDescription("SVN_Autoversioned"), "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return properties;
	}
	
	protected String getDescription(String id) {
		return SVNUIMessages.getString("Property_" + id); //$NON-NLS-1$
	}

	protected Map<String, String> getPredefinedPropertiesRegexps() {
		return Collections.emptyMap();
	}
	
	public static void doSetRevisionProperty(RevPropertiesEditPanel panel, final IRepositoryLocation location, final SVNRevision revision) {
		final SVNProperty []data = new SVNProperty[] {new SVNProperty(panel.getPropertyName(), panel.getPropertyValue())};
		SetRevisionPropertyOperation setPropOp = null;
		CompositeOperation op = new CompositeOperation(""); //$NON-NLS-1$
		if (panel.isFileSelected()) {
			final File f = new File(panel.getPropertyFile());
			AbstractActionOperation loadOp = new AbstractActionOperation("Operation_SLoadFileContent") { //$NON-NLS-1$
	            protected void runImpl(IProgressMonitor monitor) throws Exception {
	                FileInputStream input = null;
	                try {
	                    input = new FileInputStream(f);
	                    byte []binary = new byte[(int)f.length()];
	                    input.read(binary);
	                    data[0] = new SVNProperty(data[0].name, new String(binary));
	                }
	                finally {
	                    if (input != null) {
	                        input.close();
	                    }
	                }
	            }
	        };
	        op.add(loadOp);
			IRevisionPropertiesProvider provider = new IRevisionPropertiesProvider() {
				public SVNProperty[] getRevisionProperties() {
					return data;
				}
			};
			setPropOp = new SetRevisionPropertyOperation(location, revision, provider);
		}
		else {
			setPropOp = new SetRevisionPropertyOperation(location, revision, data[0]);
		}
		op.setOperationName(setPropOp.getOperationName());
		op.add(setPropOp);
		op.add(new AbstractActionOperation(setPropOp.getOperationName()) {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				SVNRemoteStorage.instance().fireRevisionPropertyChangeEvent(new RevisonPropertyChangeEvent(
						RevisonPropertyChangeEvent.SET, 
						revision,
						location,
						data[0]));
			}			
		}, new IActionOperation [] {setPropOp});
		UIMonitorUtility.doTaskNowDefault(op, true);
	}
	
}