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
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
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
 * Repository resource selection composite
 * 
 * @author Alexander Gurov
 */
public class RepositoryResourceSelectionComposite extends Composite {
	public static final int MODE_DEFAULT = 0;
	public static final int MODE_TWO = 1;
	public static final int MODE_CHECK = 2;
	
	public static final int TEXT_NONE = 0;
	public static final int TEXT_BASE = 1;
	public static final int TEXT_LAST = 2;
	
	protected Combo urlText;
	protected Button browse;
	protected UserInputHistory urlHistory;
	protected RevisionComposite revisionComposite;
	protected RevisionComposite secondRevisionComposite;
	protected IValidationManager validationManager;
	protected IRepositoryResource baseResource;
	protected boolean stopOnCopy;
	protected boolean toFilterCurrent;
	protected int mode;
	
	protected String url;

	protected CompositeVerifier verifier;
	
	protected String selectionTitle;
	protected String selectionDescription;
	protected String comboId;
	
	protected boolean foldersOnly;
	
	protected int defaultTextType;

	public RepositoryResourceSelectionComposite(Composite parent, int style, IValidationManager validationManager, String historyKey, IRepositoryResource baseResource, boolean stopOnCopy, String selectionTitle, String selectionDescription, int mode, int defaultTextType) {
		this(parent, style, validationManager, historyKey, "RepositoryResourceSelectionComposite_URL", baseResource, stopOnCopy, selectionTitle, selectionDescription, mode, defaultTextType); //$NON-NLS-1$
	}
	
	public RepositoryResourceSelectionComposite(Composite parent, int style, IValidationManager validationManager, String historyKey, String comboId, IRepositoryResource baseResource, boolean stopOnCopy, String selectionTitle, String selectionDescription, int mode, int defaultTextType) {
		super(parent, style);
		this.stopOnCopy = stopOnCopy;
		this.toFilterCurrent = false;
		this.urlHistory = new UserInputHistory(historyKey);
		this.validationManager = validationManager;
		this.baseResource = baseResource;
		this.selectionTitle = selectionTitle;
		this.selectionDescription = selectionDescription;
		this.mode = mode;
		this.comboId = comboId;
		this.foldersOnly = !(baseResource instanceof IRepositoryFile);
		this.defaultTextType = defaultTextType;
		
		this.createControls(defaultTextType);
	}
	
	public void setBaseResource(IRepositoryResource baseResource) {
		this.baseResource = baseResource;
		if (this.revisionComposite != null) {
			this.revisionComposite.setBaseResource(this.baseResource);
		}
		if (this.secondRevisionComposite != null) {
			this.secondRevisionComposite.setBaseResource(this.baseResource);
		}
		if (this.defaultTextType == RepositoryResourceSelectionComposite.TEXT_BASE && this.baseResource != null) {
			this.urlText.setText(this.baseResource.getUrl());
		}
	}
	
	public void setFoldersOnly(boolean foldersOnly) {
		this.foldersOnly = foldersOnly;
	}
	
	public void setFilterCurrent(boolean toFilter) {
		this.toFilterCurrent = toFilter;
		this.revisionComposite.setFilterCurrent(this.toFilterCurrent);
	}

	public boolean isSelectionAvailable() {
		return this.getDestination(SVNUtility.asEntryReference(this.url), true) != null;
	}
	
	public boolean isReverseRevisions() {
		return this.revisionComposite.isReverseRevisions();
	}
	
	public boolean isReverseSecondResourceRevisions() {
		return this.secondRevisionComposite != null ? this.secondRevisionComposite.isReverseRevisions() : false; 
	}	
	
	public IRepositoryResource getSelectedResource() {
		IRepositoryResource resource = this.getDestination(SVNUtility.asEntryReference(this.url), false);
		resource.setSelectedRevision(this.revisionComposite.getSelectedRevision());
		return resource;
	}
	
	public IRepositoryResource getSecondSelectedResource() {
		if (this.secondRevisionComposite == null) {
			return null;
		}
		IRepositoryResource resource = this.getDestination(SVNUtility.asEntryReference(this.url), false);
		resource.setSelectedRevision(this.secondRevisionComposite.getSelectedRevision());
		return resource;
	}
	
	public SVNRevisionRange []getSelectedRevisions() {
		if (this.mode == MODE_CHECK) {
			return this.revisionComposite.getSelectedRevisions();
		}
		SVNRevision first = this.getSelectedResource().getSelectedRevision();
		SVNRevision second = this.getSecondSelectedRevision();
		return new SVNRevisionRange[] {new SVNRevisionRange(first, second == null ? first : second)};
	}
	
	public SVNRevision getStartRevision() {
		return this.revisionComposite.getSelectedRevision();
	}
	
	public SVNRevision getSecondSelectedRevision() {
		if (this.secondRevisionComposite == null) {
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
	
	private void createControls(int defaultTextType) {
		GridLayout layout = null;
		GridData data = null;
		
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = layout.marginWidth = 0;
		this.setLayout(layout);
		
		Label urlLabel = new Label(this, SWT.NONE);
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
		if (defaultTextType == RepositoryResourceSelectionComposite.TEXT_BASE && this.baseResource != null) {
			this.urlText.setText(this.baseResource.getUrl());
		}
		else if (defaultTextType == RepositoryResourceSelectionComposite.TEXT_LAST && this.urlText.getItemCount() > 0) {
			this.urlText.select(0);
		}
		this.url = this.urlText.getText();
		this.urlText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				RepositoryResourceSelectionComposite.this.url = ((Combo)e.widget).getText();
				if (RepositoryResourceSelectionComposite.this.isSelectionAvailable()) {
					RepositoryResourceSelectionComposite.this.revisionComposite.setSelectedResource(RepositoryResourceSelectionComposite.this.getSelectedResource());
					boolean toFilter = RepositoryResourceSelectionComposite.this.toFilterCurrent 
										&& RepositoryResourceSelectionComposite.this.baseResource != null && 
										(RepositoryResourceSelectionComposite.this.getSelectedResource().getUrl().equals(RepositoryResourceSelectionComposite.this.baseResource.getUrl())
										|| RepositoryResourceSelectionComposite.this.getSelectedResource().getUrl().equals(RepositoryResourceSelectionComposite.this.baseResource.getUrl() + "/")); //$NON-NLS-1$
					RepositoryResourceSelectionComposite.this.revisionComposite.setFilterCurrent(toFilter);
					if (RepositoryResourceSelectionComposite.this.secondRevisionComposite != null) {
						RepositoryResourceSelectionComposite.this.secondRevisionComposite.setSelectedResource(RepositoryResourceSelectionComposite.this.getSecondSelectedResource());
						RepositoryResourceSelectionComposite.this.secondRevisionComposite.setFilterCurrent(toFilter);
					}
				}
			}
		});
		this.verifier = new CompositeVerifier() {
			protected void fireError(String errorReason) {
				RepositoryResourceSelectionComposite.this.revisionComposite.setEnabled(false);
				if (RepositoryResourceSelectionComposite.this.secondRevisionComposite != null) {
					RepositoryResourceSelectionComposite.this.secondRevisionComposite.setEnabled(false);
				}
				super.fireError(errorReason);
			}
			
			protected void fireOk() {
				RepositoryResourceSelectionComposite.this.revisionComposite.setEnabled(true);
				if (RepositoryResourceSelectionComposite.this.secondRevisionComposite != null) {
					RepositoryResourceSelectionComposite.this.secondRevisionComposite.setEnabled(true);
				}
				super.fireOk();
			}
		};
		this.verifier.add(new NonEmptyFieldVerifier(SVNUIMessages.getString(this.comboId + "_Verifier"))); //$NON-NLS-1$
		this.verifier.add(new URLVerifier(SVNUIMessages.getString(this.comboId + "_Verifier")) { //$NON-NLS-1$
			protected String getErrorMessage(Control input) {
				String error = super.getErrorMessage(input);
				if (RepositoryResourceSelectionComposite.this.baseResource != null && error == null) {
					String url = this.getText(input);
					if (RepositoryResourceSelectionComposite.this.getDestination(SVNUtility.asEntryReference(url), true) == null) {
						error = SVNUIMessages.format(SVNUIMessages.RepositoryResourceSelectionComposite_URL_Verifier_Error, new String[] {url, RepositoryResourceSelectionComposite.this.baseResource.getRepositoryLocation().getUrl()});
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
			        	SVNUIMessages.RepositoryResourceSelectionComposite_Select_Title, 
						RepositoryResourceSelectionComposite.this.selectionTitle,
						RepositoryResourceSelectionComposite.this.selectionDescription,
						RepositoryResourceSelectionComposite.this.baseResource == null ? new IRepositoryResource[0] : new IRepositoryResource[] {RepositoryResourceSelectionComposite.this.getSelectedResource()}, 
						true, true);
				panel.setAllowFiles(!RepositoryResourceSelectionComposite.this.foldersOnly);
				DefaultDialog browser = new DefaultDialog(RepositoryResourceSelectionComposite.this.getShell(), panel);
				if (browser.open() == 0) {
					IRepositoryResource selectedResource = panel.getSelectedResource();
					boolean samePeg = RepositoryResourceSelectionComposite.this.baseResource != null && selectedResource.getPegRevision().equals(RepositoryResourceSelectionComposite.this.baseResource.getPegRevision());
					RepositoryResourceSelectionComposite.this.urlText.setText(samePeg ? selectedResource.getUrl() : SVNUtility.getEntryReference(selectedResource).toString());
					RepositoryResourceSelectionComposite.this.revisionComposite.setSelectedResource(selectedResource);
					if (RepositoryResourceSelectionComposite.this.secondRevisionComposite != null) {
						RepositoryResourceSelectionComposite.this.secondRevisionComposite.setSelectedResource(selectedResource);
					}
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
		String revTitle = SVNUIMessages.RevisionComposite_Revision;
		if (this.mode == MODE_TWO) {
			revTitle = SVNUIMessages.RepositoryResourceSelectionComposite_StartRevision;
		}
		else if (this.mode == MODE_CHECK) {
			revTitle = SVNUIMessages.RevisionComposite_Revisions;
		}
		String revHeadName = this.mode == MODE_CHECK ? SVNUIMessages.RevisionComposite_All : SVNUIMessages.RevisionComposite_HeadRevision;
		this.revisionComposite = new RevisionComposite(revisions, this.validationManager, this.stopOnCopy, new String[] {revTitle, revHeadName}, SVNRevision.HEAD, this.mode == MODE_CHECK) {
			public void additionalValidation() {
				RepositoryResourceSelectionComposite.this.validateRevisions();
			}
		};
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = this.mode == MODE_TWO ? 1 : 2;
		this.revisionComposite.setLayoutData(data);
		this.revisionComposite.setBaseResource(this.baseResource);
		if (this.baseResource != null) {
			this.revisionComposite.setSelectedResource(this.getSelectedResource());	
		}
		if (this.mode == MODE_TWO) {
			this.secondRevisionComposite = new RevisionComposite(revisions, this.validationManager, this.stopOnCopy, new String[] {SVNUIMessages.RepositoryResourceSelectionComposite_StopRevision, SVNUIMessages.RepositoryResourceSelectionComposite_HeadRevision}, SVNRevision.HEAD, false) {
				public void additionalValidation() {
					RepositoryResourceSelectionComposite.this.validateRevisions();
				}
			};
			data = new GridData(GridData.FILL_HORIZONTAL);
			this.secondRevisionComposite.setLayoutData(data);
			this.secondRevisionComposite.setBaseResource(this.baseResource);
			this.secondRevisionComposite.setSelectedResource(this.getSelectedResource());
		}
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
	
	protected void validateRevisions() {
		if ((this.mode & MODE_TWO) != 0) {
			this.validationManager.validateContent();
		}
	}
	
}
