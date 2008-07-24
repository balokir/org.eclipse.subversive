/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.composite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.connector.ISVNConnector.Options;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.history.filter.RevisionLogEntryFilter;
import org.eclipse.team.svn.ui.panel.common.SelectRevisionPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.IntegerFieldVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.RevisionRangesVerifier;

/**
 * Allows to select a resource revision
 * 
 * @author Sergiy Logvin
 */
public class RevisionComposite extends Composite {
	protected IRepositoryResource selectedResource;
	protected IRepositoryResource baseResource;
	protected SVNRevision defaultRevision;
	protected long currentRevision;
	protected long lastSelectedRevision;
	protected boolean stopOnCopy;
	protected boolean toFilterCurrent;
	protected boolean startFromCopy;
	protected String []captions;

	protected SVNRevision selectedRevision;
	
	protected Text revisionField;
	protected Button headRevisionRadioButton;
	protected Button startFromCopyRadioButton;
	protected Button changeRevisionRadioButton;
	protected Button changeRevisionButton;
	protected Button reverseRevisionsButton;
	protected boolean reverseRevisions;
	
	protected IValidationManager validationManager;
	
	protected boolean checkStyled;
	protected SVNRevisionRange []revisions;
	
	public RevisionComposite(Composite parent, IValidationManager validationManager, boolean stopOnCopy, String []captions, SVNRevision defaultRevision, boolean checkStyled) {
		super(parent, SWT.NONE);
		this.stopOnCopy = stopOnCopy;
		this.toFilterCurrent = false;
		this.validationManager = validationManager;
		this.lastSelectedRevision = SVNRevision.INVALID_REVISION_NUMBER;
		this.captions = captions;
		this.defaultRevision = defaultRevision;
		this.checkStyled = checkStyled;
		this.createControls();
	}
	
	public void setBaseResource(IRepositoryResource baseResource) {
		this.baseResource = baseResource;
	}
	
	public void setFilterCurrent(boolean toFilter) {
		this.toFilterCurrent = toFilter;
	}
	
	public SVNRevision getSelectedRevision() {
		return this.selectedRevision;
	}
	
	public SVNRevisionRange []getSelectedRevisions() {
		// check for unspecified
		if (this.revisions[0].from.getKind() == SVNRevision.Kind.START) {
			UIMonitorUtility.doTaskNowDefault(new AbstractActionOperation("Operation.DetectStartRevision") {
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					ISVNConnector proxy = RevisionComposite.this.selectedResource.getRepositoryLocation().acquireSVNProxy();
					try {
						SVNLogEntry []msgs = SVNUtility.logEntries(proxy, SVNUtility.getEntryReference(RevisionComposite.this.selectedResource), SVNRevision.fromNumber(0), RevisionComposite.this.selectedResource.getSelectedRevision(), Options.DISCOVER_PATHS | (RevisionComposite.this.startFromCopy ? Options.STOP_ON_COPY : Options.NONE), ISVNConnector.EMPTY_LOG_ENTRY_PROPS, 1, new SVNProgressMonitor(this, monitor, null));
						if (msgs.length > 0) {
							RevisionComposite.this.revisions[0] = new SVNRevisionRange(SVNRevision.fromNumber(RevisionComposite.this.startFromCopy & msgs[0].revision > 1 ? msgs[0].revision - 1 : msgs[0].revision), RevisionComposite.this.revisions[0].to);
						}
					}
					finally {
						RevisionComposite.this.selectedResource.getRepositoryLocation().releaseSVNProxy(proxy);
					}
				}
			}, true);
		}
		// align range bounds
		for (int i = 0; i < this.revisions.length; i++) {
			if (this.reverseRevisions ^ 
				(this.revisions[i].from.getKind() == SVNRevision.Kind.HEAD || 
				this.revisions[i].from.getKind() == SVNRevision.Kind.NUMBER && this.revisions[i].to.getKind() == SVNRevision.Kind.NUMBER && ((SVNRevision.Number)this.revisions[i].from).getNumber() > ((SVNRevision.Number)this.revisions[i].to).getNumber())) {
				this.revisions[i] = new SVNRevisionRange(this.revisions[i].to, this.revisions[i].from);
			}
		}
		//reorder revisions
		Arrays.sort(this.revisions, new Comparator<SVNRevisionRange>() {
			public int compare(SVNRevisionRange o1, SVNRevisionRange o2) {
				long rev1 = ((SVNRevision.Number)o1.from).getNumber();
				long rev2 = ((SVNRevision.Number)o2.from).getNumber();
				int retVal = rev1 == rev2 ? 0 : (rev1 < rev2 ? -1 : 1);
				return RevisionComposite.this.reverseRevisions ? (retVal * -1) : retVal;
			}
		});
		return this.revisions;
	}
	
	public IRepositoryResource getSelectedResource() {
		return this.selectedResource;
	}
	
	public void addChangeRevisionListener(SelectionListener listener) {
		this.changeRevisionButton.addSelectionListener(listener);
	}
	
	public void setSelectedResource(IRepositoryResource resource) {
		this.selectedResource = resource;
		if (this.baseResource == null) {
			this.baseResource = resource;
		}
		if (this.selectedResource == null) {
			this.headRevisionRadioButton.setEnabled(false);
			this.changeRevisionRadioButton.setEnabled(false);
			this.changeRevisionButton.setEnabled(false);
			this.revisionField.setEditable(false);
		}
		else {
			SVNRevision rev = this.selectedResource.getSelectedRevision();
			if (rev.getKind() == Kind.NUMBER) {
				this.selectedRevision = rev;
				this.lastSelectedRevision = ((SVNRevision.Number)this.selectedRevision).getNumber();
				
				if (this.changeRevisionRadioButton != null) {
					this.revisionField.setText(this.selectedRevision.toString());
					this.headRevisionRadioButton.setSelection(false);
					if (this.checkStyled) {
						this.startFromCopyRadioButton.setSelection(false);
					}
					this.changeRevisionRadioButton.setSelection(true);
					this.changeRevisionButton.setEnabled(true);
					this.revisionField.setEditable(true);
				}
			}
			else {
				this.selectedRevision = this.defaultRevision;
				this.lastSelectedRevision = -1;
				
				if (this.changeRevisionRadioButton != null) {
					this.revisionField.setText("");
					this.headRevisionRadioButton.setSelection(!this.checkStyled);
					if (this.checkStyled) {
						this.startFromCopyRadioButton.setSelection(true);
					}
					this.changeRevisionRadioButton.setSelection(false);
					this.changeRevisionButton.setEnabled(false);
					this.revisionField.setEditable(false);
				}
			}
		}
	}
	
	public long getCurrentRevision() {
		return this.currentRevision;
	}
	
	public void setCurrentRevision(long currentRevision) {
		this.currentRevision = currentRevision;
	}
	
	private void createControls() {
		GridLayout layout = null;
		GridData data = null;
		
		layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        this.setLayout(layout);
		
		Group group = new Group(this, SWT.NONE);
		group.setText(this.captions == null ? SVNTeamUIPlugin.instance().getResource(this.checkStyled ? "RevisionComposite.Revisions" : "RevisionComposite.Revision") : this.captions[0]);
		layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		group.setLayoutData(data);

		this.headRevisionRadioButton = new Button(group, SWT.RADIO);
		this.headRevisionRadioButton.setText(this.captions == null ? SVNTeamUIPlugin.instance().getResource(this.checkStyled ? "RevisionComposite.All" : "RevisionComposite.HeadRevision") : this.captions[1]);
		data = new GridData();
		data.horizontalSpan = 2;
		this.headRevisionRadioButton.setLayoutData(data);
		this.headRevisionRadioButton.setSelection(!this.checkStyled);

		this.headRevisionRadioButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RevisionComposite.this.validationManager.validateContent();
				if (((Button)e.widget).getSelection()) {
					RevisionComposite.this.changeRevisionButton.setEnabled(false);
					RevisionComposite.this.revisionField.setEditable(false);
					RevisionComposite.this.startFromCopy = false;
					RevisionComposite.this.defaultToRevisions();
				}
				RevisionComposite.this.additionalValidation();
			}
		});
		
		if (this.checkStyled) {
			this.startFromCopyRadioButton = new Button(group, SWT.RADIO);
			this.startFromCopyRadioButton.setText(SVNTeamUIPlugin.instance().getResource("RevisionComposite.StartFromCopy"));
			data = new GridData();
			data.horizontalSpan = 2;
			this.startFromCopyRadioButton.setLayoutData(data);
			this.startFromCopyRadioButton.setSelection(true);

			this.startFromCopyRadioButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					RevisionComposite.this.validationManager.validateContent();
					if (((Button)e.widget).getSelection()) {
						RevisionComposite.this.changeRevisionButton.setEnabled(false);
						RevisionComposite.this.revisionField.setEditable(false);
						RevisionComposite.this.startFromCopy = true;
						RevisionComposite.this.defaultToRevisions();
					}
					RevisionComposite.this.additionalValidation();
				}
			});
		}
		
		this.changeRevisionRadioButton = new Button(group, SWT.RADIO);
		this.changeRevisionRadioButton.setText(SVNTeamUIPlugin.instance().getResource(this.checkStyled ? "RevisionComposite.RevisionsCtrl" : "RevisionComposite.RevisionCtrl"));
		
		this.changeRevisionRadioButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RevisionComposite.this.validationManager.validateContent();
				if (((Button)e.widget).getSelection()) {
					RevisionComposite.this.changeRevisionButton.setEnabled(true);
					RevisionComposite.this.revisionField.setEditable(true);
					RevisionComposite.this.startFromCopy = false;
					RevisionComposite.this.textToRevisions();
				}
				RevisionComposite.this.additionalValidation();
			}
		});
		
		final Composite revisionSelection = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		revisionSelection.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		revisionSelection.setLayoutData(data);
		
		if (this.checkStyled) {
			data = new GridData(GridData.FILL_HORIZONTAL);
		}
		else {
			data = new GridData();
			data.widthHint = 60;
		}
		this.revisionField = new Text(revisionSelection, SWT.SINGLE | SWT.BORDER);	
		this.revisionField.setLayoutData(data);
		this.revisionField.setEditable(false);
		CompositeVerifier verifier = new CompositeVerifier();
		String name = this.changeRevisionRadioButton.getText();
		verifier.add(new NonEmptyFieldVerifier(name));
		verifier.add(this.checkStyled ? new RevisionRangesVerifier(name) : new IntegerFieldVerifier(name, true));
		this.validationManager.attachTo(this.revisionField, new AbstractVerifierProxy(verifier) {
			protected boolean isVerificationEnabled(Control input) {
				return RevisionComposite.this.changeRevisionRadioButton.getSelection();
			}
		});
		this.revisionField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				RevisionComposite.this.textToRevisions();
			}
		});
		
		this.changeRevisionButton = new Button(revisionSelection, SWT.PUSH);
		this.changeRevisionButton.setText(SVNTeamUIPlugin.instance().getResource("RevisionComposite.Select"));
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(this.changeRevisionButton);
		this.changeRevisionButton.setLayoutData(data);
		this.changeRevisionButton.setEnabled(false);
		
		this.changeRevisionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				GetLogMessagesOperation msgsOp = SelectRevisionPanel.getMsgsOp(RevisionComposite.this.selectedResource, RevisionComposite.this.stopOnCopy);
				if (!UIMonitorUtility.doTaskNowDefault(RevisionComposite.this.getShell(), msgsOp, true).isCancelled() && msgsOp.getExecutionState() == IActionOperation.OK) {
				    SelectRevisionPanel panel = new SelectRevisionPanel(msgsOp, RevisionComposite.this.checkStyled, RevisionComposite.this.checkStyled, RevisionComposite.this.currentRevision);
				    if (RevisionComposite.this.toFilterCurrent) {
				    	RevisionLogEntryFilter revFilter = new RevisionLogEntryFilter();
				    	long revNum = SVNRevision.INVALID_REVISION_NUMBER;
				    	if (RevisionComposite.this.baseResource != null) {
					    	try {
					    		revNum = RevisionComposite.this.baseResource.getRevision(); 
					    	}
					    	catch (SVNConnectorException ex) {
					    	}
				    	}
				    	revFilter.setRevisionstoHide(revNum, revNum);
				    	panel.addFilter(revFilter);
				    }
					DefaultDialog dialog = new DefaultDialog(RevisionComposite.this.getShell(), panel);
					if (dialog.open() == 0) {
						if (RevisionComposite.this.checkStyled) {
							RevisionComposite.this.revisions = panel.getSelectedRevisions();
							String text = "";
							for (SVNRevisionRange range : RevisionComposite.this.revisions) {
								text += text.length() == 0 ? range.toString() : (", " + range.toString());
							}
						    RevisionComposite.this.revisionField.setText(text);
						}
						else {
						    long selectedRevisionNum = panel.getSelectedRevision();
						    RevisionComposite.this.lastSelectedRevision = selectedRevisionNum;
						    RevisionComposite.this.selectedRevision = SVNRevision.fromNumber(selectedRevisionNum);
						    RevisionComposite.this.revisionField.setText(String.valueOf(selectedRevisionNum));
						}
					}
				}
				RevisionComposite.this.additionalValidation();
			}
		});
		if (this.checkStyled) {
			this.reverseRevisionsButton = new Button(group, SWT.CHECK);
			this.reverseRevisionsButton.setText(SVNTeamUIPlugin.instance().getResource("RevisionComposite.Reverse"));
			data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;
			this.reverseRevisionsButton.setLayoutData(data);
			this.reverseRevisionsButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					RevisionComposite.this.reverseRevisions = ((Button)e.widget).getSelection();
				}
			});
		}
		
		this.defaultToRevisions();
	}
	
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.changeRevisionButton.setEnabled(enabled && this.changeRevisionRadioButton.getSelection());
		this.changeRevisionRadioButton.setEnabled(enabled);
		this.headRevisionRadioButton.setEnabled(enabled);
		if (this.checkStyled) {
			this.startFromCopyRadioButton.setEnabled(enabled);
			this.reverseRevisionsButton.setEnabled(enabled);
		}
	}
	
	public void additionalValidation() {
		//override this if there is a need to perform additional validation
	}
	
	protected void defaultToRevisions() {
		if (this.checkStyled) {
			this.revisions = new SVNRevisionRange[] {new SVNRevisionRange(SVNRevision.START, this.defaultRevision)};
		}
		else {
			this.selectedRevision = this.defaultRevision;
		}
	}
	
	protected void textToRevisions() {
		String input = this.revisionField.getText();
		try {
			if (this.checkStyled) {
				String []parts = input.split(",");
		    	ArrayList<SVNRevisionRange> revisions = new ArrayList<SVNRevisionRange>();
				for (String part : parts) {
					revisions.add(new SVNRevisionRange(part.trim()));
				}
				this.revisions = revisions.toArray(new SVNRevisionRange[revisions.size()]);
			}
			else {
				long selectedRevisionNum = Long.parseLong(input);
				if (selectedRevisionNum >= 0) {
				    this.lastSelectedRevision = selectedRevisionNum;
				    this.selectedRevision = SVNRevision.fromNumber(selectedRevisionNum);
				}
			}
		}
		catch (NumberFormatException ex) {
			//don't handle this exception - already handled by the verifier
		}
	}
	
}
