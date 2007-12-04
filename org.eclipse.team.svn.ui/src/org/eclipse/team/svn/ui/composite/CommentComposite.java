/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Alessandro Nistico - [patch] Change Set's implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.composite;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.panel.IDialogManager;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.properties.bugtraq.BugtraqModel;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.CommentVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

/**
 * Operation comment composite
 * 
 * @author Alexander Gurov
 */
public class CommentComposite extends Composite  {
	public static String TEMPORARY_COMMENT = null;
	protected static final String COMMENT_HISTORY_NAME = "comment";
	protected static String PREVIOUS_COMMENTS_HEADER;
	protected static String PREVIOUS_COMMENTS_HINT;
    protected static String TEMPLATE_HEADER;
    protected static String TEMPLATE_HINT;
    protected static String TSVN_LOGTEMPLATE_HEADER;
    protected static String TSVN_LOGTEMPLATE_HINT;
	
	protected StyledText text;
	protected Text bugIdText;
	protected String message;
	protected String bugID;
	protected UserInputHistory history;
	protected Set logTemplates;
	protected Set ignoredStrings;
	protected BugtraqModel bugtraqModel;
	
	protected IValidationManager validationManager;
	protected IDialogManager dialogManager;

    public CommentComposite(Composite parent, IValidationManager validationManager) {
        this(parent, validationManager, null);
    }
    
    public CommentComposite(Composite parent, IValidationManager validationManager, Set logTemplates) {
    	this(parent, null, validationManager, logTemplates, null);
    }
        
    public CommentComposite(Composite parent, String message, IValidationManager validationManager, Set logTemplates, BugtraqModel bugtraqModel) {
    	super(parent, SWT.NONE);
    	
    	CommentComposite.PREVIOUS_COMMENTS_HEADER = SVNTeamUIPlugin.instance().getResource("CommentComposite.Previous");
    	CommentComposite.PREVIOUS_COMMENTS_HINT = "    " + SVNTeamUIPlugin.instance().getResource("CommentComposite.Previous.Hint");
    	CommentComposite.TEMPLATE_HEADER = SVNTeamUIPlugin.instance().getResource("CommentComposite.Template");
        CommentComposite.TEMPLATE_HINT = "    " + SVNTeamUIPlugin.instance().getResource("CommentComposite.Template.Hint");
        CommentComposite.TSVN_LOGTEMPLATE_HEADER = SVNTeamUIPlugin.instance().getResource("CommentComposite.LogTemplate");
        CommentComposite.TSVN_LOGTEMPLATE_HINT = "    " + SVNTeamUIPlugin.instance().getResource("CommentComposite.LogTemplate.Hint");
    	
        this.message = message;
        this.validationManager = validationManager;
        this.logTemplates = logTemplates;
        this.ignoredStrings = new HashSet();
        this.bugtraqModel = bugtraqModel;
        this.createControls();
    }

	public String getMessage() {
		return this.message;
	}
	
	public void setMessage(String message) {
		this.text.setText(message);
	}

	public String getBugID() {
		return this.bugID;
	}
	
	public void insertText(String text) {
		this.text.insert(text);
	}
	
    public void saveChanges() {
		this.message = this.text.getText();
		this.history.addLine(this.message);
		CommentComposite.TEMPORARY_COMMENT = null;
		
		if (this.bugIdText != null) {
			this.bugID = this.bugIdText.getText();
		}
    }
    
    public void cancelChanges() {
    	CommentComposite.TEMPORARY_COMMENT = this.text.getText().trim().length() == 0 ? null : this.text.getText();
    }

    private void createControls() {
        GridData data = null;
        
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        this.setLayout(layout);
        
        if (this.bugtraqModel != null && this.bugtraqModel.getMessage() != null) {
        	Composite bugtraqComposite = new Composite(this, SWT.NONE);
        	layout = new GridLayout();
        	layout.numColumns = 2;
        	layout.marginHeight = layout.marginWidth = 0;
        	bugtraqComposite.setLayout(layout);
        	
        	bugtraqComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        	
        	Label label = new Label(bugtraqComposite, SWT.NONE);
        	label.setLayoutData(new GridData(GridData.BEGINNING));
        	label.setText(this.bugtraqModel.getLabel());
        	
        	this.bugIdText = new Text(bugtraqComposite, SWT.FILL | SWT.BORDER);
        	this.bugIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        	
        	this.validationManager.attachTo(this.bugIdText, new AbstractVerifier() {
				protected String getErrorMessage(Control input) {
					String logregex = CommentComposite.this.bugtraqModel.isNumber() ? "[0-9]+(,?[0-9]+)*" : CommentComposite.this.bugtraqModel.getLogregex()[0];
					if (logregex != null) {
						String bugId = this.getText(input);
						if (bugId.length() > 0 && !bugId.matches(logregex)) {
							if (CommentComposite.this.bugtraqModel.isNumber()) {
								return MessageFormat.format(SVNTeamUIPlugin.instance().getResource("CommentComposite.BugID.Verifier.Error.Number"), new String[] {CommentComposite.this.bugtraqModel.getLabel()});
							}
							else {
								return MessageFormat.format(SVNTeamUIPlugin.instance().getResource("CommentComposite.BugID.Verifier.Error.Text"), new String[] {CommentComposite.this.bugtraqModel.getLabel(), CommentComposite.this.bugtraqModel.getLogregex()[0]});
							}
						}
					}
					return null;
				}

				protected String getWarningMessage(Control input) {
					if (CommentComposite.this.bugtraqModel.isWarnIfNoIssue() && this.getText(input).length() == 0) {
						return SVNTeamUIPlugin.instance().getResource("CommentComposite.BugID.Verifier.Warning");
					}
					return null;
				}
        	});
        }
        
        AnnotationModel annotationModel = new AnnotationModel();
        IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
        
        SourceViewer sourceViewer = new SourceViewer(this, null, null, true, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
        sourceViewer.getTextWidget().setIndent(2);
        
        final SourceViewerDecorationSupport support = new SourceViewerDecorationSupport(sourceViewer, null, annotationAccess, EditorsUI.getSharedTextColors());
		Iterator e= new MarkerAnnotationPreferences().getAnnotationPreferences().iterator();
		while (e.hasNext())
			support.setAnnotationPreference((AnnotationPreference) e.next());
		
        support.install(EditorsUI.getPreferenceStore());
        
        sourceViewer.getTextWidget().addDisposeListener(new DisposeListener() {
		
			public void widgetDisposed(DisposeEvent e) {
				support.uninstall();
			}
		
		});
        
        Document document = new Document();
        sourceViewer.configure(new TextSourceViewerConfiguration(EditorsUI.getPreferenceStore()));
        sourceViewer.setDocument(document, annotationModel);
        this.text = sourceViewer.getTextWidget();
        data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 80;
		this.text.setLayoutData(data);
		this.text.selectAll();
		this.validationManager.attachTo(this.text, new CommentVerifier(SVNTeamUIPlugin.instance().getResource("CommentComposite.Comment.Verifier")));
				
		Label label = new Label(this, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(SVNTeamUIPlugin.instance().getResource("CommentComposite.ChooseComment"));
		
		this.history = new UserInputHistory(CommentComposite.COMMENT_HISTORY_NAME, SVNTeamPreferences.getCommentTemplatesInt(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.COMMENT_SAVED_COMMENTS_COUNT_NAME));
		
		final Combo previousCommentsCombo = new Combo(this, SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		previousCommentsCombo.setLayoutData(data);
		
		final List commentsList = this.getCommentsList();
		if (this.message != null && this.message.length() > 0) {
			this.text.setText(this.message);
		}
		else if (CommentComposite.TEMPORARY_COMMENT != null) {
			this.text.setText(CommentComposite.TEMPORARY_COMMENT);
		}
		this.text.selectAll();
		List flattenCommentsList = new ArrayList();
		for (Iterator iter = commentsList.iterator(); iter.hasNext();) {
			flattenCommentsList.add(FileUtility.flattenText((String)iter.next()));
		}
		previousCommentsCombo.setVisibleItemCount(flattenCommentsList.size());
		previousCommentsCombo.setItems((String[])flattenCommentsList.toArray(new String[flattenCommentsList.size()]));
		previousCommentsCombo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
            	String comboText = (String)commentsList.get(previousCommentsCombo.getSelectionIndex());
                CommentComposite.this.text.setText(CommentComposite.this.ignoredStrings.contains(comboText) ? 
                		CommentComposite.this.text.getText() : comboText);
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }
    
    public void postInit(IDialogManager dialogManager) {
    	this.dialogManager = dialogManager;
    	this.text.addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent event) {
        		if (event.keyCode == SWT.TAB) {
        			CommentComposite.this.text.traverse(SWT.TRAVERSE_TAB_NEXT);
        			event.doit = false;
        		}
        	}
		});
    }
    
    protected List getCommentsList() {
    	List commentsList = new ArrayList();
    	
    	IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		boolean logTemplatesEnabled = SVNTeamPreferences.getCommentTemplatesBoolean(store, SVNTeamPreferences.COMMENT_LOG_TEMPLATES_ENABLED_NAME);
		boolean userTemplatesEnabled = SVNTeamPreferences.getCommentTemplatesBoolean(store, SVNTeamPreferences.COMMENT_TEMPLATES_LIST_ENABLED_NAME);
		
		if ((logTemplatesEnabled && this.logTemplates != null) || userTemplatesEnabled) {
			commentsList.add(CommentComposite.PREVIOUS_COMMENTS_HEADER);
			this.ignoredStrings.add(CommentComposite.PREVIOUS_COMMENTS_HEADER);
			if (this.history.getHistory().length > 0) {
				commentsList.addAll(Arrays.asList(this.history.getHistory()));
			}
			else {
				commentsList.add(CommentComposite.PREVIOUS_COMMENTS_HINT);
				this.ignoredStrings.add(CommentComposite.PREVIOUS_COMMENTS_HINT);
			}
		}
		else {
			commentsList.addAll(Arrays.asList(this.history.getHistory()));
		}
		
		if (userTemplatesEnabled) {
			commentsList.add(CommentComposite.TEMPLATE_HEADER);
			this.ignoredStrings.add(CommentComposite.TEMPLATE_HEADER);
			String []templates = FileUtility.decodeStringToArray(SVNTeamPreferences.getCommentTemplatesString(store, 
					SVNTeamPreferences.COMMENT_TEMPLATES_LIST_NAME));
			if (templates.length == 0) {
				commentsList.add(CommentComposite.TEMPLATE_HINT);
				this.ignoredStrings.add(CommentComposite.TEMPLATE_HINT);
			}
			else {
				commentsList.addAll(new ArrayList(Arrays.asList(templates)));
			}
		}
		if (this.logTemplates != null && logTemplatesEnabled) {
			commentsList.add(CommentComposite.TSVN_LOGTEMPLATE_HEADER);
			this.ignoredStrings.add(CommentComposite.TSVN_LOGTEMPLATE_HEADER);
			if (this.logTemplates.size() > 0) {
				String mainTemplate = (String)this.logTemplates.iterator().next();
				this.text.setText(mainTemplate);
				this.text.selectAll();
				commentsList.addAll(this.logTemplates);
			}
			else {
				commentsList.add(CommentComposite.TSVN_LOGTEMPLATE_HINT);
				this.ignoredStrings.add(CommentComposite.TSVN_LOGTEMPLATE_HINT);
			}
		}
		
		return commentsList;
    }
    
}
