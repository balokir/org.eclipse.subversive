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
package org.eclipse.team.svn.revision.graph.graphic.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;

/** 
 * Add expand/collapse decoration to main figure
 * 
 * TODO make correct controls
 * 
 * TODO make active actions; add UI feedback etc.
 * 
 * TODO take into account visual connections to place expand/collapse but not model
 * 
 * @author Igor Burilo
 */
public class ExpandCollapseDecorationFigure extends Figure {

	protected static Image minusImage = SVNRevisionGraphPlugin.instance().getImageDescriptor("/icons/minus.gif").createImage(); 
	protected static Image plusImage = SVNRevisionGraphPlugin.instance().getImageDescriptor("/icons/plus.gif").createImage();
	
	protected final RevisionNode revisionNode;
	
	protected ImageFigure topFigure;
	protected ImageFigure leftFigure;
	protected ImageFigure rightFigure;
	protected ImageFigure bottomFigure;
	
	public ExpandCollapseDecorationFigure(RevisionNode revisionNode) {
		this.revisionNode = revisionNode;
		
		this.createControls();
		this.initControls();
				
		//make transparent
		this.setOpaque(false);
	}
	
	protected void createControls() {
		GridLayout layout = new GridLayout();		
		layout.numColumns = 3;		
		layout.marginHeight = layout.marginWidth = 2;
		layout.horizontalSpacing = layout.verticalSpacing = 3;
			
		this.setLayoutManager(layout);
						
		//top
		this.topFigure = new ImageFigure();
		this.add(this.topFigure);
		GridData data = new GridData();
		data.horizontalAlignment = SWT.CENTER;
		data.horizontalSpan = 3;
		layout.setConstraint(this.topFigure, data);		
		
		//left
		this.leftFigure = new ImageFigure();
		this.add(this.leftFigure);
		data = new GridData();
		data.verticalAlignment = SWT.CENTER;
		layout.setConstraint(this.leftFigure, data);		
						
		//content
		Figure contentFigure = new Figure();
		this.add(contentFigure);	
		data = new GridData(SWT.FILL, SWT.FILL, true, true);		
		layout.setConstraint(contentFigure, data);				
		
		//right
		this.rightFigure = new ImageFigure();
		this.add(this.rightFigure);
		data = new GridData();
		data.verticalAlignment = SWT.CENTER;
		layout.setConstraint(this.rightFigure, data);				

		//bottom
		this.bottomFigure = new ImageFigure();
		this.add(this.bottomFigure);
		data = new GridData();
		data.horizontalAlignment = SWT.CENTER;
		data.horizontalSpan = 3;
		layout.setConstraint(this.bottomFigure, data);
	}
	
	public void update() {
		this.initControls();
	}
	
	protected void initControls() {
		//TODO handle rename
		
		Image icon = null;
		if (this.revisionNode.isNextCollapsed()) {
			icon = ExpandCollapseDecorationFigure.plusImage;
		} else if (this.revisionNode.getNext() != null) {
			icon = ExpandCollapseDecorationFigure.minusImage;
		} else {
			icon = null;
		}
		this.topFigure.setImage(icon);
			
		
		if (this.revisionNode.isCopiedToCollapsed()) {
			icon = ExpandCollapseDecorationFigure.plusImage;	
		} else if (this.revisionNode.getCopiedTo().length > 0) {
			icon = ExpandCollapseDecorationFigure.minusImage;	
		} else {
			icon = null;
		}
		this.rightFigure.setImage(icon);
				
		if (this.revisionNode.isCopiedFromCollapsed()) {
			icon = ExpandCollapseDecorationFigure.plusImage;	
		} else if (this.revisionNode.getCopiedFrom() != null) {
			icon = ExpandCollapseDecorationFigure.minusImage;
		} else {
			icon = null;
		}
		this.leftFigure.setImage(icon);
				
		if (this.revisionNode.isPreviousCollapsed()) {
			icon = ExpandCollapseDecorationFigure.plusImage;	
		} else if (this.revisionNode.getPrevious() != null) {
			icon = ExpandCollapseDecorationFigure.minusImage;
		} else {
			icon = null;
		}
		this.bottomFigure.setImage(icon);
							
		//TODO repaint ?
	}
}
