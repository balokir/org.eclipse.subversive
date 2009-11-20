/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.core.connector.SVNEntryReference;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
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
 * 
 * Repository resource selection composite
 * 
 * In contrast to RepositoryResourceSelectionComposite it 
 * doesn't contain any revision controls
 * 
 * @author Igor Burilo
 *
 */
public class RepositoryResourceOnlySelectionComposite extends Composite {
	
	protected Label urlLabel;
	protected Combo urlText;
	protected Button browse;
	protected UserInputHistory urlHistory;
	protected IValidationManager validationManager;
	protected IRepositoryResource baseResource;
	
	protected String url;

	protected CompositeVerifier verifier;
	
	protected String selectionTitle;
	protected String selectionDescription;
	protected String comboId;
	
	protected boolean foldersOnly;
	
	/*
	 * Flag which determines whether to verify that selected
	 * resource starts with base resource.
	 * This can be used if we want that selected resource only starts
	 * with base resource but not with repository root corresponding to base resource
	 */
	protected boolean isMatchToBaseResource;

	public RepositoryResourceOnlySelectionComposite(Composite parent, int style, IValidationManager validationManager, String historyKey, IRepositoryResource baseResource, String selectionTitle, String selectionDescription) {
		this(parent, style, validationManager, historyKey, "RepositoryResourceOnlySelectionComposite_URL", baseResource, selectionTitle, selectionDescription); //$NON-NLS-1$
	}
	
	public RepositoryResourceOnlySelectionComposite(Composite parent, int style, IValidationManager validationManager, String historyKey, String comboId, IRepositoryResource baseResource, String selectionTitle, String selectionDescription) {
		super(parent, style);
		this.urlHistory = new UserInputHistory(historyKey);
		this.validationManager = validationManager;
		this.baseResource = baseResource;
		this.selectionTitle = selectionTitle;
		this.selectionDescription = selectionDescription;
		this.comboId = comboId;
		this.foldersOnly = !(baseResource instanceof IRepositoryFile);
		
		this.createControls();
	}
	
	public void setMatchToBaseResource(boolean isMatchToBaseResource) {
		this.isMatchToBaseResource = isMatchToBaseResource;
	}
	
	public boolean isMatchToBaseResource() {
		return this.isMatchToBaseResource;
	}
	
	public void setBaseResource(IRepositoryResource baseResource) {
		this.baseResource = baseResource;
		this.urlText.setText(this.baseResource.getUrl());
	}
	
	public void setFoldersOnly(boolean foldersOnly) {
		this.foldersOnly = foldersOnly;
	}

	public boolean isSelectionAvailable() {
		return this.getDestination(SVNUtility.asEntryReference(this.url), true) != null;
	}
	
	public IRepositoryResource getSelectedResource() {
		IRepositoryResource resource = this.getDestination(SVNUtility.asEntryReference(this.url), false);
		return resource;
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
		this.urlLabel.setEnabled(enabled);
		this.urlText.setEnabled(enabled);
		this.browse.setEnabled(enabled);
	}
	
	private void createControls() {
		GridLayout layout = null;
		GridData data = null;
		
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = layout.marginWidth = 0;
		this.setLayout(layout);
		
		this. urlLabel = new Label(this, SWT.NONE);
		urlLabel.setLayoutData(new GridData());
		urlLabel.setText(SVNUIMessages.getString(this.comboId));
		
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
		
		if (this.baseResource != null) {
			this.urlText.setText(this.baseResource.getUrl());
		}
		
		this.url = this.urlText.getText();
		this.urlText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				RepositoryResourceOnlySelectionComposite.this.url = ((Combo)e.widget).getText();
			}
		});
		this.verifier = new CompositeVerifier();
		this.verifier.add(new NonEmptyFieldVerifier(SVNUIMessages.getString(this.comboId + "_Verifier"))); //$NON-NLS-1$
		this.verifier.add(new URLVerifier(SVNUIMessages.getString(this.comboId + "_Verifier")) { //$NON-NLS-1$
			protected String getErrorMessage(Control input) {
				String error = super.getErrorMessage(input);
				if (RepositoryResourceOnlySelectionComposite.this.baseResource != null && error == null) {
					String url = this.getText(input);
					if (RepositoryResourceOnlySelectionComposite.this.getDestination(SVNUtility.asEntryReference(url), true) == null) {
						error = SVNUIMessages.format(SVNUIMessages.RepositoryResourceOnlySelectionComposite_URL_Verifier_Error, new String[] {url, RepositoryResourceOnlySelectionComposite.this.baseResource.getRepositoryLocation().getUrl()});
					}
					
					//check that resource starts with location
					if (error == null && RepositoryResourceOnlySelectionComposite.this.isMatchToBaseResource) {
						String baseResourceUrl = RepositoryResourceOnlySelectionComposite.this.baseResource.getUrl();
						if (!url.startsWith(baseResourceUrl)) {							
							error = SVNUIMessages.format(SVNUIMessages.RepositoryResourceOnlySelectionComposite_URL_Verifier_Error, new String[] {url, baseResourceUrl});
						}
					}
				}
				return error;
			}
		});
		this.verifier.add(new AbsolutePathVerifier(this.comboId));
		this.validationManager.attachTo(this.urlText, this.verifier);
		
		this.browse = new Button(select, SWT.PUSH);
		this.browse.setText(SVNUIMessages.Button_Browse);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(this.browse);
		this.browse.setLayoutData(data);
		this.browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RepositoryTreePanel panel = 
			        new RepositoryTreePanel(
			        	SVNUIMessages.RepositoryResourceOnlySelectionComposite_Select_Title, 
						RepositoryResourceOnlySelectionComposite.this.selectionTitle,
						RepositoryResourceOnlySelectionComposite.this.selectionDescription,
						null, 
						true, 
						RepositoryResourceOnlySelectionComposite.this.baseResource, false);
				panel.setAllowFiles(!RepositoryResourceOnlySelectionComposite.this.foldersOnly);
				DefaultDialog browser = new DefaultDialog(RepositoryResourceOnlySelectionComposite.this.getShell(), panel);
				if (browser.open() == 0) {
					IRepositoryResource selectedResource = panel.getSelectedResource();
					boolean samePeg = RepositoryResourceOnlySelectionComposite.this.baseResource != null && selectedResource.getPegRevision().equals(RepositoryResourceOnlySelectionComposite.this.baseResource.getPegRevision());
					RepositoryResourceOnlySelectionComposite.this.urlText.setText(samePeg ? selectedResource.getUrl() : SVNUtility.getEntryReference(selectedResource).toString());
					RepositoryResourceOnlySelectionComposite.this.validationManager.validateContent();
				}
			}
		});		
	}
	
	protected IRepositoryResource getDestination(SVNEntryReference ref, boolean allowsNull) {
		if (ref == null) {
			if (this.baseResource == null) {
				if (allowsNull) {
					return null;
				}
				throw new IllegalArgumentException("SVN entry reference cannot be null.");
			}
			return SVNUtility.copyOf(this.baseResource);
		}
		String url = SVNUtility.normalizeURL(ref.path);
		try {
			IRepositoryResource base = this.baseResource;
			IRepositoryResource resource = null;
			if (base != null) {
				resource = this.foldersOnly ? (IRepositoryResource)this.baseResource.asRepositoryContainer(url, false) : this.baseResource.asRepositoryFile(url, false);
			}
			else {
				SVNUtility.getSVNUrl(url);	// validate an URL
				resource = SVNUtility.asRepositoryResource(url, this.foldersOnly);
			}
			if (ref.pegRevision != null) {
				resource.setPegRevision(ref.pegRevision);
			}
			return resource;
		}
		catch (Exception ex) {
			if (allowsNull) {
				return null;
			}
			if (this.baseResource == null) {
				throw new IllegalArgumentException("SVN entry reference must contain a valid value.");
			}
			return SVNUtility.copyOf(this.baseResource);
		}
	}
}
