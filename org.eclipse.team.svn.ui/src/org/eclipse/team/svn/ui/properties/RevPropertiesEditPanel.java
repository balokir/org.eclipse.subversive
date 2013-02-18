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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
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
	
	protected boolean isPropertyAccepted(PredefinedProperty property) {
		return (property.type & PredefinedProperty.TYPE_REVISION) != 0;
	}
	
	public static void doSetRevisionProperty(RevPropertiesEditPanel panel, final IRepositoryLocation location, final SVNRevision revision) {
		final SVNProperty []data = new SVNProperty[] {new SVNProperty(panel.getPropertyName(), panel.getPropertyValue())};
		SetRevisionPropertyOperation setPropOp = null;
		CompositeOperation op = new CompositeOperation("", SVNUIMessages.class); //$NON-NLS-1$
		if (panel.isFileSelected()) {
			final File f = new File(panel.getPropertyFile());
			AbstractActionOperation loadOp = new AbstractActionOperation("Operation_SLoadFileContent", SVNUIMessages.class) { //$NON-NLS-1$
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
		op.add(new AbstractActionOperation(setPropOp.getOperationName(), SVNMessages.class) {
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