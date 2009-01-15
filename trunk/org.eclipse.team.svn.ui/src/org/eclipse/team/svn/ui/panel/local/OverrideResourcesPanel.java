/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.ResourceSelectionComposite;
import org.eclipse.team.svn.ui.panel.participant.BasePaneParticipant;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.ui.synchronize.ResourceScope;

/**
 * Override and commit/update panel implementation
 * 
 * @author Alexander Gurov
 */
public class OverrideResourcesPanel extends AbstractResourceSelectionPanel {
	public static final int MSG_COMMIT = 0;
	public static final int MSG_UPDATE = 1;
	
	protected IResource [] affectedResource;

	protected IResourceStatesListener resourceStatesListener;
	
	protected static final String []MESSAGES = new String[] {
		"OverrideResourcesPanel_Description_Commit", //$NON-NLS-1$
		"OverrideResourcesPanel_Description_Update" //$NON-NLS-1$
	};
	
	public OverrideResourcesPanel(IResource[] resources, IResource[] userSelectedResources, int msgId) {
		this(resources, userSelectedResources, msgId, new IResource [] {});
	}

	
    public OverrideResourcesPanel(IResource[] resources, IResource[] userSelectedResources, int msgId, IResource [] affectedResources) {
        super(resources, userSelectedResources, new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL});
        this.dialogTitle = SVNUIMessages.OverrideResourcesPanel_Title;
        this.dialogDescription = SVNUIMessages.getString(OverrideResourcesPanel.MESSAGES[msgId]);
        boolean isParticipantPane = this.paneParticipantHelper.isParticipantPane();
        this.defaultMessage = isParticipantPane ? SVNUIMessages.OverrideResourcesPanel_Pane_Message : SVNUIMessages.OverrideResourcesPanel_Message;
        this.affectedResource = affectedResources; 
    }
		
	protected String getDialogID() {
	   	return super.getDialogID() + (this.affectedResource.length > 0 ? "Affected" : ""); 
	}

	public void createControlsImpl(Composite parent) {
		super.createControlsImpl(parent);
		if (this.affectedResource.length == 0) {
			return;
		}
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label description = new Label(parent, SWT.LEFT | SWT.WRAP);
		description.setText(SVNUIMessages.OverrideResourcesPanel_Affected);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		data.widthHint = 300;
		description.setLayoutData(data);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 100;
		ResourceSelectionComposite affectedResourcesComposite = new ResourceSelectionComposite(parent, SWT.NONE, this.affectedResource, false, false);
		affectedResourcesComposite.setLayoutData(data);
		this.attachTo(affectedResourcesComposite, new AbstractVerifier() {
			protected String getErrorMessage(Control input) {
				return null;
			}

			protected String getWarningMessage(Control input) {
				return SVNUIMessages.OverrideResourcesPanel_Affected_Warning;
			}
		});
	}
	
	protected void updateResources(ResourceStatesChangedEvent event) {
		HashSet<IResource> allResources = new HashSet<IResource>(Arrays.asList(this.resources));
		
		HashSet<IResource> toDeleteSet = new HashSet<IResource>();
		toDeleteSet.addAll(Arrays.asList(FileUtility.getResourcesRecursive(event.resources, IStateFilter.SF_NOTMODIFIED, IResource.DEPTH_ZERO)));
		toDeleteSet.addAll(Arrays.asList(FileUtility.getResourcesRecursive(event.resources, IStateFilter.SF_NOTEXISTS, IResource.DEPTH_ZERO)));
		toDeleteSet.addAll(Arrays.asList(FileUtility.getResourcesRecursive(event.resources, IStateFilter.SF_IGNORED, IResource.DEPTH_ZERO)));
		
		allResources.removeAll(toDeleteSet);
		
		final IResource[] newResources = allResources.toArray(new IResource[allResources.size()]);
		
		if (!this.paneParticipantHelper.isParticipantPane()) {
			UIMonitorUtility.getDisplay().syncExec(new Runnable() {
				public void run() {
					//FIXME isDisposed() test is necessary as dispose() method is not called from FastTrack Commit Dialog
					if (!OverrideResourcesPanel.this.selectionComposite.isDisposed()) {
						OverrideResourcesPanel.this.selectionComposite.setResources(newResources);
						OverrideResourcesPanel.this.selectionComposite.fireSelectionChanged();
					}
				}
			});	
		}
		
		this.resources = newResources;
	}	
	
	public void postInit() {
	   	super.postInit();
	   	this.validateContent();
	   	
	   	this.resourceStatesListener = new IResourceStatesListener() {
			public void resourcesStateChanged(ResourceStatesChangedEvent event) {
				OverrideResourcesPanel.this.updateResources(event);
			}
		};
		SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, OverrideResourcesPanel.this.resourceStatesListener);
	}
    
	public void dispose() {
    	super.dispose();
    	
    	SVNRemoteStorage.instance().removeResourceStatesListener(ResourceStatesChangedEvent.class, this.resourceStatesListener);	    	    	
    }
	
    public String getHelpId() {
    	return "org.eclipse.team.svn.help.overrideDialogContext"; //$NON-NLS-1$
    }
	
	protected BasePaneParticipant createPaneParticipant() {
		return new BasePaneParticipant(new ResourceScope(this.resources), this) {
			protected Collection<AbstractSynchronizeActionGroup> getActionGroups() {
				Collection<AbstractSynchronizeActionGroup> actionGroups = new ArrayList<AbstractSynchronizeActionGroup>();
				actionGroups.add(new BasePaneActionGroup(this.validationManager));
		    	return actionGroups;
			}
		};
	}
}
