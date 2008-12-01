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
import java.util.Calendar;
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
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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
import org.eclipse.team.svn.ui.SVNUIMessages;
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
	protected Button dateTimeRadioButton;
	protected DateTime dateField;
	protected DateTime timeField;
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
			UIMonitorUtility.doTaskNowDefault(new AbstractActionOperation("Operation_DetectStartRevision") { //$NON-NLS-1$
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
			this.setEnabled(false);
		}
		else {
			SVNRevision rev = this.selectedResource.getSelectedRevision();
			if (rev.getKind() == Kind.NUMBER) {
				this.selectedRevision = rev;
				this.lastSelectedRevision = ((SVNRevision.Number)this.selectedRevision).getNumber();
				
				this.revisionField.setText(this.selectedRevision.toString());
				this.headRevisionRadioButton.setSelection(false);
				if (this.checkStyled) {
					this.startFromCopyRadioButton.setSelection(false);
				}
				else {
					this.dateTimeRadioButton.setSelection(false);
				}
				this.changeRevisionRadioButton.setSelection(true);
			}
			else if (rev.getKind() == Kind.DATE && !this.checkStyled) {
				this.selectedRevision = rev;
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(((SVNRevision.Date)rev).getDate());
				this.dateField.setYear(calendar.get(Calendar.YEAR));
				this.dateField.setMonth(calendar.get(Calendar.MONTH));
				this.dateField.setDay(calendar.get(Calendar.DAY_OF_MONTH));
				this.timeField.setHours(calendar.get(Calendar.HOUR_OF_DAY));
				this.timeField.setMinutes(calendar.get(Calendar.MINUTE));
				this.timeField.setSeconds(calendar.get(Calendar.SECOND));
				this.headRevisionRadioButton.setSelection(false);
				this.dateTimeRadioButton.setSelection(true);
				this.changeRevisionRadioButton.setSelection(false);
			}
			else {
				this.selectedRevision = this.defaultRevision;
				this.lastSelectedRevision = -1;
				
				this.revisionField.setText(""); //$NON-NLS-1$
				this.headRevisionRadioButton.setSelection(!this.checkStyled);
				if (this.checkStyled) {
					this.startFromCopyRadioButton.setSelection(true);
				}
				else {
					this.dateTimeRadioButton.setSelection(false);
				}
				this.changeRevisionRadioButton.setSelection(false);
			}
			this.setEnabled(true);
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
		group.setText(this.captions == null ? this.checkStyled ? SVNUIMessages.RevisionComposite_Revisions : SVNUIMessages.RevisionComposite_Revision : this.captions[0]);
		layout = new GridLayout();
		layout.numColumns = 3;
		group.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		group.setLayoutData(data);

		this.headRevisionRadioButton = new Button(group, SWT.RADIO);
		this.headRevisionRadioButton.setText(this.captions == null ? this.checkStyled ? SVNUIMessages.RevisionComposite_All : SVNUIMessages.RevisionComposite_HeadRevision : this.captions[1]);
		data = new GridData();
		data.horizontalSpan = 3;
		this.headRevisionRadioButton.setLayoutData(data);
		this.headRevisionRadioButton.setSelection(!this.checkStyled);

		this.headRevisionRadioButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RevisionComposite.this.validationManager.validateContent();
				if (((Button)e.widget).getSelection()) {
					RevisionComposite.this.changeRevisionButton.setEnabled(false);
					RevisionComposite.this.revisionField.setEnabled(false);
					RevisionComposite.this.dateField.setEnabled(false);
					RevisionComposite.this.timeField.setEnabled(false);
					RevisionComposite.this.startFromCopy = false;
					RevisionComposite.this.defaultToRevisions();
				}
				RevisionComposite.this.additionalValidation();
			}
		});
		
		if (this.checkStyled) {
			this.startFromCopyRadioButton = new Button(group, SWT.RADIO);
			this.startFromCopyRadioButton.setText(SVNUIMessages.RevisionComposite_StartFromCopy);
			data = new GridData();
			data.horizontalSpan = 3;
			this.startFromCopyRadioButton.setLayoutData(data);
			this.startFromCopyRadioButton.setSelection(true);

			this.startFromCopyRadioButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					RevisionComposite.this.validationManager.validateContent();
					if (((Button)e.widget).getSelection()) {
						RevisionComposite.this.changeRevisionButton.setEnabled(false);
						RevisionComposite.this.revisionField.setEnabled(false);
						RevisionComposite.this.dateField.setEnabled(false);
						RevisionComposite.this.timeField.setEnabled(false);
						RevisionComposite.this.startFromCopy = true;
						RevisionComposite.this.defaultToRevisions();
					}
					RevisionComposite.this.additionalValidation();
				}
			});
		}
		else {
			this.dateTimeRadioButton = new Button(group, SWT.RADIO);
			this.dateTimeRadioButton.setText(SVNUIMessages.RevisionComposite_DateTime);
			data = new GridData();
			this.dateTimeRadioButton.setLayoutData(data);
			this.dateTimeRadioButton.setSelection(false);

			this.dateTimeRadioButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					RevisionComposite.this.validationManager.validateContent();
					if (((Button)e.widget).getSelection()) {
						RevisionComposite.this.changeRevisionButton.setEnabled(false);
						RevisionComposite.this.revisionField.setEnabled(false);
						RevisionComposite.this.dateField.setEnabled(true);
						RevisionComposite.this.timeField.setEnabled(true);
						RevisionComposite.this.startFromCopy = false;
						RevisionComposite.this.dateTimeToRevision();
					}
					RevisionComposite.this.additionalValidation();
				}
			});

			Composite cmp = new Composite(group, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginHeight = layout.marginWidth = 0;
			cmp.setLayout(layout);
			data = new GridData();
			cmp.setLayoutData(data);
			
			this.dateField = new DateTime(cmp, SWT.DATE | SWT.MEDIUM);
			data = new GridData();
			this.dateField.setLayoutData(data);
			this.dateField.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					RevisionComposite.this.dateTimeToRevision();
				}
			});

			this.timeField = new DateTime(cmp, SWT.TIME | SWT.MEDIUM);
			data = new GridData();
			this.timeField.setLayoutData(data);
			this.timeField.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					RevisionComposite.this.dateTimeToRevision();
				}
			});
			
			Label label = new Label(group, SWT.NONE);
			data = new GridData();
			label.setLayoutData(data);
			
			this.dateField.setEnabled(false);
			this.timeField.setEnabled(false);
		}
		
		this.changeRevisionRadioButton = new Button(group, SWT.RADIO);
		this.changeRevisionRadioButton.setText(this.checkStyled ? SVNUIMessages.RevisionComposite_RevisionsCtrl : SVNUIMessages.RevisionComposite_RevisionCtrl);
		
		this.changeRevisionRadioButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RevisionComposite.this.validationManager.validateContent();
				if (((Button)e.widget).getSelection()) {
					RevisionComposite.this.changeRevisionButton.setEnabled(true);
					RevisionComposite.this.revisionField.setEnabled(true);
					RevisionComposite.this.dateField.setEnabled(false);
					RevisionComposite.this.timeField.setEnabled(false);
					RevisionComposite.this.startFromCopy = false;
					RevisionComposite.this.textToRevisions();
				}
				RevisionComposite.this.additionalValidation();
			}
		});
		
		if (this.checkStyled) {
			data = new GridData(GridData.FILL_HORIZONTAL);
		}
		else {
			data = new GridData();
			data.horizontalAlignment = SWT.FILL;
		}
		this.revisionField = new Text(group, SWT.SINGLE | SWT.BORDER);	
		this.revisionField.setLayoutData(data);
		this.revisionField.setEnabled(false);
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
		
		this.changeRevisionButton = new Button(group, SWT.PUSH);
		this.changeRevisionButton.setText(SVNUIMessages.Button_Browse);
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
							String text = ""; //$NON-NLS-1$
							for (SVNRevisionRange range : RevisionComposite.this.revisions) {
								text += text.length() == 0 ? range.toString() : (", " + range.toString()); //$NON-NLS-1$
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
			this.reverseRevisionsButton.setText(SVNUIMessages.RevisionComposite_Reverse);
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
		this.headRevisionRadioButton.setEnabled(enabled);
		if (this.checkStyled) {
			this.startFromCopyRadioButton.setEnabled(enabled);
			this.reverseRevisionsButton.setEnabled(enabled);
		}
		else {
			this.dateTimeRadioButton.setEnabled(enabled);
			this.dateField.setEnabled(enabled && this.dateTimeRadioButton.getSelection());
			this.timeField.setEnabled(enabled && this.dateTimeRadioButton.getSelection());
		}
		this.changeRevisionRadioButton.setEnabled(enabled);
		this.changeRevisionButton.setEnabled(enabled && this.changeRevisionRadioButton.getSelection());
		this.revisionField.setEnabled(enabled && this.changeRevisionRadioButton.getSelection());
	}
	
	public void additionalValidation() {
		//override this if there is a need to perform additional validation
	}
	
	protected void dateTimeToRevision() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(this.dateField.getYear(), this.dateField.getMonth(), this.dateField.getDay(), this.timeField.getHours(), this.timeField.getMinutes(), this.timeField.getSeconds());
		this.selectedRevision = SVNRevision.fromDate(calendar.getTimeInMillis());
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
				String []parts = input.split(","); //$NON-NLS-1$
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
