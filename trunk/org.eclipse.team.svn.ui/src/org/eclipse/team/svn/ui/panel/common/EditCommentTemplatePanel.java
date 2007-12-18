/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.common;

import java.util.Iterator;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

/**
 * Edit comment templates panel
 *
 * @author Sergiy Logvin
 */
public class EditCommentTemplatePanel extends AbstractDialogPanel {
	
	protected StyledText templateText;
	protected String template;
	
	public EditCommentTemplatePanel(String template) {
		super();
		this.dialogTitle = SVNTeamUIPlugin.instance().getResource(template == null ? "EditCommentTemplatePanel.Title.New" : "EditCommentTemplatePanel.Title.edit");
		this.defaultMessage = SVNTeamUIPlugin.instance().getResource("EditCommentTemplatePanel.Message");
		this.dialogDescription = SVNTeamUIPlugin.instance().getResource("EditCommentTemplatePanel.Description");
		this.template = template;
	}
	
	public void createControlsImpl(Composite parent) {
		AnnotationModel annotationModel = new AnnotationModel();
        IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
        
        SourceViewer sourceViewer = new SourceViewer(parent, null, null, true, SWT.BORDER | SWT.MULTI);
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
        this.templateText = sourceViewer.getTextWidget();
	
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
		data.heightHint = 180;
		this.templateText.setLayoutData(data);
		this.templateText.setText(this.template == null ? "" : this.template);
		this.templateText.selectAll();
		this.attachTo(this.templateText, new NonEmptyFieldVerifier(SVNTeamUIPlugin.instance().getResource("EditCommentTemplatePanel.Tempalte.Verifier")));
	}
	
	public String getTemplate() {
		return this.template;
	}

	protected void saveChangesImpl() {
		this.template = this.templateText.getText().trim();
	}

	protected void cancelChangesImpl() {
	}

}
