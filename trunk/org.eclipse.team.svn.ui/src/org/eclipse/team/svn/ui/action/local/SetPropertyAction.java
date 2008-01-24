/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.local;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.property.IPropertyProvider;
import org.eclipse.team.svn.core.operation.local.property.SetMultiPropertiesOperation;
import org.eclipse.team.svn.core.operation.local.property.SetPropertiesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.utility.StringMatcher;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.composite.PropertiesComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.view.property.PropertyEditPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Action to set property on one or more resources
 * 
 * @author Sergiy Logvin
 */
public class SetPropertyAction extends AbstractNonRecursiveTeamAction {
	
	public SetPropertyAction() {
		super();
	}

	public void runImpl(IAction action) {
		final IResource []resources = this.getSelectedResources(IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED);
		
		PropertyEditPanel panel = new PropertyEditPanel(null, resources, false);
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == Dialog.OK) {
			SetPropertyAction.doSetProperty(resources, panel, null);
		}
	}
	
	public static void doSetProperty(final IResource []resources, PropertyEditPanel panel, IActionOperation addOn) {
		SetPropertyAction.doSetProperty(resources, panel.getPropertyName(), panel.getPropertyValue(), panel.getPropertyFile(), panel.isFileSelected(), panel.isRecursiveSelected(), panel.getApplyMethod(), panel.useMask(), panel.getFilterMask(), panel.isStrict(), addOn);
	}
	
	public static void doSetProperty(final IResource []resources, String propertyName, String value, String fileName, boolean isFileSelected, boolean isRecursive, final int applyMethod, boolean useMask, String filterMask, boolean strict, IActionOperation addOn) {
		final SVNProperty []data = SetPropertyAction.getPropertyData(propertyName, isFileSelected ? fileName : value, isFileSelected);
		IActionOperation loadOp = null;
		if (isFileSelected) {
			final File f = new File(fileName);
	        loadOp = new AbstractActionOperation("Operation.SLoadFileContent") {
	            protected void runImpl(IProgressMonitor monitor) throws Exception {
	                FileInputStream input = null;
	                try {
	                    input = new FileInputStream(f);
	                    input.read(data[0].data);
	                }
	                finally {
	                    if (input != null) {
	                        input.close();
	                    }
	                }
	            }
	        };
		}
		SetPropertyAction.doSetProperty(resources, data, loadOp, isRecursive, applyMethod, useMask, filterMask, strict, addOn);
	}
	
	public static void doSetProperty(final IResource []resources, final SVNProperty []data, IActionOperation loadOp, boolean isRecursive, final int applyMethod, boolean useMask, String filterMask, boolean strict, IActionOperation addOn) {
		IResourceProvider resourceProvider = new IResourceProvider() {
			public IResource []getResources() {
				return resources;
			}
		};
		IActionOperation mainOp;
		if (!isRecursive || applyMethod == PropertiesComposite.APPLY_TO_ALL && !useMask) {
			// use faster version
			mainOp = new SetPropertiesOperation(resourceProvider, data, isRecursive & !strict);
		}
		else {
			final StringMatcher matcher = useMask ? new StringMatcher(filterMask) : null; 

			IPropertyProvider propertyProvider = new IPropertyProvider() {
				public SVNProperty []getProperties(IResource resource) {
					return data;
				}
			};
			IStateFilter filter = new IStateFilter.AbstractStateFilter() {
				protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
					return IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED.allowsRecursion(resource, state, mask) || state == IStateFilter.ST_ADDED;
				}
				protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
					if (applyMethod == PropertiesComposite.APPLY_TO_FILES && resource.getType() != IResource.FILE ||
						applyMethod == PropertiesComposite.APPLY_TO_FOLDERS && resource.getType() == IResource.FILE ||
						!IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED.accept(resource, state, mask) || 
						matcher != null && !matcher.match(resource.getName())) {
						return false;
					}
					return true;
				}
			};
			mainOp = new SetMultiPropertiesOperation(resourceProvider, propertyProvider, filter, isRecursive && !strict ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO);
		}
		CompositeOperation composite = new CompositeOperation(mainOp.getId());
		if (loadOp != null) {
			composite.add(loadOp);
			composite.add(mainOp, new IActionOperation[] {loadOp});
		}
		else {
			composite.add(mainOp);
		}
		if (addOn != null) {
			composite.add(addOn);
		}
		composite.add(new RefreshResourcesOperation(resourceProvider, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL), new IActionOperation[] {mainOp});
		UIMonitorUtility.doTaskScheduledWorkspaceModify(composite);
	}
	
	protected static SVNProperty []getPropertyData(String name, String data, boolean isFileSelected) {
		SVNProperty retVal = isFileSelected ? new SVNProperty(name, null, new byte[(int)new File(data).length()]) : new SVNProperty(name, data, null);
		return new SVNProperty[] {retVal};
	}

	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED);
	}
	
}
