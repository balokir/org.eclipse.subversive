/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.composite;

import java.text.MessageFormat;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.common.RepositoryTreePanel;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbsolutePathVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.URLVerifier;

/**
 * Repository resource selection composite
 * 
 * @author Alexander Gurov
 */
public class RepositoryResourceSelectionComposite extends Composite {
	public static final int MODE_DEFAULT = 0;
	public static final int MODE_TWO = 1;
	public static final int MODE_AUTO = 2;
	
	protected Combo urlText;
	protected Button browse;
	protected UserInputHistory urlHistory;
	protected RevisionComposite revisionComposite;
	protected RevisionComposite secondRevisionComposite;
	protected IValidationManager validationManager;
	protected IRepositoryResource baseResource;
	protected boolean stopOnCopy;
	protected int twoRevisions;
	
	protected IRepositoryResource selectedResource;
	protected IRepositoryResource secondSelectedResource;
	
	protected CompositeVerifier verifier;
	
	protected String selectionTitle;
	protected String selectionDescription;
	protected String comboId;

	public RepositoryResourceSelectionComposite(Composite parent, int style, IValidationManager validationManager, String historyKey, IRepositoryResource baseResource, boolean stopOnCopy, String selectionTitle, String selectionDescription, int twoRevisions) {
		this(parent, style, validationManager, historyKey, "RepositoryResourceSelectionComposite.URL", baseResource, stopOnCopy, selectionTitle, selectionDescription, twoRevisions);
	}
	
	public RepositoryResourceSelectionComposite(Composite parent, int style, IValidationManager validationManager, String historyKey, String comboId, IRepositoryResource baseResource, boolean stopOnCopy, String selectionTitle, String selectionDescription, int twoRevisions) {
		super(parent, style);
		this.stopOnCopy = stopOnCopy;
		this.urlHistory = new UserInputHistory(historyKey);
		this.validationManager = validationManager;
		this.baseResource = baseResource;
		this.selectedResource = this.getDestination(this.baseResource.getUrl());
		this.secondSelectedResource = this.getDestination(this.baseResource.getUrl());
		this.selectionTitle = selectionTitle;
		this.selectionDescription = selectionDescription;
		this.twoRevisions = twoRevisions;
		this.comboId = comboId;
		this.createControls();
	}

	public IRepositoryResource getSelectedResource() {
		this.selectedResource.setSelectedRevision(this.revisionComposite.getSelectedRevision());
		this.selectedResource.setPegRevision(this.baseResource.getPegRevision());
		return this.selectedResource;
	}
	
	public void setSelectedResource(IRepositoryResource resource, SVNRevision secondRevision) {
		this.selectedResource = resource;
		this.revisionComposite.setSelectedResource(SVNUtility.copyOf(this.selectedResource));
		if (this.secondRevisionComposite != null) {
			this.secondSelectedResource = SVNUtility.copyOf(resource);
			this.secondSelectedResource.setSelectedRevision(secondRevision != null ? secondRevision : SVNRevision.HEAD);
			this.secondRevisionComposite.setSelectedResource(this.secondSelectedResource);
		}
		this.setUrl(this.selectedResource.getUrl());
	}
	
	public SVNRevision getStartRevision() {
		return this.revisionComposite.getSelectedRevision();
	}
	
	public SVNRevision getSecondSelectedRevision() {
		if (this.secondSelectedResource == null || this.secondRevisionComposite == null) {
			return null;
		}
		return this.secondRevisionComposite.getSelectedRevision();
	}
	
	public void setCurrentRevision(long currentRevision) {
		this.revisionComposite.setCurrentRevision(currentRevision);
	}
	
	public void addVerifier(AbstractVerifier verifier) {
		this.verifier.add(verifier);
	}
	
	public void removeVerifier(AbstractVerifier verifier) {
		this.verifier.remove(verifier);
	}
	
	public void setUrl(String url) {
		this.urlText.setText(url);
	}
	
	public String getUrl() {
		return this.urlText.getText();
	}
	
	public void saveHistory() {
		this.urlHistory.addLine(this.urlText.getText());
	}
	
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.urlText.setEnabled(enabled);
		this.browse.setEnabled(enabled);
		this.revisionComposite.setEnabled(enabled);
		if (this.secondRevisionComposite != null) {
			this.secondRevisionComposite.setEnabled(enabled);
		}
	}
	
	private void createControls() {
		GridLayout layout = null;
		GridData data = null;
		
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = layout.marginWidth = 0;
		this.setLayout(layout);
		
		Label urlLabel = new Label(this, SWT.NONE);
		urlLabel.setLayoutData(new GridData());
		urlLabel.setText(SVNTeamUIPlugin.instance().getResource(this.comboId));
		
		Composite select = new Composite(this, SWT.NONE);	
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		select.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		select.setLayoutData(data);	
		
		this.urlText = new Combo(select, SWT.NULL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.urlText.setLayoutData(data);
		this.urlText.setVisibleItemCount(this.urlHistory.getDepth());
		this.urlText.setItems(this.urlHistory.getHistory());
		this.urlText.setText(this.selectedResource.getUrl());
		this.urlText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				RepositoryResourceSelectionComposite.this.checkUrl();
				RepositoryResourceSelectionComposite.this.revisionComposite.setSelectedResource(RepositoryResourceSelectionComposite.this.selectedResource);
				if (RepositoryResourceSelectionComposite.this.secondRevisionComposite != null) {
					RepositoryResourceSelectionComposite.this.secondRevisionComposite.setSelectedResource(RepositoryResourceSelectionComposite.this.secondSelectedResource);
				}
			}
		});
		this.verifier = new CompositeVerifier();
		this.verifier.add(new NonEmptyFieldVerifier(SVNTeamUIPlugin.instance().getResource(this.comboId + ".Verifier")));
		this.verifier.add(new URLVerifier(SVNTeamUIPlugin.instance().getResource(this.comboId + ".Verifier")) {
			protected String getErrorMessage(Control input) {
				String error = super.getErrorMessage(input);
				if (error == null) {
					String url = this.getText(input);
					if (RepositoryResourceSelectionComposite.this.getDestination(url) == null) {
						error = MessageFormat.format(SVNTeamUIPlugin.instance().getResource("RepositoryResourceSelectionComposite.URL.Verifier.Error"), new String[] {url, RepositoryResourceSelectionComposite.this.selectedResource.getRepositoryLocation().getUrl()});
					}
				}
				return error;
			}
		});
		this.verifier.add(new AbsolutePathVerifier(this.comboId));
		this.validationManager.attachTo(this.urlText, this.verifier);
		
		this.browse = new Button(select, SWT.PUSH);
		this.browse.setText(SVNTeamUIPlugin.instance().getResource("Button.Browse"));
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(this.browse);
		this.browse.setLayoutData(data);
		this.browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RepositoryTreePanel panel = 
			        new RepositoryTreePanel(
			        	SVNTeamUIPlugin.instance().getResource("RepositoryResourceSelectionComposite.Select.Title"), 
						RepositoryResourceSelectionComposite.this.selectionTitle,
						RepositoryResourceSelectionComposite.this.selectionDescription,
						new IRepositoryResource[] {RepositoryResourceSelectionComposite.this.baseResource}, 
						true);
				panel.setAllowFiles(RepositoryResourceSelectionComposite.this.baseResource instanceof IRepositoryFile);
				DefaultDialog browser = new DefaultDialog(RepositoryResourceSelectionComposite.this.getShell(), panel);
				if (browser.open() == 0) {
					RepositoryResourceSelectionComposite.this.selectedResource = panel.getSelectedResource();
					RepositoryResourceSelectionComposite.this.urlText.setText(RepositoryResourceSelectionComposite.this.selectedResource.getUrl());
					RepositoryResourceSelectionComposite.this.revisionComposite.setSelectedResource(RepositoryResourceSelectionComposite.this.selectedResource);
					if (RepositoryResourceSelectionComposite.this.secondRevisionComposite != null) {
						RepositoryResourceSelectionComposite.this.secondRevisionComposite.setSelectedResource(RepositoryResourceSelectionComposite.this.secondSelectedResource =
							panel.getSelectedResource());
					}
					RepositoryResourceSelectionComposite.this.checkUrl();
					RepositoryResourceSelectionComposite.this.validationManager.validateContent();
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
		this.revisionComposite = new RevisionComposite(revisions, this.validationManager, this.stopOnCopy, new String[] {((this.twoRevisions & MODE_TWO) != 0 ? SVNTeamUIPlugin.instance().getResource("RepositoryResourceSelectionComposite.StartRevision") : SVNTeamUIPlugin.instance().getResource("RepositoryResourceSelectionComposite.Revision")), (this.twoRevisions & MODE_AUTO) != 0 ? SVNTeamUIPlugin.instance().getResource("RepositoryResourceSelectionComposite.Autodetect") : SVNTeamUIPlugin.instance().getResource("RepositoryResourceSelectionComposite.HeadRevision")}, (this.twoRevisions & MODE_AUTO) != 0 ? null : SVNRevision.HEAD) {
			public void additionalValidation() {
				RepositoryResourceSelectionComposite.this.validateRevisions();
			}
		};
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = (this.twoRevisions & MODE_TWO) != 0 ? 1 : 2;
		this.revisionComposite.setLayoutData(data);
		this.revisionComposite.setSelectedResource(this.selectedResource);
		if ((this.twoRevisions & MODE_TWO) != 0) {
			this.secondRevisionComposite = new RevisionComposite(revisions, this.validationManager, this.stopOnCopy, new String[] {SVNTeamUIPlugin.instance().getResource("RepositoryResourceSelectionComposite.StopRevision"), SVNTeamUIPlugin.instance().getResource("RepositoryResourceSelectionComposite.HeadRevision")}, SVNRevision.HEAD) {
				public void additionalValidation() {
					RepositoryResourceSelectionComposite.this.validateRevisions();
				}
			};
			data = new GridData(GridData.FILL_HORIZONTAL);
			this.secondRevisionComposite.setLayoutData(data);
			this.secondRevisionComposite.setSelectedResource(this.secondSelectedResource);
//			this.validationManager.attachTo(this.secondRevisionComposite, new AbstractVerifier() {
//
//				protected String getErrorMessage(Control input) {
//					Revision startRevision = RepositoryResourceSelectionComposite.this.revisionComposite.getSelectedRevision();
//					Revision stopRevision = RepositoryResourceSelectionComposite.this.secondRevisionComposite.getSelectedRevision();
//					if (startRevision != null && startRevision.getKind() == RevisionKind.number && stopRevision.getKind() == RevisionKind.number &&
//							((Revision.Number)startRevision).getNumber() > ((Revision.Number)stopRevision).getNumber()) {
//						return "Stop revision cannot be less than start revision.";
//					}
//					return null;
//				}
//
//				protected String getWarningMessage(Control input) {
//					return null;
//				}
//			});
		}
	}
	
	protected void checkUrl() {
		String url = this.urlText.getText().trim();
		IRepositoryResource tmp = this.getDestination(url);
		if (tmp != null) {
			tmp.setSelectedRevision(this.selectedResource.getSelectedRevision());
			this.selectedResource = tmp;
			tmp = this.getDestination(url);
			tmp.setSelectedRevision(this.secondSelectedResource.getSelectedRevision());
			this.secondSelectedResource = tmp;
			this.revisionComposite.setEnabled(true);
			if (this.secondRevisionComposite != null) {
				this.secondRevisionComposite.setEnabled(true);
			}
		}
		else {
			this.revisionComposite.setEnabled(false);
			if (this.secondRevisionComposite != null) {
				this.secondRevisionComposite.setEnabled(false);
			}
		}
	}
	
	protected IRepositoryResource getDestination(String url) {
		IRepositoryLocation location = this.baseResource.getRepositoryLocation();
		url = SVNUtility.normalizeURL(url);
		try {
			return this.baseResource instanceof IRepositoryContainer ? (IRepositoryResource)location.asRepositoryContainer(url, true) : location.asRepositoryFile(url, true);
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}
	
	protected void validateRevisions() {
		if ((this.twoRevisions & MODE_TWO) != 0) {
			this.validationManager.validateContent();
		}
	}
	
}
