/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.SpellcheckedTextProvider;
import org.eclipse.team.svn.ui.panel.IDialogManager;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.properties.bugtraq.BugtraqModel;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.CommentVerifier;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;

/**
 * Operation comment composite
 * 
 * @author Alexander Gurov
 */
public class CommentComposite extends Composite {
	public static String TEMPORARY_COMMENT = null;

	protected static final String COMMENT_HISTORY_NAME = "comment"; //$NON-NLS-1$

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

	protected int minLogSize;

	protected int maxLogWidth;

	protected IValidationManager validationManager;

	protected IDialogManager dialogManager;

	public CommentComposite(Composite parent, IValidationManager validationManager) {
		this(parent, validationManager, null);
	}

	public CommentComposite(Composite parent, IValidationManager validationManager, Set logTemplates) {
		this(parent, null, validationManager, logTemplates, null);
	}

	public CommentComposite(Composite parent, String message, IValidationManager validationManager, Set logTemplates, BugtraqModel bugtraqModel) {
		this(parent, null, validationManager, logTemplates, null, 0);
	}

	public CommentComposite(Composite parent, String message, IValidationManager validationManager, Set logTemplates, BugtraqModel bugtraqModel, int minLogSize) {
		this(parent, null, validationManager, logTemplates, null, minLogSize, 0);
	}

	public CommentComposite(Composite parent, String message, IValidationManager validationManager, Set logTemplates, BugtraqModel bugtraqModel, int minLogSize, int maxLogWidth) {
		super(parent, SWT.NONE);

		CommentComposite.PREVIOUS_COMMENTS_HEADER = SVNUIMessages.CommentComposite_Previous;
		CommentComposite.PREVIOUS_COMMENTS_HINT = "    " + SVNUIMessages.CommentComposite_Previous_Hint; //$NON-NLS-1$
		CommentComposite.TEMPLATE_HEADER = SVNUIMessages.CommentComposite_Template;
		CommentComposite.TEMPLATE_HINT = "    " + SVNUIMessages.CommentComposite_Template_Hint; //$NON-NLS-1$
		CommentComposite.TSVN_LOGTEMPLATE_HEADER = SVNUIMessages.CommentComposite_LogTemplate;
		CommentComposite.TSVN_LOGTEMPLATE_HINT = "    " + SVNUIMessages.CommentComposite_LogTemplate_Hint; //$NON-NLS-1$

		this.message = message;
		this.validationManager = validationManager;
		this.logTemplates = logTemplates;
		this.ignoredStrings = new HashSet();
		this.bugtraqModel = bugtraqModel;
		this.minLogSize = minLogSize;
		this.maxLogWidth = maxLogWidth;
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

		final Text []tBugIdTextA = new Text[1];
		if (this.bugtraqModel != null && (this.bugtraqModel.getMessage() != null || this.bugtraqModel.getLogregex() != null)) {
			Composite bugtraqComposite = new Composite(this, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginHeight = layout.marginWidth = 0;
			bugtraqComposite.setLayout(layout);

			bugtraqComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Label label = new Label(bugtraqComposite, SWT.NONE);
			label.setLayoutData(new GridData(GridData.BEGINNING));
			label.setText(this.bugtraqModel.getLabel());

			if (this.bugtraqModel.getLogregex() == null) {
				this.bugIdText = new Text(bugtraqComposite, SWT.FILL | SWT.BORDER);
				this.bugIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				this.bugIdText.setFocus();
	
				this.validationManager.attachTo(this.bugIdText, new AbstractVerifier() {
					protected String getErrorMessage(Control input) {
						String bugId = this.getText(input);
						if (bugId.length() > 0 && CommentComposite.this.bugtraqModel.isNumber() && !bugId.matches("[0-9]+(\\s*,\\s*?[0-9]+)*")) {
							return SVNUIMessages.format(SVNUIMessages.CommentComposite_BugID_Verifier_Error_Number,
									new String[] { CommentComposite.this.bugtraqModel.getLabel() });
						}
						return null;
					}
	
					protected String getWarningMessage(Control input) {
						if (CommentComposite.this.bugtraqModel.isWarnIfNoIssue() && this.getText(input).length() == 0) {
							return SVNUIMessages.CommentComposite_BugID_Verifier_Warning;
						}
						return null;
					}
				});
			}
			else {
				final Text tBugIdText = new Text(bugtraqComposite, SWT.FILL | SWT.BORDER | SWT.READ_ONLY);
				tBugIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				tBugIdTextA[0] = tBugIdText;
			}
		}
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 80;
		this.text = SpellcheckedTextProvider.getTextWidget(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP, data, this.maxLogWidth);
		this.text.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
	        	if (!SVNTeamPreferences.getCommentTemplatesBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.COMMENT_USE_SHIFT_ENTER_NAME) &&
	        		(e.stateMask & SWT.SHIFT) != 0 && e.detail == SWT.TRAVERSE_RETURN) {
		       		e.doit = false;
	        	}
	        	else if (e.character == SWT.TAB) {
					// no TABs are accepted as a text part
					e.doit = true;
				}
			}
		});
		CompositeVerifier verifier = new CompositeVerifier();
		verifier.add(new CommentVerifier(SVNUIMessages.CommentComposite_Comment_Verifier, this.minLogSize));
		if (this.bugtraqModel != null && this.bugtraqModel.getLogregex() != null) {
			this.text.setFocus();
			String []logregex = this.bugtraqModel.getLogregex();
			final Pattern mainRegex = Pattern.compile(logregex[0]);
			final Pattern numberRegex = logregex.length > 1 ? Pattern.compile(logregex[1]) : (this.bugtraqModel.isNumber() ? Pattern.compile("[0-9]+(\\s*,\\s*?[0-9]+)*") : null);
			verifier.add(new AbstractVerifier() {
				protected String getErrorMessage(Control input) {
					return null;
				}

				protected String getWarningMessage(Control input) {
					if (CommentComposite.this.bugtraqModel.isWarnIfNoIssue()) {
						String text = this.getText(input);
						Matcher matcher = mainRegex.matcher(text);
						if (matcher.find()) {
							String bugIdEntry = matcher.group();
							if (numberRegex != null) {
								matcher = numberRegex.matcher(bugIdEntry);
								String entryList = null;
								while (matcher.find()) {
									entryList = entryList == null ? matcher.group() : (entryList + ", " + matcher.group());
								}
								if (entryList != null) {
									tBugIdTextA[0].setText(entryList);
									return null;
								}
								tBugIdTextA[0].setText("");
								return SVNUIMessages.format(SVNUIMessages.CommentComposite_BugID_Verifier_Error_Text,
										new String[] { CommentComposite.this.bugtraqModel.getLabel(), numberRegex.pattern() });
							}
							tBugIdTextA[0].setText(bugIdEntry);
							return null;
						}
						tBugIdTextA[0].setText("");
						return SVNUIMessages.CommentComposite_BugID_Verifier_Warning;
					}
					return null;
				}
			});
		}
		this.validationManager.attachTo(this.text, verifier);

		Label label = new Label(this, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(SVNUIMessages.CommentComposite_ChooseComment);

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
			flattenCommentsList.add(FileUtility.flattenText((String) iter.next()));
		}
		previousCommentsCombo.setVisibleItemCount(flattenCommentsList.size());
		previousCommentsCombo.setItems((String[]) flattenCommentsList.toArray(new String[flattenCommentsList.size()]));
		previousCommentsCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int idx = previousCommentsCombo.getSelectionIndex();
				if (idx != -1) {
					String comboText = (String) commentsList.get(idx);
					CommentComposite.this.text.setText(CommentComposite.this.ignoredStrings.contains(comboText) ? CommentComposite.this.text.getText() : comboText);
				}
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
		if (this.minLogSize > 0) {
			this.validationManager.validateContent();
		}
	}

	protected List getCommentsList() {
		List commentsList = new ArrayList();

		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		boolean logTemplatesEnabled = SVNTeamPreferences.getCommentTemplatesBoolean(store, SVNTeamPreferences.COMMENT_LOG_TEMPLATES_ENABLED_NAME);
		boolean userTemplatesEnabled = SVNTeamPreferences.getCommentTemplatesBoolean(store, SVNTeamPreferences.COMMENT_TEMPLATES_LIST_ENABLED_NAME);

		if (logTemplatesEnabled && this.logTemplates != null || userTemplatesEnabled) {
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
			String[] templates = FileUtility.decodeStringToArray(SVNTeamPreferences.getCommentTemplatesString(store, SVNTeamPreferences.COMMENT_TEMPLATES_LIST_NAME));
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
				String mainTemplate = (String) this.logTemplates.iterator().next();
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