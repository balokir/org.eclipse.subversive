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
package org.eclipse.team.svn.revision.graph.graphic;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.draw2d.parts.Thumbnail;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class RevisionGraphOutlinePage extends Page implements IContentOutlinePage {

	protected EditPartViewer viewer;
	protected Canvas canvas;
	protected Thumbnail thumbnail;
	
	public RevisionGraphOutlinePage(EditPartViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void createControl(Composite parent) {
		//create canvas and light weight system
		this.canvas = new Canvas(parent, SWT.NONE);
		LightweightSystem lws = new LightweightSystem(this.canvas);
		
		ScalableRootEditPart rootEditPart = (ScalableRootEditPart) this.viewer.getRootEditPart();
		this.thumbnail = new ScrollableThumbnail((Viewport) rootEditPart.getFigure());
		this.thumbnail.setBorder(new MarginBorder(3));
		this.thumbnail.setSource(rootEditPart.getLayer(LayerConstants.PRINTABLE_LAYERS));
		lws.setContents(this.thumbnail);			
	}	
	
	public Control getControl() {
		return this.canvas;
	}
	
	@Override
	public void dispose() {
		if (this.thumbnail != null) {
			this.thumbnail.deactivate();
		}
		super.dispose();
	}
	
	public void setFocus() {
		if (getControl() != null)
			getControl().setFocus();
	}
	
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		
	}
	
	public ISelection getSelection() {
		return StructuredSelection.EMPTY;
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		
	}

	public void setSelection(ISelection selection) {	
		
	}
	
}

