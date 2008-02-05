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

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.core.connector.ISVNProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNNotification.PerformedAction;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.JavaHLMergeOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.RepositoryResourceSelectionComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.AbstractAdvancedDialogPanel;
import org.eclipse.team.svn.ui.panel.IDialogManagerEx;
import org.eclipse.team.svn.ui.panel.reporting.PreviewPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.IValidationManager;

/**
 * JavaHL-mode merge panel
 * 
 * @author Alexander Gurov
 */
public class MergePanel extends AbstractAdvancedDialogPanel {
	protected static final String FIRST_URL_HISTORY = "Merge.FirstUrl";
	protected static final String SECOND_URL_HISTORY = "Merge.SecondUrl";
	
	protected IResource []to;
	protected IRepositoryResource baseResource;
	protected long currentRevision;

	protected IRepositoryResource firstSelectedResource;
	protected IRepositoryResource secondSelectedResource;
	
	protected boolean advancedMode;
	
	protected boolean ignoreAncestry;

	protected RepositoryResourceSelectionComposite simpleSelectionComposite;
	
	protected RepositoryResourceSelectionComposite firstSelectionComposite;
	protected RepositoryResourceSelectionComposite secondSelectionComposite;
	
	protected Composite simpleView;
	protected Composite advancedView;
	protected Button ignoreAncestryButton;
	
	public MergePanel(IResource []to, IRepositoryResource baseResource, long currentRevision) {
		super(new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, new String[] {SVNTeamUIPlugin.instance().getResource("Button.Advanced"), SVNTeamUIPlugin.instance().getResource("MergePanel.Preview")});
		
        this.dialogTitle = SVNTeamUIPlugin.instance().getResource("MergePanel.Title");
        this.dialogDescription = SVNTeamUIPlugin.instance().getResource("MergePanel.Description");
        this.defaultMessage = SVNTeamUIPlugin.instance().getResource("MergePanel.Message");
        
        this.to = to;
        this.baseResource = this.firstSelectedResource = this.secondSelectedResource = baseResource;
        this.currentRevision = currentRevision;
	}
	
    public Point getPrefferedSizeImpl() {
        return new Point(550, 245);
    }
    
    public SVNRevision getStartRevision() {
    	return this.simpleSelectionComposite.getStartRevision();
    }
    
    public IRepositoryResource getSelectedResource() {
    	IRepositoryResource retVal = SVNUtility.copyOf(this.simpleSelectionComposite.getSelectedResource());
    	retVal.setSelectedRevision(this.simpleSelectionComposite.getSecondSelectedRevision());
		return retVal;
    }
    
	public IRepositoryResource []getSelection() {
		return this.getSelection(this.getSelectedResource());
	}

	public IRepositoryResource []getFirstSelection() {
		return this.getSelection(this.firstSelectedResource);
	}

	public IRepositoryResource []getSecondSelection() {
		return this.getSelection(this.secondSelectedResource);
	}
	
	public boolean getIgnoreAncestry() {
		return this.advancedMode ? this.ignoreAncestry : false;
	}
	
	public void createControlsImpl(Composite parent) {
		((GridLayout)parent.getLayout()).verticalSpacing = 0;
		
		this.simpleView = this.createSimpleModeView(parent);
        this.advancedView = this.createAdvancedModeView(parent);
        
        this.setMode(false);
	}
	
	protected Composite createSimpleModeView(Composite parent) {
		GridData data = null;
		
		this.simpleSelectionComposite = new RepositoryResourceSelectionComposite(
				parent, SWT.NONE, new ValidationManagerProxy() {
					protected AbstractVerifier wrapVerifier(AbstractVerifier verifier) {
						return new AbstractVerifierProxy(verifier) {
							protected boolean isVerificationEnabled(Control input) {
								return !MergePanel.this.advancedMode;
							}
						};
					}
				}, MergePanel.FIRST_URL_HISTORY, this.firstSelectedResource, true, 
				SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Title"), SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Description"), RepositoryResourceSelectionComposite.MODE_TWO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.simpleSelectionComposite.setLayoutData(data);
		this.simpleSelectionComposite.setCurrentRevision(this.currentRevision);
		
		return this.simpleSelectionComposite;
	}
	
	protected Composite createAdvancedModeView(Composite parent) {
		GridData data = null;
		GridLayout layout = null;
		
		parent = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		parent.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		parent.setLayoutData(data);
		
		ValidationManagerProxy proxy = new ValidationManagerProxy() {
			protected AbstractVerifier wrapVerifier(AbstractVerifier verifier) {
				return new AbstractVerifierProxy(verifier) {
					protected boolean isVerificationEnabled(Control input) {
						return MergePanel.this.advancedMode;
					}
				};
			}
		};
		
		this.firstSelectionComposite = new RepositoryResourceSelectionComposite(
				parent, SWT.NONE, proxy, MergePanel.FIRST_URL_HISTORY, "MergePanel.SourceURL1", this.firstSelectedResource, true, 
				SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Title"), SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Description"), RepositoryResourceSelectionComposite.MODE_DEFAULT);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.firstSelectionComposite.setLayoutData(data);
		this.firstSelectionComposite.setCurrentRevision(this.currentRevision);
		
		Label strut = new Label(parent, SWT.NONE);
		data = new GridData();
		data.heightHint = 12;
		strut.setLayoutData(data);
		
		this.secondSelectionComposite = new RepositoryResourceSelectionComposite(
				parent, SWT.NONE, proxy, MergePanel.SECOND_URL_HISTORY, "MergePanel.SourceURL2", this.secondSelectedResource, true, 
				SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Title"), SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Description"), RepositoryResourceSelectionComposite.MODE_DEFAULT);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.secondSelectionComposite.setLayoutData(data);
		this.secondSelectionComposite.setCurrentRevision(this.currentRevision);

		strut = new Label(parent, SWT.NONE);
		data = new GridData();
		data.heightHint = 7;
		strut.setLayoutData(data);

		data = new GridData();
        this.ignoreAncestryButton = new Button(parent, SWT.CHECK);
        this.ignoreAncestryButton.setLayoutData(data);
        this.ignoreAncestryButton.setText(SVNTeamUIPlugin.instance().getResource("MergePanel.Button.IgnoreAncestry"));
        this.ignoreAncestryButton.setSelection(this.ignoreAncestry);
        
		return parent;
	}
	
	public void extendedButtonPressed(int idx) {
		super.extendedButtonPressed(idx);
		if (idx == 1) {
			this.saveChangesImpl();
			JavaHLMergeOperation mergeOp = new JavaHLMergeOperation(this.to, this.getFirstSelection(), this.getSecondSelection(), true, this.getIgnoreAncestry());
			final StringBuffer buf = new StringBuffer();
			buf.append(SVNTeamUIPlugin.instance().getResource("MergePanel.Preview.Header.Text"));
			buf.append(SVNTeamUIPlugin.instance().getResource("MergePanel.Preview.Header.Line"));
			mergeOp.setExternalMonitor(new ISVNProgressMonitor() {
				public boolean isActivityCancelled() {
					return false;
				}
				public void progress(int current, int total, ItemState state) {
					buf.append("<b>");
					switch (state.action) {
					case PerformedAction.UPDATE_ADD: {
						buf.append(SVNTeamUIPlugin.instance().getResource("MergePanel.Preview.Added"));
						break;
					}
					case PerformedAction.UPDATE_DELETE: {
						buf.append(SVNTeamUIPlugin.instance().getResource("MergePanel.Preview.Deleted"));
						break;
					}
					case PerformedAction.UPDATE_UPDATE: {
						buf.append(SVNTeamUIPlugin.instance().getResource("MergePanel.Preview.Modified"));
						break;
					}
					default: {
						buf.append(PerformedAction.actionNames[state.action]);
						buf.append(SVNTeamUIPlugin.instance().getResource("MergePanel.Preview.Default"));
					}
					}
					buf.append(state.path);
					buf.append("\n");
				}
			});
			
			UIMonitorUtility.doTaskNowDefault(mergeOp, true);
			
			if (mergeOp.getExecutionState() == IActionOperation.OK) {
				Font font = new Font(UIMonitorUtility.getDisplay(), "Courier New", 8, SWT.NORMAL);
				new DefaultDialog(this.manager.getShell(), new PreviewPanel(SVNTeamUIPlugin.instance().getResource("MergePanel.Preview.Title"), SVNTeamUIPlugin.instance().getResource("MergePanel.Preview.Description"), SVNTeamUIPlugin.instance().getResource("MergePanel.Preview.Message"), buf.toString(), font)).open();
			}
		}
	}
	
	protected void showDetails() {
		this.setMode(!this.advancedMode);
	}
	
	protected void setMode(boolean advanced) {
		this.saveChangesImpl();
		
		if (this.advancedMode = advanced) {
			((GridData)this.simpleView.getLayoutData()).heightHint = 0;
			((GridData)this.advancedView.getLayoutData()).heightHint = SWT.DEFAULT;
			
			this.simpleView.setVisible(false);
			this.advancedView.setVisible(true);
			this.ignoreAncestryButton.setSelection(this.ignoreAncestry);
			
			this.firstSelectionComposite.setUrl(this.simpleSelectionComposite.getUrl());
		}
		else {
			((GridData)this.advancedView.getLayoutData()).heightHint = 0;
			((GridData)this.simpleView.getLayoutData()).heightHint = SWT.DEFAULT;
			
			this.simpleView.setVisible(true);
			this.advancedView.setVisible(false);
			
			this.simpleSelectionComposite.setUrl(this.firstSelectionComposite.getUrl());
		}
		this.simpleView.getParent().layout();
		if (this.manager != null) {
			((IDialogManagerEx)this.manager).setExtendedButtonCaption(0, this.advancedMode ? SVNTeamUIPlugin.instance().getResource("Button.Simple") : SVNTeamUIPlugin.instance().getResource("Button.Advanced"));
			this.validateContent();
		}
	}
	
	protected void saveChangesImpl() {
    	if (!this.advancedMode) {
        	this.firstSelectedResource = this.simpleSelectionComposite.getSelectedResource();
    		this.secondSelectedResource = this.simpleSelectionComposite.getSecondSelectedResource();
        	this.simpleSelectionComposite.saveHistory();
    	}
    	else {
        	this.firstSelectedResource = this.firstSelectionComposite.getSelectedResource();
        	this.firstSelectionComposite.saveHistory();
        	
        	this.secondSelectedResource = this.secondSelectionComposite.getSelectedResource();
        	this.secondSelectionComposite.saveHistory();
    	}
    	this.ignoreAncestry = this.ignoreAncestryButton.getSelection();
	}

	protected void cancelChangesImpl() {
	}

	protected void setButtonsEnabled(boolean enabled) {
	    ((IDialogManagerEx)this.manager).setExtendedButtonEnabled(1, SVNTeamPreferences.getMergeBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MERGE_USE_JAVAHL_NAME));
	}
	
	protected IRepositoryResource []getSelection(IRepositoryResource base) {
		if (this.to.length == 1) {
			return new IRepositoryResource[] {base};
		}
		IRepositoryResource []retVal = new IRepositoryResource[this.to.length];
		String baseUrl = base.getUrl();
		for (int i = 0; i < retVal.length; i++) {
			String url = baseUrl + "/" + SVNRemoteStorage.instance().asRepositoryResource(this.to[i]).getName();
			retVal[i] = this.to[i].getType() == IResource.FILE ? (IRepositoryResource)base.asRepositoryFile(url, false) : base.asRepositoryContainer(url, false);
		}
		return retVal;
	}

	protected abstract class ValidationManagerProxy implements IValidationManager {
		public void attachTo(Control cmp, AbstractVerifier verifier) {
			MergePanel.this.attachTo(cmp, this.wrapVerifier(verifier));
		}
		
		public void detachFrom(Control cmp) {
			MergePanel.this.detachFrom(cmp);
		}

		public void detachAll() {
			MergePanel.this.detachAll();
		}

		public boolean isFilledRight() {
			return MergePanel.this.isFilledRight();
		}

		public void validateContent() {
			MergePanel.this.validateContent();
		}
		
		protected abstract AbstractVerifier wrapVerifier(AbstractVerifier verifier);
		
	}
	
    public String getHelpId() {
    	return "org.eclipse.team.svn.help.mergeDialogContext";
    }

}
