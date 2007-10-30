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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.property.GetMultiPropertiesOperation;
import org.eclipse.team.svn.core.operation.local.property.IPropertyProvider;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.view.property.PropertyKeywordEditPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Set keywords properties operation
 * 
 * @author Sergiy Logvin
 */
public class SetKeywordsAction extends AbstractNonRecursiveTeamAction {
	public SetKeywordsAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		SetKeywordsAction.doSetKeywords(this.getSelectedResources(IStateFilter.SF_VERSIONED));
	}
	
	public static void doSetKeywords(final IResource []resources) {
		if (!SVNTeamPreferences.getKeywordsBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.COMPUTE_KEYWORDS_NAME)) {
			SetKeywordsAction.queryUser(resources, new IResourceProvider() {
				public IResource[] getResources() {
					return resources;
				}
			}, null);
		}
		else {
			CompositeOperation composite = new CompositeOperation("Operation.SetKeywordsProperty");
			final GetMultiPropertiesOperation getKeywordsOp = new GetMultiPropertiesOperation(resources, IResource.DEPTH_INFINITE, IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED_FILES, PropertyData.KEYWORDS);
			composite.add(getKeywordsOp);
			composite.add(new AbstractNonLockingOperation(composite.getId()) {
				protected void runImpl(final IProgressMonitor monitor) throws Exception {
					if (!monitor.isCanceled()) {
						SetKeywordsAction.queryUser(resources, getKeywordsOp, getKeywordsOp);
					}
				}
			});
			UIMonitorUtility.doTaskScheduledActive(composite);
		}
	}
	
	protected static void queryUser(IResource []resources, IResourceProvider resourceProvider, IPropertyProvider propertyProvider) {
		final PropertyKeywordEditPanel panel = new PropertyKeywordEditPanel(resources, resourceProvider, propertyProvider);
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getShell(), panel);
				if (dialog.open() == 0) {
					panel.performKeywordChanges();
				}
			}
		});
	}

	public boolean isEnabled() {
		return this.checkForResourcesPresenceRecursive(IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED_FILES);
	}

}
