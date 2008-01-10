/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui;

import java.util.Iterator;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.MarginPainter;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

/**
 * Standart spellchecked text widget used in plug-in provider.
 * 
 * @author Alexei Goncharov
 */
public class SpellcheckedTextProvider {
	
	public static StyledText getTextWidget(Composite parent, int style) {
		return SpellcheckedTextProvider.getTextWidget(parent, style, 0);
	}
	
	public static StyledText getTextWidget(Composite parent, int style, int widthMarker){
		AnnotationModel annotationModel = new AnnotationModel();
        IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
        SourceViewer sourceViewer = new SourceViewer(parent, null, null, true, style);
        final SourceViewerDecorationSupport support = new SourceViewerDecorationSupport(sourceViewer, null, annotationAccess, EditorsUI.getSharedTextColors());
		Iterator e= new MarkerAnnotationPreferences().getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			support.setAnnotationPreference((AnnotationPreference) e.next());
		}
        support.install(EditorsUI.getPreferenceStore());
        sourceViewer.getTextWidget().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				support.uninstall();
			}
		});        
        Document document = new Document();
        sourceViewer.configure(new TextSourceViewerConfiguration(EditorsUI.getPreferenceStore()));
        sourceViewer.setDocument(document, annotationModel);
        if (widthMarker > 0) {
        	MarginPainter painter = new MarginPainter(sourceViewer);
        	painter.setMarginRulerColumn(widthMarker);
        	painter.setMarginRulerColor(parent.getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
            sourceViewer.addPainter(painter);
        }
        sourceViewer.getTextWidget().setIndent(0);
        return sourceViewer.getTextWidget();
	}

}
