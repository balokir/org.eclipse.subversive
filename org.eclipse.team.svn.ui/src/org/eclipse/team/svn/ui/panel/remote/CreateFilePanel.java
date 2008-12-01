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

package org.eclipse.team.svn.ui.panel.remote;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.team.svn.ui.verifier.ExistingResourceVerifier;

/**
 * create Remote File Panel
 * 
 * @author Sergiy Logvin
 */
public class CreateFilePanel extends AbstractDialogPanel {
	protected Text locationField;
	protected String location;
	protected CommentComposite comment;
	protected String []fileNames;
	
	public CreateFilePanel(String importToUrl) {
		super();
		this.dialogTitle = SVNUIMessages.CreateFilePanel_Title;
		this.dialogDescription = SVNUIMessages.CreateFilePanel_Description;
		this.defaultMessage = SVNUIMessages.format(SVNUIMessages.CreateFilePanel_Message, new String[] {importToUrl});
    }
	
	public void createControlsImpl(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		Composite fileGroup = new Composite(parent, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		fileGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		fileGroup.setLayoutData(data);
		
		Label fileLabel = new Label(fileGroup, SWT.NONE);
		data = new GridData();
		fileLabel.setLayoutData(data);
		fileLabel.setText(SVNUIMessages.CreateFilePanel_FilePath);
		
		this.locationField = new Text(fileGroup,  SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.locationField.setLayoutData(data);
		this.locationField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = CreateFilePanel.this.locationField.getText(); 
				if (text.indexOf("\"") == -1) {
					CreateFilePanel.this.location = new Path(text).removeLastSegments(1).toString();
				}
			}
		});
		this.attachTo(this.locationField, new ExistingResourceMultiVerifier(SVNUIMessages.CreateFilePanel_FilePath_Verifier));
		Button browseButton = new Button(fileGroup, SWT.PUSH);
		browseButton.setText(SVNUIMessages.Button_Browse);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(browseButton);
		browseButton.setLayoutData(data);
		browseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog fileDialog = new FileDialog(CreateFilePanel.this.manager.getShell(), SWT.MULTI);
				fileDialog.setText(SVNUIMessages.CreateFilePanel_ImportFile);
				String path = fileDialog.open();
				if (path != null) {
					String []fileNames = fileDialog.getFileNames();
					CreateFilePanel.this.location = new Path(path).removeLastSegments(1).toString();
					CreateFilePanel.this.fileNames = fileNames;
					if (fileNames.length > 1) {
						String text = "";
						for (int i = 0; i < fileNames.length; i++) {
							text += text.length() > 0 ? " \"" + fileNames[i] + "\"": "\"" + fileNames[i] + "\""; 
						}
						CreateFilePanel.this.locationField.setText(text);
					}
					else {
						CreateFilePanel.this.locationField.setText(path);
					}
				}
			}
		});
						
		Group group = new Group(parent, SWT.NULL);
		group.setLayout(new GridLayout());
		data = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(data);
		group.setText(SVNUIMessages.CreateFilePanel_Comment);
		
		this.comment = new CommentComposite(group, this);
		data = new GridData(GridData.FILL_BOTH);
		this.comment.setLayoutData(data);
    }
	
    public Point getPrefferedSizeImpl() {
    	return new Point(525, SWT.DEFAULT);
    }
    
	public void postInit() {
		super.postInit();
		this.comment.postInit(this.manager);
	}
		
	protected void saveChangesImpl() {
		this.comment.saveChanges();
		String text = CreateFilePanel.this.locationField.getText();
		if (text.indexOf("\"") > -1) {
			this.fileNames = this.parseFileNames(text);
		}	
		else {
			Path path = new Path(text);
			this.location = path.removeLastSegments(1).toString();
			this.fileNames = new String[] {path.lastSegment()};
		}
	}
	
	protected String[] parseFileNames(String text) {
		Reader r = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(text.getBytes())));
		final StreamTokenizer tokenizer = new StreamTokenizer(r);
		tokenizer.resetSyntax();
		tokenizer.quoteChar('\"');
		tokenizer.whitespaceChars(32, 32);
		final Set<String> fileNames = new HashSet<String>();
		
		new AbstractActionOperation("Operation.ParseFile") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
					fileNames.add(tokenizer.sval);
				}
			}
			public ISchedulingRule getSchedulingRule() {
				return null;
			}
		}.run(new NullProgressMonitor());
		
		return fileNames.toArray(new String[fileNames.size()]);
	}
	
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.remote_createFileDialogContext";
	}

    protected void cancelChangesImpl() {
    	this.comment.cancelChanges();
    }
    
    public String getLocation() {
    	return this.location;
    }
    
    public String []getFileNames() {
		return this.fileNames;
    }
    
    public String getMessage() {
    	return this.comment.getMessage();
    }
   
    protected class ExistingResourceMultiVerifier extends ExistingResourceVerifier {
    	protected String ERROR_MESSAGE_DOES_NOT_EXIST_MULTIPLE;
    	protected String ERROR_MESSAGE_IS_NOT_A_FILE_MULTIPLE;

		public ExistingResourceMultiVerifier(String fieldName) {
			super(fieldName, true);
	    	this.ERROR_MESSAGE_DOES_NOT_EXIST_MULTIPLE = SVNUIMessages.format(SVNUIMessages.CreateFilePanel_FilePath_Verifier_Error_Exists, new String[] {AbstractFormattedVerifier.FIELD_NAME});
	    	this.ERROR_MESSAGE_IS_NOT_A_FILE_MULTIPLE = SVNUIMessages.format(SVNUIMessages.CreateFilePanel_FilePath_Verifier_Error_NotAFile, new String[] {AbstractFormattedVerifier.FIELD_NAME});
		}
    	
		protected String getErrorMessageImpl(Control input) {
			if (CreateFilePanel.this.location == null) {
				return ExistingResourceVerifier.ERROR_MESSAGE_DOES_NOT_EXIST;
			}
			boolean existAll = true;
			boolean allFiles = true;
			String text = this.getText(input);
		    if (text.indexOf("\"") > -1) {
		        String []fileNames = CreateFilePanel.this.parseFileNames(text);
				for (int i = 0; i < fileNames.length; i++) {
					if (fileNames[i] == null || fileNames[i].trim().length() == 0) {
						existAll = false;
						break;
					}
					String pathToFile = (new Path(CreateFilePanel.this.location)).append(fileNames[i]).toString();
					File file = new File(pathToFile);
					existAll = existAll && file.exists();
					allFiles = allFiles && file.isFile();
					if (!existAll || !allFiles) {
						break;
					}
				}
				if (!existAll) {
					return this.ERROR_MESSAGE_DOES_NOT_EXIST_MULTIPLE;
				}
				else if (!allFiles) {
					return this.ERROR_MESSAGE_IS_NOT_A_FILE_MULTIPLE;
				}
			}
			else {
				return super.getErrorMessageImpl(input);
			}
			return null;
		}
		
    }
    
}
