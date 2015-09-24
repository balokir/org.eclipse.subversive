/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

/**
 * Standard spell checked text widget used in plug-in provider.
 * 
 * @author Alexei Goncharov
 */
public class SpellcheckedTextProvider {

	public static StyledText getTextWidget(Composite parent, Object layoutData, int style) {
		return SpellcheckedTextProvider.getTextWidget(parent, style, layoutData, 0);
	}

	public static StyledText getTextWidget(Composite parent, int style, Object layoutData, int widthMarker) {
		Composite offset = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginRight = 0;
		layout.marginLeft = 4;
		layout.horizontalSpacing = 0;
		offset.setLayout(layout);
		offset.setLayoutData(layoutData);

		final SourceViewer sourceViewer = new SourceViewer(offset, null, null, true, style & ~SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		sourceViewer.getTextWidget().setLayoutData(data);
		offset.setBackground(sourceViewer.getTextWidget().getBackground());

		AnnotationModel annotationModel = new AnnotationModel();
		IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
		final SourceViewerDecorationSupport support = new SourceViewerDecorationSupport(sourceViewer, null, annotationAccess, EditorsUI.getSharedTextColors());
		for (Iterator<?> e = new MarkerAnnotationPreferences().getAnnotationPreferences().iterator(); e.hasNext();) {
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
		
		sourceViewer.getTextWidget().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 'z' && e.stateMask == SWT.CONTROL) {
					if (sourceViewer.canDoOperation(SourceViewer.UNDO)) {
						sourceViewer.doOperation(SourceViewer.UNDO);
					}
				}
				else if (e.keyCode == 'y' && e.stateMask == SWT.CONTROL) {
					if (sourceViewer.canDoOperation(SourceViewer.REDO)) {
						sourceViewer.doOperation(SourceViewer.REDO);
					}
				}
			}
		});
		
		return sourceViewer.getTextWidget();
	}

}
