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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;

/**
 * 
 * @author Igor Burilo
 */
public class RevisionFigure extends RoundedRectangle {

	protected final static int FIGURE_WIDTH = 150;
	protected final static int LINE_HEIGHT = 20;
	
	protected Label revisionLabel;
	protected PathsFigure pathsFigure;
	
	public RevisionFigure() {
		ToolbarLayout layout = new ToolbarLayout();
		layout.setVertical(true);
		
		//layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);				
		
		//non-transparent figure
		this.setOpaque(true); 
		//layout.setStretchMinorAxis(true);
		//setBorder(new LineBorder(ColorConstants.black, 1));
		this.setLayoutManager(layout);
		
				
		this.revisionLabel = new Label();		    
	    this.add(this.revisionLabel);
		
	    this.pathsFigure = new PathsFigure();	    
	    this.add(this.pathsFigure);	    	    	    	    
	}
	
	public void init(long revision, String path) {		
		this.revisionLabel.setText(String.valueOf(revision));
		
		//set path
		List<String> pathParts = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(path, "/", false);
		while (tokenizer.hasMoreTokens()) {
			pathParts.add("/" + tokenizer.nextToken());
		}		
		for (String pathPart : pathParts) {
			Label label = new Label(pathPart);			
			this.pathsFigure.add(label);						
		}
		
		this.setPreferredSize(RevisionFigure.FIGURE_WIDTH, RevisionFigure.LINE_HEIGHT * pathParts.size() + 1);
	}		
}
