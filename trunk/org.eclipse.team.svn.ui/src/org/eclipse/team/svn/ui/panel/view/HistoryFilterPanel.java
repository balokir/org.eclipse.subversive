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

package org.eclipse.team.svn.ui.panel.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;

/**
 * History filter modification dialog
 * 
 * @author Alexander Gurov
 */
public class HistoryFilterPanel extends AbstractDialogPanel {
    protected static final String FILTER_AUTHOR_HISTORY_NAME = "filterAuthor";
    protected static final String FILTER_COMMENT_HISTORY_NAME = "filterComment";
    
    protected String filter;
    protected Button authorButton;
    protected Button commentButton;
    protected Combo authorsCombo;
    protected Combo commentsCombo;
    protected boolean commentFilterEnabled;
    protected String authorInput = "";
    protected String commentInput = "";
    protected String[] selectedAuthors;
    protected UserInputHistory authorsHistory;
    protected UserInputHistory commentsHistory;
    
    public HistoryFilterPanel(String authorInput, String commentInput, String[] selectedAuthors, boolean commentFilterEnabled) {
        super();
        this.dialogTitle = SVNTeamUIPlugin.instance().getResource("HistoryFilterPanel.Title");
        this.dialogDescription = SVNTeamUIPlugin.instance().getResource("HistoryFilterPanel.Description");
        this.defaultMessage = SVNTeamUIPlugin.instance().getResource("HistoryFilterPanel.Message");
        
        this.selectedAuthors = selectedAuthors;
        this.authorInput = authorInput;
        this.commentInput = commentInput;
        this.commentFilterEnabled = commentFilterEnabled;
    }

    public String getAuthor() {
        return this.authorInput.trim();
    }
    
    public String getComment() {
        return this.commentInput.trim();
    }
    
    public boolean isCommentFilterEnabled() {
    	return this.commentFilterEnabled;    	
    }
    
    public Point getPrefferedSize() {
        return new Point(470, 100);
    }
    
    public void createControls(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);

		this.authorButton = new Button(composite, SWT.CHECK);
		this.authorButton.setText(SVNTeamUIPlugin.instance().getResource("HistoryFilterPanel.Author"));
		this.authorButton.setSelection(authorInput.length() > 0);
		this.authorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				HistoryFilterPanel.this.validateContent();
				if (HistoryFilterPanel.this.authorButton.getSelection()) {
				    HistoryFilterPanel.this.authorsCombo.setEnabled(true);
				}
				else {
				    HistoryFilterPanel.this.authorsCombo.setEnabled(false);
				    HistoryFilterPanel.this.authorInput = "";
				}				
			}
		});
		this.authorsHistory = new UserInputHistory(HistoryFilterPanel.FILTER_AUTHOR_HISTORY_NAME);
				
		this.authorsCombo = new Combo(composite, SWT.NULL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.authorsCombo.setLayoutData(data);
		this.authorsCombo.setEnabled(this.authorButton.getSelection());
		this.authorsCombo.setVisibleItemCount(this.authorsHistory.getDepth());
		this.authorsCombo.setItems(this.mergeAuthorsList());
		this.authorsCombo.setText(this.authorInput);
		this.authorsCombo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                HistoryFilterPanel.this.authorInput = HistoryFilterPanel.this.authorsCombo.getText();
                HistoryFilterPanel.this.validateContent();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
		this.attachTo(this.authorsCombo, 
				new AbstractVerifierProxy(new NonEmptyFieldVerifier(SVNTeamUIPlugin.instance().getResource("HistoryFilterPanel.Author.Verifier"))) {
					protected boolean isVerificationEnabled(Control input) {
						return HistoryFilterPanel.this.authorButton.getSelection();
					}
		});
		
		this.commentButton = new Button(composite, SWT.CHECK);
		commentButton.setText(SVNTeamUIPlugin.instance().getResource("HistoryFilterPanel.Comment"));
		commentButton.setSelection(this.commentFilterEnabled);
		commentButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				HistoryFilterPanel.this.validateContent();
				if (HistoryFilterPanel.this.commentButton.getSelection()) {
				    HistoryFilterPanel.this.commentsCombo.setEnabled(true);
				    HistoryFilterPanel.this.commentFilterEnabled = true;
				}
				else {
				    HistoryFilterPanel.this.commentsCombo.setEnabled(false);
				    HistoryFilterPanel.this.commentInput = "";
				    HistoryFilterPanel.this.commentFilterEnabled = false;
				}				
			}
		});
		
		this.commentsHistory = new UserInputHistory(HistoryFilterPanel.FILTER_COMMENT_HISTORY_NAME);
		
		this.commentsCombo = new Combo(composite, SWT.NULL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.commentsCombo.setLayoutData(data);
		this.commentsCombo.setEnabled(this.commentButton.getSelection());
		this.commentsCombo.setVisibleItemCount(this.commentsHistory.getDepth());
		this.commentsCombo.setItems(this.commentsHistory.getHistory());
		this.commentsCombo.setText(this.commentInput);
		this.commentsCombo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                HistoryFilterPanel.this.commentInput = HistoryFilterPanel.this.commentsCombo.getText();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }
    
    protected void saveChanges() {
        if (this.authorButton.getSelection()) {
            this.authorInput = this.authorsCombo.getText();
            this.authorsHistory.addLine(this.authorInput);
        }
        if (this.commentButton.getSelection()) {
            this.commentInput = this.commentsCombo.getText();
            this.commentsHistory.addLine(this.commentInput);
        }
    }

    protected void cancelChanges() {

    }
    
    protected String []mergeAuthorsList() {
    	ArrayList merged = new ArrayList();
    	merged.addAll(Arrays.asList(this.selectedAuthors));
    	Collections.sort(merged);
    	
    	String []userInputs = this.authorsHistory == null ? new String[0] : this.authorsHistory.getHistory();
    	for (int i = 0; i < userInputs.length; i++) {
    		if (!merged.contains(userInputs[i])) {
    			merged.add(userInputs[i]);
    		}
    	}
    	  		
    	return (String[])merged.toArray(new String[merged.size()]);
    }

}
