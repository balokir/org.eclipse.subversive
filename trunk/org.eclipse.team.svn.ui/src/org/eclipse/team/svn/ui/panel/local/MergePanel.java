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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.compare.internal.TabFolderLayout;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNNotification;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.connector.SVNNotification.PerformedAction;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.JavaHLMergeOperation;
import org.eclipse.team.svn.core.operation.remote.LocateResourceURLInHistoryOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.DepthSelectionComposite;
import org.eclipse.team.svn.ui.composite.RepositoryResourceSelectionComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.AbstractAdvancedDialogPanel;
import org.eclipse.team.svn.ui.panel.reporting.PreviewPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.IValidationManager;

/**
 * Merge panel implementation
 * 
 * @author Alexander Gurov
 */
public class MergePanel extends AbstractAdvancedDialogPanel {
	public static final int MODE_1URL = 0;
	public static final int MODE_2URL = 1;
	public static final int MODE_REINTEGRATE = 2;
	
	protected static final String FIRST_URL_HISTORY = "Merge.FirstUrl";
	protected static final String SECOND_URL_HISTORY = "Merge.SecondUrl";
	
	protected IResource []to;
	protected IRepositoryResource baseResource;
	protected long currentRevision;
	protected DepthSelectionComposite depthSelector;
	protected DepthSelectionComposite depthSelectorSimple;

	protected IRepositoryResource firstSelectedResource;
	protected IRepositoryResource secondSelectedResource;
	protected SVNRevisionRange []selectedRevisions;
	
	protected int mode;
	
	protected boolean ignoreAncestry;

	protected RepositoryResourceSelectionComposite simpleSelectionComposite;
	
	protected RepositoryResourceSelectionComposite firstSelectionComposite;
	protected RepositoryResourceSelectionComposite secondSelectionComposite;
	
	protected RepositoryResourceSelectionComposite reintegrateSelectionComposite;
	
	protected Button ignoreAncestryButton;
	protected Button ignoreAncestrySimpleButton;
	
	public MergePanel(IResource []to, IRepositoryResource baseResource, long currentRevision) {
		super(new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, new String[] {SVNTeamUIPlugin.instance().getResource("MergePanel.Preview")});
		
        this.dialogTitle = SVNTeamUIPlugin.instance().getResource("MergePanel.Title");
        this.dialogDescription = SVNTeamUIPlugin.instance().getResource("MergePanel.Description");
        this.defaultMessage = SVNTeamUIPlugin.instance().getResource("MergePanel.Message");
        
        this.to = to;
        this.baseResource = this.firstSelectedResource = this.secondSelectedResource = baseResource;
        this.currentRevision = currentRevision;
	}
	
	public int getMode() {
		return this.mode;
	}
	
    public Point getPrefferedSizeImpl() {
        return new Point(570, 245);
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
		return this.ignoreAncestry;
	}
	
	public SVNRevisionRange []getSelectedRevisions() {
		return this.selectedRevisions;
	}
	
	public void createControlsImpl(Composite parent) {
		((GridLayout)parent.getLayout()).verticalSpacing = 2;
		
		final TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayout(new TabFolderLayout());
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));		
		
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNTeamUIPlugin.instance().getResource("MergePanel.1URL"));
		tabItem.setControl(this.create1URLModeView(tabFolder));
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNTeamUIPlugin.instance().getResource("MergePanel.2URL"));
		tabItem.setControl(this.create2URLModeView(tabFolder));
		
		if (CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() >= ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x) {
			tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(SVNTeamUIPlugin.instance().getResource("MergePanel.Reintegrate"));
			tabItem.setControl(this.createReintegrateModeView(tabFolder));
		}
		
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MergePanel.this.mode = tabFolder.getSelectionIndex();
				MergePanel.this.validateContent();
			}
		});
		
        this.mode = MergePanel.MODE_1URL;
	}
	
	protected Composite create1URLModeView(Composite parent) {
		GridData data = null;
		GridLayout layout = null;
		
		parent = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		parent.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		parent.setLayoutData(data);
		
		int mode = CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() >= ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x ? RepositoryResourceSelectionComposite.MODE_CHECK : RepositoryResourceSelectionComposite.MODE_TWO;
		this.simpleSelectionComposite = new RepositoryResourceSelectionComposite(
				parent, SWT.NONE, new ValidationManagerProxy() {
					protected AbstractVerifier wrapVerifier(AbstractVerifier verifier) {
						return new AbstractVerifierProxy(verifier) {
							protected boolean isVerificationEnabled(Control input) {
								return MergePanel.this.mode == MergePanel.MODE_1URL;
							}
						};
					}
				}, MergePanel.FIRST_URL_HISTORY, this.firstSelectedResource, true, 
				SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Title"), SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Description"), mode, RepositoryResourceSelectionComposite.TEXT_LAST);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.simpleSelectionComposite.setLayoutData(data);
		this.simpleSelectionComposite.setCurrentRevision(this.currentRevision);
		
		Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		separator.setVisible(false);

		data = new GridData();
        this.ignoreAncestrySimpleButton = new Button(parent, SWT.CHECK);
        this.ignoreAncestrySimpleButton.setLayoutData(data);
        this.ignoreAncestrySimpleButton.setText(SVNTeamUIPlugin.instance().getResource("MergePanel.Button.IgnoreAncestry"));
        this.ignoreAncestrySimpleButton.setSelection(this.ignoreAncestry);
		
		this.depthSelectorSimple = new DepthSelectionComposite(parent, SWT.NONE);
		this.depthSelectorSimple.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.depthSelectorSimple.addAndSelectWorkingCopyDepth();
		
		return parent;
	}
	
	protected Composite create2URLModeView(Composite parent) {
		GridData data = null;
		GridLayout layout = null;
		
		parent = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		parent.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		parent.setLayoutData(data);
		
		final ValidationManagerProxy proxy2 = new ValidationManagerProxy() {
			protected AbstractVerifier wrapVerifier(AbstractVerifier verifier) {
				return new AbstractVerifierProxy(verifier) {
					protected boolean isVerificationEnabled(Control input) {
						return MergePanel.this.mode == MergePanel.MODE_2URL;
					}
				};
			}
		};
		
		ValidationManagerProxy proxy = new ValidationManagerProxy() {
			protected AbstractVerifier wrapVerifier(AbstractVerifier verifier) {
				return new AbstractVerifierProxy(verifier) {
					protected boolean isVerificationEnabled(Control input) {
						return MergePanel.this.mode == MergePanel.MODE_2URL;
					}
					
					public boolean verify(Control input) {
						for (Control cmp : proxy2.getControls()) {
							proxy2.validateControl(cmp);
						}
						return super.verify(input);
					}
				};
			}
		};
		
		this.firstSelectionComposite = new RepositoryResourceSelectionComposite(
				parent, SWT.NONE, proxy, MergePanel.FIRST_URL_HISTORY, "MergePanel.SourceURL1", this.firstSelectedResource, true, 
				SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Title"), SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Description"), RepositoryResourceSelectionComposite.MODE_DEFAULT, RepositoryResourceSelectionComposite.TEXT_LAST);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.firstSelectionComposite.setLayoutData(data);
		this.firstSelectionComposite.setCurrentRevision(this.currentRevision);
		
		Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		separator.setVisible(false);
		
		this.secondSelectionComposite = new RepositoryResourceSelectionComposite(
				parent, SWT.NONE, proxy2, MergePanel.SECOND_URL_HISTORY, "MergePanel.SourceURL2", this.secondSelectedResource, true, 
				SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Title"), SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Description"), RepositoryResourceSelectionComposite.MODE_DEFAULT, RepositoryResourceSelectionComposite.TEXT_LAST);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.secondSelectionComposite.setLayoutData(data);
		this.secondSelectionComposite.setCurrentRevision(this.currentRevision);
		
		separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		separator.setVisible(false);

		data = new GridData();
        this.ignoreAncestryButton = new Button(parent, SWT.CHECK);
        this.ignoreAncestryButton.setLayoutData(data);
        this.ignoreAncestryButton.setText(SVNTeamUIPlugin.instance().getResource("MergePanel.Button.IgnoreAncestry"));
        this.ignoreAncestryButton.setSelection(this.ignoreAncestry);
		
		this.depthSelector = new DepthSelectionComposite(parent, SWT.NONE);
		this.depthSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.depthSelector.addAndSelectWorkingCopyDepth();
        
		return parent;
	}
	
	protected Composite createReintegrateModeView(Composite parent) {
		GridData data = null;
		GridLayout layout = null;
		
		parent = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		parent.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		parent.setLayoutData(data);
		
		this.reintegrateSelectionComposite = new RepositoryResourceSelectionComposite(
				parent, SWT.NONE, new ValidationManagerProxy() {
					protected AbstractVerifier wrapVerifier(AbstractVerifier verifier) {
						return new AbstractVerifierProxy(verifier) {
							protected boolean isVerificationEnabled(Control input) {
								return MergePanel.this.mode == MergePanel.MODE_REINTEGRATE;
							}
						};
					}
				}, MergePanel.FIRST_URL_HISTORY, this.firstSelectedResource, true, 
				SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Title"), SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Description"), RepositoryResourceSelectionComposite.MODE_DEFAULT, RepositoryResourceSelectionComposite.TEXT_LAST);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.reintegrateSelectionComposite.setLayoutData(data);
		this.reintegrateSelectionComposite.setCurrentRevision(this.currentRevision);
		
		return parent;
	}
	
	public int getDepth() {
		return this.mode == MergePanel.MODE_1URL ? this.depthSelectorSimple.getDepth() : (this.mode == MergePanel.MODE_2URL ? this.depthSelector.getDepth() : ISVNConnector.Depth.UNKNOWN);
	}
	
	protected void showDetails() {
		this.saveChangesImpl();
		
		LocateResourceURLInHistoryOperation locateFirst = new LocateResourceURLInHistoryOperation(this.getFirstSelection());
		LocateResourceURLInHistoryOperation locateSecond = new LocateResourceURLInHistoryOperation(this.getSecondSelection());
		IRepositoryResourceProvider firstSet = locateFirst;
		IRepositoryResourceProvider secondSet = locateSecond;
		if (this.mode == MergePanel.MODE_1URL) {
			firstSet = new IRepositoryResourceProvider.DefaultRepositoryResourceProvider(this.getFirstSelection());
		}
		JavaHLMergeOperation mergeOp = null;
		if (this.mode == MergePanel.MODE_2URL) {
			mergeOp = new JavaHLMergeOperation(this.to, firstSet, secondSet, true, this.getIgnoreAncestry(), this.getDepth()); 
		}
		else if (this.mode == MergePanel.MODE_1URL) {
			mergeOp = new JavaHLMergeOperation(this.to, firstSet, this.getSelectedRevisions(), true, this.getIgnoreAncestry(), this.getDepth());
		}
		else {
			mergeOp = new JavaHLMergeOperation(this.to, firstSet, true);
		}
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
						if (SVNNotification.PerformedAction.isKnownAction(state.action)) {
							buf.append(PerformedAction.actionNames[state.action]);
						}
						else {
							buf.append("\t");
						}
						buf.append(SVNTeamUIPlugin.instance().getResource("MergePanel.Preview.Default"));
					}
				}
				buf.append(state.path);
				buf.append("\n");
			}
		});
		
		if (this.mode != MergePanel.MODE_1URL) {
			CompositeOperation op = new CompositeOperation(mergeOp.getId());
			op.add(locateFirst);
			if (this.mode != MergePanel.MODE_2URL) {
				op.add(locateSecond);
			}
			op.add(mergeOp);
			UIMonitorUtility.doTaskNowDefault(op, true);
		}
		else {
			UIMonitorUtility.doTaskNowDefault(mergeOp, true);
		}
		
		if (mergeOp.getExecutionState() == IActionOperation.OK) {
			Font font = new Font(UIMonitorUtility.getDisplay(), "Courier New", 8, SWT.NORMAL);
			new DefaultDialog(this.manager.getShell(), new PreviewPanel(SVNTeamUIPlugin.instance().getResource("MergePanel.Preview.Title"), SVNTeamUIPlugin.instance().getResource("MergePanel.Preview.Description"), SVNTeamUIPlugin.instance().getResource("MergePanel.Preview.Message"), buf.toString(), font)).open();
		}
	}
	
	protected void saveChangesImpl() {
    	if (this.mode == MergePanel.MODE_1URL) {
        	this.firstSelectedResource = this.simpleSelectionComposite.getSelectedResource();
    		this.secondSelectedResource = this.simpleSelectionComposite.getSecondSelectedResource();
    		this.selectedRevisions = this.simpleSelectionComposite.getSelectedRevisions();
        	this.simpleSelectionComposite.saveHistory();
        	
        	this.ignoreAncestry = this.ignoreAncestrySimpleButton.getSelection();
    	}
    	else if (this.mode == MergePanel.MODE_2URL) {
        	this.firstSelectedResource = this.firstSelectionComposite.getSelectedResource();
        	this.firstSelectionComposite.saveHistory();
        	
        	this.secondSelectedResource = this.secondSelectionComposite.getSelectedResource();
        	this.secondSelectionComposite.saveHistory();
        	
        	this.ignoreAncestry = this.ignoreAncestryButton.getSelection();
    	}
    	else {
        	this.firstSelectedResource = this.secondSelectedResource = this.reintegrateSelectionComposite.getSelectedResource();
        	this.reintegrateSelectionComposite.saveHistory();
    	}
	}

	protected void cancelChangesImpl() {
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
		protected Set<Control> controls = new HashSet<Control>();
		
		public void attachTo(Control cmp, AbstractVerifier verifier) {
			this.controls.add(cmp);
			MergePanel.this.attachTo(cmp, this.wrapVerifier(verifier));
		}
		
		public void detachFrom(Control cmp) {
			this.controls.remove(cmp);
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
		
		public boolean validateControl(Control cmp) {
			return MergePanel.this.validateControl(cmp);
		}
		
		public Set<Control> getControls() {
			return this.controls;
		}
		
		protected abstract AbstractVerifier wrapVerifier(AbstractVerifier verifier);
		
	}
	
    public String getHelpId() {
    	return "org.eclipse.team.svn.help.mergeDialogContext";
    }

}
