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

package org.eclipse.team.svn.ui.panel.view.property;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.resource.IRepositoryFolder;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.RevisionComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.URLVerifier;
import org.eclipse.team.svn.ui.wizard.SelectRepositoryResourceWizard;

/**
 * Set externals property panel
 * 
 * @author Igor Burilo
 */
public class ExternalsEditPanel extends AbstractDialogPanel {	
	
	protected IResource resource;
	protected IRepositoryResource repositoryResource;
	
	protected Combo urlText;
	protected Button browse;
	protected UserInputHistory urlHistory;
	protected String comboId;	
	protected Button folderButton;
	protected Text localPathText;
	protected RevisionComposite revisionComposite;
	protected Button priorToSVN15FormatButton;
	protected Combo relativeText;
	protected Button formatButton;
	
	protected String localPath;
	protected boolean priorToSVN15Format;
	protected String url;
	protected SVNRevision revision;
	protected boolean isFolder;
	protected String processedUrl;
	protected IRepositoryResource repositoryResourceForUrl;
	
	protected final static int REPOSITORY_ROOT_INDEX = 0;
	protected final static int URL_SCHEME_INDEX = 1;
	protected final static int HOST_NAME_INDEX = 2;
	protected final static int EXTERNAL_DIRECTORY_INDEX = 3;
	
	public ExternalsEditPanel(String historyKey, String comboId, IResource resource, IRepositoryResource repositoryResource) {
		this.resource = resource;
		this.repositoryResource = repositoryResource;
								
		this.dialogTitle = SVNUIMessages.ExternalsEditPanel_DialogTitle;
		this.dialogDescription = SVNUIMessages.ExternalsEditPanel_DialogDescription;
		this.defaultMessage = SVNUIMessages.ExternalsEditPanel_DialogDefaultMessage;
		
		this.urlHistory = new UserInputHistory(historyKey);
		this.comboId =  comboId;
	}
	
	protected void createControlsImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;	
		layout.numColumns = 3;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);		
		composite.setLayoutData(data);
				
		this.createLocalPathSelectionControls(composite);
				
		this.createRepositoryResourceSelectionControls(composite);
		
		this.revisionComposite = new RevisionComposite(composite, this, true, new String[] {SVNUIMessages.RevisionComposite_Revision, SVNUIMessages.RepositoryResourceSelectionComposite_HeadRevision}, SVNRevision.INVALID_REVISION, false, false);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		this.revisionComposite.setLayoutData(data);
		
		this.priorToSVN15FormatButton = new Button(composite, SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 3;
		this.priorToSVN15FormatButton.setLayoutData(data);
		this.priorToSVN15FormatButton.setText(SVNUIMessages.ExternalsEditPanel_PriortoSVN15);
		this.priorToSVN15FormatButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ExternalsEditPanel.this.priorToSVN15Format = ExternalsEditPanel.this.priorToSVN15FormatButton.getSelection();				
				ExternalsEditPanel.this.enableFormatUrl();
				ExternalsEditPanel.this.enableIsFolder();
				ExternalsEditPanel.this.onChangeUrlText();
				ExternalsEditPanel.this.validateContent();
			}
		});
		
		this.initValues();
	}
	
	protected void initValues() {
		if(CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() < ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x) {
			this.priorToSVN15Format = false;
			this.priorToSVN15FormatButton.setEnabled(this.priorToSVN15Format);
		}		
				
		this.folderButton.setSelection(this.isFolder = true);		
		this.enableIsFolder();

		this.enableFormatUrl();
	}
	
	protected void enableIsFolder() {
		if (this.isLessSVN16() || this.isPriorToSVN15Format()) {			
			this.folderButton.setSelection(this.isFolder = true);
			this.folderButton.setEnabled(false);
		} else {
			this.folderButton.setEnabled(true);
		}
	}
	
	protected void enableFormatUrl() {
		boolean isEnable = false;
		if (!this.isPriorToSVN15Format() && CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() > ISVNConnectorFactory.APICompatibility.SVNAPI_1_4_x) {
			//url is full and from the same repository
			isEnable = this.url != null && this.repositoryResourceForUrl != null && SVNUtility.isValidSVNURL(this.url) && this.repositoryResourceForUrl.getRepositoryLocation().getRepositoryRoot().equals(this.repositoryResource.getRepositoryLocation().getRepositoryRoot());			
		}
		this.relativeText.setEnabled(isEnable);
		this.formatButton.setEnabled(isEnable);					
	}
	
	protected void createLocalPathSelectionControls(Composite parent) {
		Label localPathLabel = new Label(parent, SWT.NONE);
		GridData data = new GridData();
		localPathLabel.setLayoutData(data);
		localPathLabel.setText(SVNUIMessages.ExternalsEditPanel_LocalPathLabel);
		
		this.localPathText = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);		
		this.localPathText.setLayoutData(data);				
		this.localPathText.addModifyListener(new ModifyListener() {			
			public void modifyText(ModifyEvent e) {
				ExternalsEditPanel.this.localPath = ExternalsEditPanel.this.localPathText.getText();			
			}
		});		
		
		this.folderButton = new Button(parent, SWT.CHECK);
		this.folderButton.setLayoutData(new GridData());
		this.folderButton.setText(SVNUIMessages.ExternalsEditPanel_IsFolder);
		this.folderButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ExternalsEditPanel.this.isFolder = ExternalsEditPanel.this.folderButton.getSelection();
			}			
		});						
	}
	
	protected void createRepositoryResourceSelectionControls(Composite parent) {
		Label urlLabel = new Label(parent, SWT.NONE);
		urlLabel.setLayoutData(new GridData());
		urlLabel.setText(SVNUIMessages.getString(this.comboId));
		
		this.urlText = new Combo(parent, SWT.NULL);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.urlText.setLayoutData(data);
		this.urlText.setVisibleItemCount(this.urlHistory.getDepth());
		this.urlText.setItems(this.urlHistory.getHistory());							
		
		Listener urlTextListener = new Listener() {
			public void handleEvent(Event event) {
				ExternalsEditPanel.this.onChangeUrlText();				
			}
		};
		this.urlText.addListener(SWT.Selection, urlTextListener);
		this.urlText.addListener(SWT.Modify, urlTextListener);
						
		this.browse = new Button(parent, SWT.PUSH);
		this.browse.setText(SVNUIMessages.Button_Browse);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(this.browse);
		this.browse.setLayoutData(data);
		this.browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ExternalsEditPanel.this.onRepositoryResourceSelection();								
			}
		});
		
		//format url		
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(SVNUIMessages.ExternalsEditPanel_FormatUrl);
		
		this.relativeText = new Combo(parent, SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.relativeText.setLayoutData(data);
		
		this.relativeText.add(SVNUIMessages.ExternalsEditPanel_RelativeToRepositoryRoot, ExternalsEditPanel.REPOSITORY_ROOT_INDEX);
		this.relativeText.add(SVNUIMessages.ExternalsEditPanel_RelativeToUrlScheme, ExternalsEditPanel.URL_SCHEME_INDEX);
		this.relativeText.add(SVNUIMessages.ExternalsEditPanel_RelativeToHostName, ExternalsEditPanel.HOST_NAME_INDEX);
		this.relativeText.add(SVNUIMessages.ExternalsEditPanel_RelativeToDirectory, ExternalsEditPanel.EXTERNAL_DIRECTORY_INDEX);
		this.relativeText.setVisibleItemCount(4);				
		this.relativeText.select(0);				
		
		this.formatButton = new Button(parent, SWT.PUSH);
		this.formatButton.setText(SVNUIMessages.ExternalsEditPanel_FormatButton);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(this.formatButton);
		this.formatButton.setLayoutData(data);
		
		this.formatButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ExternalsEditPanel.this.formatUrl();								
			}
		});
				
		CompositeVerifier urlVerifier = new CompositeVerifier() {
			@Override
			protected void fireError(String errorReason) {
				super.fireError(errorReason);
				ExternalsEditPanel.this.onUrlValidity(false);				
			}
			@Override
			protected void fireOk() {
				super.fireOk();
				ExternalsEditPanel.this.onUrlValidity(true);				
			}
		};
		urlVerifier.add(new NonEmptyFieldVerifier(SVNUIMessages.getString(this.comboId + "_Verifier"))); //$NON-NLS-1$
		urlVerifier.add(new URLVerifier(SVNUIMessages.getString(this.comboId + "_Verifier")) { //$NON-NLS-1$
			protected String getText(Control input) {
				return processedUrl != null ? processedUrl : url;								
			}
		});		
		this.attachTo(this.urlText, urlVerifier);		
		
		CompositeVerifier localPathVerifier = new CompositeVerifier();
		localPathVerifier.add(new NonEmptyFieldVerifier(SVNUIMessages.ExternalsEditPanel_LocalPathLabel_Verifier));
		//don't allow spaces  if SVN < 1.6
		localPathVerifier.add(new AbstractFormattedVerifier(SVNUIMessages.ExternalsEditPanel_LocalPathLabel_Verifier) {
			@Override
			protected String getErrorMessageImpl(Control input) {
				String text = this.getText(input);
				text = text.trim();
				if (text.contains(" ") && ExternalsEditPanel.this.isLessSVN16()) { //$NON-NLS-1$
					return SVNUIMessages.format(SVNUIMessages.Verifier_NoSpaces, new String[] {AbstractFormattedVerifier.FIELD_NAME}); 
				}			
				return null;
			}
			@Override
			protected String getWarningMessageImpl(Control input) {				
				return null;
			}			
		});
		//TODO check that resource doesn't exist on file system ?
		this.attachTo(this.localPathText, localPathVerifier);
	}
	
	protected void onUrlValidity(boolean isValidUrl) {
		this.revisionComposite.setEnabled(isValidUrl);		
		this.enableFormatUrl();
	}
	
	protected void formatUrl() {
		String fullUrl = this.url.trim();
		IPath fullUrlPath = new Path(fullUrl);	
		int relativeIndex = this.relativeText.getSelectionIndex();
		if (relativeIndex == ExternalsEditPanel.REPOSITORY_ROOT_INDEX) {
			IPath repositoryRoot = new Path(this.repositoryResource.getRepositoryLocation().getRepositoryRootUrl());					
			if (repositoryRoot.isPrefixOf(fullUrlPath)) {
				String relative = fullUrlPath.makeRelativeTo(repositoryRoot).toString();
				this.urlText.setText("^/" + relative); //$NON-NLS-1$
			}
		} else if (relativeIndex == ExternalsEditPanel.URL_SCHEME_INDEX) {			
			int index = 0;
			if (fullUrl.startsWith("file:///")) { //$NON-NLS-1$
				index = "file:///".length(); //$NON-NLS-1$
			} else if (fullUrl.startsWith("file://")) { //$NON-NLS-1$
				index = "file://".length(); //$NON-NLS-1$
			} else if (fullUrl.startsWith("http://")) { //$NON-NLS-1$
				index = "http://".length(); //$NON-NLS-1$
			} else if (fullUrl.startsWith("https://")) { //$NON-NLS-1$
				index = "https://".length(); //$NON-NLS-1$
			} else if (fullUrl.startsWith("svn://")) { //$NON-NLS-1$
				index = "svn://".length(); //$NON-NLS-1$
			} else if (fullUrl.startsWith("svn+ssh://")) { //$NON-NLS-1$
				index = "svn+ssh://".length(); //$NON-NLS-1$
			}
			if (index != 0) {
				String relativaPath = "//" + fullUrl.substring(index); //$NON-NLS-1$
				this.urlText.setText(relativaPath);
			}				
		} else if (relativeIndex == ExternalsEditPanel.HOST_NAME_INDEX) {			
			try {				
				URL url = SVNUtility.getSVNUrl(fullUrl);
				String relativePath = url.getFile();
				this.urlText.setText(relativePath);				
			} catch (MalformedURLException me) {
				//ignore
			} 			
		} else if (relativeIndex == ExternalsEditPanel.EXTERNAL_DIRECTORY_INDEX) {								
			//find common path
			IPath resourcePath = new Path(this.repositoryResource.getUrl());
			IPath commonPath = resourcePath;
			int relativeSegmentsCount = 0;			
			do {
				commonPath = commonPath.removeLastSegments(1);
				relativeSegmentsCount ++;
			} while (!commonPath.isPrefixOf(fullUrlPath) && !commonPath.isEmpty());						
			if (!commonPath.isEmpty()) {
				StringBuffer relativePath = new StringBuffer();
				for (int i = 0; i < relativeSegmentsCount; i ++) {
					relativePath.append("../"); //$NON-NLS-1$
				}
				relativePath.append(fullUrlPath.makeRelativeTo(commonPath).toString());
				this.urlText.setText(relativePath.toString());
			}						
		}
	}
	
	protected void onChangeUrlText() {
		this.url = this.urlText.getText();
		if (!this.isPriorToSVN15Format() && CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() > ISVNConnectorFactory.APICompatibility.SVNAPI_1_4_x) {
			try {
				this.processedUrl = SVNUtility.replaceRelativeExternalParts(this.url, this.repositoryResource);	
			} catch (Exception e) {
				this.processedUrl = null;
			}				
		} else {
			this.processedUrl = this.url;
		}
		this.repositoryResourceForUrl = this.processedUrl != null ? SVNUtility.asRepositoryResource(this.processedUrl, this.isFolder) : null;		
		this.revisionComposite.setSelectedResource(this.repositoryResourceForUrl);					
	}
	
	protected void onRepositoryResourceSelection() {
		SelectRepositoryResourceWizard wizard;
		if (this.isFolder) {
			wizard = new SelectRepositoryResourceWizard(this.isFolder);
		} else {
			wizard = new SelectRepositoryResourceWizard(this.isFolder, this.repositoryResource.getRepositoryLocation());
		}		
		WizardDialog dialog = new WizardDialog(this.manager.getShell(), wizard);
		if (dialog.open() == 0) {
			IRepositoryResource resource = wizard.getSelectedResource();
			this.folderButton.setSelection(resource instanceof IRepositoryFolder);			
			this.urlText.setText(resource.getUrl());
		}
	}
		
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.setExternalsDialogContext"; //$NON-NLS-1$
	}
	
	protected void saveChangesImpl() {							
		this.revision = this.revisionComposite.getSelectedRevision();
		
		this.urlHistory.addLine(this.urlText.getText());
	}
	
	protected void cancelChangesImpl() {		
	}
	
	protected boolean isLessSVN16() {
		return CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() < ISVNConnectorFactory.APICompatibility.SVNAPI_1_6_x;		
	}
	
	public String getLocalPath() {
		return this.localPath;
	}
	
	public boolean isPriorToSVN15Format() {
		return this.priorToSVN15Format;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public SVNRevision getRevision() {
		return this.revision;
	}

	public boolean isFolder() {
		return this.isFolder;
	}
		
	@Override
	protected Point getPrefferedSizeImpl() {	
		return new Point(520, SWT.DEFAULT);
	}
}
