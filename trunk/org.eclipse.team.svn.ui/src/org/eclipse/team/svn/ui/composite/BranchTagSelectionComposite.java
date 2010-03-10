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

package org.eclipse.team.svn.ui.composite;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.svn.core.connector.SVNEntryReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.GetBranchesTagsOperation;
import org.eclipse.team.svn.ui.panel.common.RepositoryTreePanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.URLVerifier;

/**
 * Composite for branch/tag selection with appropriate revision
 * 
 * @author Alexei Goncharov
 */
public class BranchTagSelectionComposite extends Composite {
	public static final int BRANCH_OPERATED = 0;
	public static final int TAG_OPERATED = 1;
	
	protected Combo urlText;
	protected Button browse;
	protected UserInputHistory inputHistory;
	protected RevisionComposite revisionComposite;
	protected RevisionComposite secondRevisionComposite;
	protected IValidationManager validationManager;
	protected IRepositoryResource baseResource;
	protected boolean considerStructure;
	protected int type;
	protected String url;
	protected CompositeVerifier verifier;
	protected String selectionTitle;
	protected String selectionDescription;
	protected IRepositoryRoot root;
		
	protected IRepositoryResource[] branchTagResources;
	
	public BranchTagSelectionComposite(Composite parent, int style, IRepositoryResource baseResource, String historyKey, IValidationManager validationManager, int type, IRepositoryResource[] branchTagResources) {
		super(parent, style);
		this.baseResource = baseResource;
		this.inputHistory = new UserInputHistory(historyKey);
		this.validationManager = validationManager;
		this.type = type;
		this.branchTagResources = branchTagResources;
		this.considerStructure = BranchTagSelectionComposite.considerStructure(this.baseResource);
		this.root = BranchTagSelectionComposite.getRepositoryRoot(this.type, this.baseResource);
		this.createControls(parent);
	}
	
	protected void createControls (Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = layout.marginWidth = 0;
		this.setLayout(layout);
		
		Label resourceLabel = new Label(this, SWT.NONE);
		resourceLabel.setLayoutData(new GridData());
		if (this.type == BranchTagSelectionComposite.BRANCH_OPERATED) {
			resourceLabel.setText(SVNUIMessages.Select_Branch_Label);
		}
		else {
			resourceLabel.setText(SVNUIMessages.Select_Tag_Label);
		}
		
		Composite select = new Composite(this, SWT.NONE);	
		layout = new GridLayout();
		layout.numColumns = this.considerStructure ? 1 : 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		select.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		select.setLayoutData(data);
		
		final IRepositoryLocation location = this.baseResource.getRepositoryLocation();
				
		this.urlText = new Combo(select, this.considerStructure ? SWT.READ_ONLY : SWT.NULL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.urlText.setLayoutData(data);						
		
		if (!this.considerStructure) {
			this.urlText.setVisibleItemCount(this.inputHistory.getDepth() * 2);
			this.urlText.setItems(this.inputHistory.getHistory());			
			this.validationManager.attachTo(this.urlText, new NonEmptyFieldVerifier(resourceLabel.getText()));
			this.validationManager.attachTo(this.urlText, new URLVerifier(resourceLabel.getText()));					
		} else {
			for (IRepositoryResource branchTagResource : this.branchTagResources) {
				this.urlText.add(branchTagResource.getName());				
			}
			
			if (this.branchTagResources.length > 0) {
				this.urlText.select(0);	
				this.url = this.urlText.getText();
			}			
		}		
		
		Listener urlTextListener = new Listener() {
			public void handleEvent(Event e) {
				BranchTagSelectionComposite.this.url = ((Combo)e.widget).getText();
				BranchTagSelectionComposite.this.revisionComposite.setSelectedResource(BranchTagSelectionComposite.this.getSelectedResource());							
			}
		};
		this.urlText.addListener(SWT.Selection, urlTextListener);
		if (!this.considerStructure) {
			this.urlText.addListener(SWT.Modify, urlTextListener);
		} else {
			this.validationManager.attachTo(this.urlText, new AbstractVerifier() {
				@Override
				protected String getErrorMessage(Control input) {
					if (BranchTagSelectionComposite.this.branchTagResources.length == 0) {					
						return BranchTagSelectionComposite.this.type == BranchTagSelectionComposite.BRANCH_OPERATED ?
								SVNUIMessages.BranchTagSelectionComposite_NoBranches : SVNUIMessages.BranchTagSelectionComposite_NoTags;
					}
					return null;
				}		
				@Override
				protected String getWarningMessage(Control input) {
					return null;
				}	
			});
		}
		
		if (!this.considerStructure) {
			this.browse = new Button(select, SWT.PUSH);
			this.browse.setText(SVNUIMessages.Button_Browse);
			data = new GridData();
			data.widthHint = DefaultDialog.computeButtonWidth(this.browse);
			this.browse.setLayoutData(data);
			this.browse.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					RepositoryTreePanel panel;
					String part = BranchTagSelectionComposite.this.type == BranchTagSelectionComposite.BRANCH_OPERATED ? SVNUIMessages.Select_Branch_Title : SVNUIMessages.Select_Tag_Title;					
					panel = new RepositoryTreePanel(part,
							SVNUIMessages.RepositoryBrowsingPanel_Description,
							SVNUIMessages.RepositoryBrowsingPanel_Message,
							null, true, location, false);
					DefaultDialog browser = new DefaultDialog(BranchTagSelectionComposite.this.getShell(), panel);
					if (browser.open() == 0) {
						IRepositoryResource selectedResource = panel.getSelectedResource();
						boolean samePeg = selectedResource.getPegRevision().equals(BranchTagSelectionComposite.this.baseResource.getPegRevision());
						BranchTagSelectionComposite.this.urlText.setText(samePeg ? selectedResource.getUrl() : SVNUtility.getEntryReference(selectedResource).toString());						
						BranchTagSelectionComposite.this.revisionComposite.setSelectedResource(selectedResource);
						BranchTagSelectionComposite.this.validationManager.validateContent();
					}
				}
			});	
		}
		
		Composite revisions = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.numColumns = 2;
		revisions.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		revisions.setLayoutData(data);
		this.revisionComposite = new RevisionComposite(revisions, this.validationManager, true, new String[] {SVNUIMessages.RevisionComposite_Revision, SVNUIMessages.RepositoryResourceSelectionComposite_HeadRevision}, SVNRevision.HEAD, false) {
			public void additionalValidation() {
				BranchTagSelectionComposite.this.validationManager.validateContent();
			}
		};
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		this.revisionComposite.setLayoutData(data);
		this.revisionComposite.setSelectedResource(this.baseResource);
	}
	
	public void addUrlModifyListener(Listener listener) {
		this.urlText.addListener(SWT.Selection, listener);
		if (!this.considerStructure) {
			this.urlText.addListener(SWT.Modify, listener);	
		}
	}
	
	public void addUrlVerifier(AbstractVerifier verifier) {
		this.validationManager.attachTo(this.urlText, verifier);
	}
	
	public void saveChanges() {
		if (!this.considerStructure) {
			this.inputHistory.addLine(this.url);
		}
	}
	
	public void setCurrentRevision(long currentRevision) {
		this.revisionComposite.setCurrentRevision(currentRevision);
	}
	
	public IRepositoryResource getSelectedResource() {
		if (this.considerStructure && this.branchTagResources.length == 0) {
			return null;
		}
		String url = this.considerStructure ? (this.root.getUrl() + "/" + this.url) : this.url; //$NON-NLS-1$
		IRepositoryResource resource = this.getDestination(SVNUtility.asEntryReference(url), false);
		resource.setSelectedRevision(this.revisionComposite.getSelectedRevision());
		return resource;
	}
	
	protected IRepositoryResource getDestination(SVNEntryReference ref, boolean allowsNull) {
		if (ref == null) {
			return SVNUtility.copyOf(this.baseResource);
		}
		String url = SVNUtility.normalizeURL(ref.path);
		try {
			IRepositoryResource resource = this.baseResource instanceof IRepositoryContainer ? (IRepositoryResource)this.baseResource.asRepositoryContainer(url, false) : this.baseResource.asRepositoryFile(url, false);
			if (ref.pegRevision != null) {
				resource.setPegRevision(ref.pegRevision);
			}
			return resource;
		}
		catch (IllegalArgumentException ex) {
			return allowsNull ? null : SVNUtility.copyOf(this.baseResource);
		}
	}
	
	/**	
	 * Look for branches/tags for given resource.
	 * If error happens during execution or there are no branches/tags then
	 * show corresponding dialog. 
	 * 
	 * @param type			BRANCH_OPERATED or TAG_OPERATED
	 * @param resource
	 * @return	
	 */
	public static IRepositoryResource[] calculateBranchTagResources(IRepositoryResource resource, int type) {
		IRepositoryRoot root = BranchTagSelectionComposite.getRepositoryRoot(type, resource);
		GetBranchesTagsOperation op = new GetBranchesTagsOperation(root, type == BranchTagSelectionComposite.BRANCH_OPERATED);
		UIMonitorUtility.doTaskNowDefault(op, false);
		return op.getChildren();
	}
	
	protected static IRepositoryRoot getRepositoryRoot(int type, IRepositoryResource resource) {
		IRepositoryRoot root = (type == BranchTagSelectionComposite.BRANCH_OPERATED) ? SVNUtility.getBranchesLocation(resource) : SVNUtility.getTagsLocation(resource);
		return root;
	}
	
	public static boolean considerStructure(IRepositoryResource resource) {
		boolean considerStructure = 
			resource.getRepositoryLocation().isStructureEnabled() &&
			SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME);
		return considerStructure;
	}
	
	public static IRepositoryResource getResourceToCompareWith(IRepositoryResource resource, IRepositoryResource brunchTagResource) {
		/*
		 * When constructing resource to compare with we assume that
		 * user follows the recommended layout, i.e. trunk, branches, tags have the same parent;
		 * otherwise there will be unexpected results. 
		 * 
		 * E.g.
		 * Root/
		 * 	trunk/
		 *  branches/
		 *  tags/
		 *  
		 * If for some reason we can't construct the resource to compare with, we return null
		 * 
		 * Examples: 
		 * 	resource:	http://localhost/repos/first/layouts/multiple/trunk/Project/task.txt
		 * 	brunch:		http://localhost/repos/first/layouts/multiple/branches/br2
		 * 
		 *  resource:	http://localhost/repos/first/layouts/multiple/branches/br1/task.txt
		 * 	brunch:		http://localhost/repos/first/layouts/multiple/branches/br2
		 */					
		IRepositoryResource res = null;
		IPath fromPath = SVNUtility.createPathForSVNUrl(resource.getUrl());
		IPath brunchPath = SVNUtility.createPathForSVNUrl(brunchTagResource.getUrl());		
		if (brunchPath.segmentCount() > 2) {										
			int matchedSegments = fromPath.matchingFirstSegments(brunchPath);
			if (fromPath.segmentCount() >= matchedSegments + 1) {
				//remove common parent + ('trunk' or branch/tag name)
				IPath relativePath = fromPath.removeFirstSegments(matchedSegments + 1);
				String[] segments = relativePath.segments();
				String relativeUrl = ""; //$NON-NLS-1$
				//as IPath contains also device, we can't use its toString method
				for (int i = 0; i < segments.length; i ++) {
					relativeUrl += segments[i];
					if (i < segments.length - 1) {
						relativeUrl += "/"; //$NON-NLS-1$
					}
				}
				String compareUrl = brunchTagResource.getUrl() + "/" + relativeUrl; //$NON-NLS-1$
				res = resource instanceof IRepositoryFile ? brunchTagResource.asRepositoryFile(compareUrl, false) : brunchTagResource.asRepositoryContainer(compareUrl, false);	
			}						
		}		
		return res;						
	}

}
