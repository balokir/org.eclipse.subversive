/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import org.eclipse.team.svn.core.connector.SVNProperty.BuiltIn;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.property.GetMultiPropertiesOperation;
import org.eclipse.team.svn.core.operation.local.property.IPropertyProvider;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.action.AbstractWorkingCopyAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.view.property.PropertyKeywordEditPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Set keywords properties operation
 * 
 * @author Sergiy Logvin
 */
public class SetKeywordsAction extends AbstractWorkingCopyAction {
	public SetKeywordsAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		SetKeywordsAction.doSetKeywords(this.getSelectedResources(IStateFilter.SF_VERSIONED));
	}
	
	public static void doSetKeywords(final IResource []resources) {
		if (!SVNTeamPreferences.getBehaviourBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BEHAVIOUR_COMPUTE_KEYWORDS_NAME)) {
			SetKeywordsAction.askUser(resources, new IResourceProvider() {
				public IResource[] getResources() {
					return resources;
				}
			}, null);
		}
		else {
			CompositeOperation composite = new CompositeOperation("Operation_SetKeywordsProperty", SVNUIMessages.class); //$NON-NLS-1$
			final GetMultiPropertiesOperation getKeywordsOp = new GetMultiPropertiesOperation(resources, IResource.DEPTH_INFINITE, IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED_FILES, BuiltIn.KEYWORDS);
			composite.add(getKeywordsOp);
			composite.add(new AbstractActionOperation(composite.getId(), composite.getMessagesClass()) {
				protected void runImpl(final IProgressMonitor monitor) throws Exception {
					if (!monitor.isCanceled()) {
						SetKeywordsAction.askUser(resources, getKeywordsOp, getKeywordsOp);
					}
				}
			});
			UIMonitorUtility.doTaskScheduledActive(composite);
		}
	}
	
	protected static void askUser(IResource []resources, IResourceProvider resourceProvider, IPropertyProvider propertyProvider) {
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
