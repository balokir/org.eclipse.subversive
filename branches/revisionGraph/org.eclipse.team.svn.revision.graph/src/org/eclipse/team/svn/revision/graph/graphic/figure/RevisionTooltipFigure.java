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

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;

/**
 * TODO make correct implementation
 * 
 * @author Igor Burilo
 */
public class RevisionTooltipFigure extends RectangleFigure {

	protected RevisionNode revisionNode;
	
	public RevisionTooltipFigure(RevisionNode revisionNode) {
		this.revisionNode = revisionNode;
		
		/*
		 * TODO 
		 * 
		 * make scrollable
		 * add shadow
		 * use font types: bold, italic
		 * insets
		 */
		
		Color bColor = new Color(null, 255, 255, 225);
		
		ToolbarLayout layout = new ToolbarLayout(false);
		layout.setSpacing(2);		
		this.setLayoutManager(layout);
		
		this.setBackgroundColor(bColor);
		this.setOpaque(true);
		
		Label label = new Label("Revision: 2688 (tagged)");
		this.add(label);
		
		label = new Label("URL: /tags/0.10.2"); 
		this.add(label);
		
		label = new Label("Copied from: /trunk@587"); 
		this.add(label);
		
		label = new Label("Author: guest"); 
		this.add(label);
		
		label = new Label("Date: 25 June 2008"); 
		this.add(label);
		
		this.add(new Label(""));
		
		label = new Label("Comment:"); 
		this.add(label);				
		
		label = new Label("make correct correct connection make correct correct connection\r\n make correct correct connection"); 
		this.add(label);
								
		this.setBorder(new LineBorder(bColor));
	}
}
