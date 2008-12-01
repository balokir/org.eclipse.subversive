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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.core.connector.SVNEntryReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.GetRemoteFolderChildrenOperation;
import org.eclipse.team.svn.ui.panel.common.RepositoryTreePanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourceNameVerifier;

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
	
	protected String ignored;
		
	public BranchTagSelectionComposite(Composite parent, int style, IRepositoryResource baseResource, String historyKey, IValidationManager validationManager, int type) {
		super(parent, style);
		this.baseResource = baseResource;
		this.inputHistory = new UserInputHistory(historyKey);
		this.validationManager = validationManager;
		this.type = type;
		this.ignored = this.type == BranchTagSelectionComposite.BRANCH_OPERATED ? SVNUIMessages.Branch_Read_Separator : SVNUIMessages.Tag_Read_Separator;
		this.considerStructure = 
			baseResource.getRepositoryLocation().isStructureEnabled() &&
			SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME);
		this.root = (this.type == BranchTagSelectionComposite.BRANCH_OPERATED) ? SVNUtility.getBranchesLocation(this.baseResource) : SVNUtility.getTagsLocation(this.baseResource);
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
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		select.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		select.setLayoutData(data);
		
		final IRepositoryLocation location = this.baseResource.getRepositoryLocation();
				
		this.urlText = new Combo(select, SWT.NULL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.urlText.setLayoutData(data);
		this.urlText.setVisibleItemCount(this.inputHistory.getDepth() * 2);
		this.urlText.setItems(this.inputHistory.getHistory());
						
		IRepositoryResource [] children = new IRepositoryResource [0];
		if (this.considerStructure) {
			GetRemoteFolderChildrenOperation op = new GetRemoteFolderChildrenOperation(this.root, true);
			UIMonitorUtility.doTaskNowDefault(op, false);
			if (op.getChildren() != null) {
				children = op.getChildren();
			}
		}
		if (children.length > 0) {
			this.urlText.add(this.ignored);
		}
		for (int i = 0; i < children.length; i++) {
			this.urlText.add(children[i].getName());
		}
		this.urlText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				BranchTagSelectionComposite.this.url = ((Combo)e.widget).getText();
				BranchTagSelectionComposite.this.revisionComposite.setSelectedResource(BranchTagSelectionComposite.this.getSelectedResource());
			}
		});
		this.urlText.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int idx = BranchTagSelectionComposite.this.urlText.getSelectionIndex();
				if (idx != -1) {
	            	String comboText = BranchTagSelectionComposite.this.urlText.getItem(idx);
	            	if (comboText.equals(BranchTagSelectionComposite.this.ignored)) {
	            		BranchTagSelectionComposite.this.urlText.setText(""); //$NON-NLS-1$
	            	}
				}
			}
		});
		this.validationManager.attachTo(this.urlText, new ResourceNameVerifier(resourceLabel.getText(), true));
		this.validationManager.attachTo(this.urlText, new NonEmptyFieldVerifier(resourceLabel.getText()));
		
		this.browse = new Button(select, SWT.PUSH);
		this.browse.setText(SVNUIMessages.Button_Browse);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(this.browse);
		this.browse.setLayoutData(data);
		this.browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RepositoryTreePanel panel;
				String part = BranchTagSelectionComposite.this.type == BranchTagSelectionComposite.BRANCH_OPERATED ? SVNUIMessages.Select_Branch_Title : SVNUIMessages.Select_Tag_Title;
				if (BranchTagSelectionComposite.this.considerStructure) {
					panel = new RepositoryTreePanel(part,
							SVNUIMessages.RepositoryBrowsingPanel_Description,
							SVNUIMessages.RepositoryBrowsingPanel_Message,
							null, true, BranchTagSelectionComposite.this.root);
				}
				else {
					panel = new RepositoryTreePanel(part,
							SVNUIMessages.RepositoryBrowsingPanel_Description,
							SVNUIMessages.RepositoryBrowsingPanel_Message,
							null, true, location);
				}
				DefaultDialog browser = new DefaultDialog(BranchTagSelectionComposite.this.getShell(), panel);
				if (browser.open() == 0) {
					IRepositoryResource selectedResource = panel.getSelectedResource();
					boolean samePeg = selectedResource.getPegRevision().equals(BranchTagSelectionComposite.this.baseResource.getPegRevision());
					if (BranchTagSelectionComposite.this.considerStructure) {
						String toTrim = BranchTagSelectionComposite.this.root.getUrl();
						BranchTagSelectionComposite.this.urlText.setText(samePeg ? selectedResource.getUrl().substring(toTrim.length() + 1) : SVNUtility.getEntryReference(selectedResource).toString());
					}
					else {
						BranchTagSelectionComposite.this.urlText.setText(samePeg ? selectedResource.getUrl() : SVNUtility.getEntryReference(selectedResource).toString());
					}
					BranchTagSelectionComposite.this.revisionComposite.setSelectedResource(selectedResource);
					BranchTagSelectionComposite.this.validationManager.validateContent();
				}
			}
		});
		
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
	
	public void saveChanges() {
		this.inputHistory.addLine(this.url);
	}
	
	public void setCurrentRevision(long currentRevision) {
		this.revisionComposite.setCurrentRevision(currentRevision);
	}
	
	public IRepositoryResource getSelectedResource() {
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

}
