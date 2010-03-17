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
import org.eclipse.swt.graphics.Color;
import org.eclipse.team.svn.revision.graph.PathRevision.ReviosionNodeType;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;

/**
 * 
 * @author Igor Burilo
 */
public class RevisionFigure extends RoundedRectangle {

	protected final static int FIGURE_WIDTH = 150;
	protected final static int LINE_HEIGHT = 20;
	
	//TODO correctly work with system resources
	protected final static Color trunkColor = new Color(null, 188, 255, 188);
	protected final static Color branchColor = new Color(null, 229, 255, 229);
	protected final static Color tagColor = new Color(null, 239, 252, 162);	
	protected final static Color createOrCopyColor = new Color(null, 229, 255, 229);	
	protected final static Color renameColor = new Color(null, 229, 229, 255);
	protected final static Color deleteColor = new Color(null, 255, 229, 229);	
	protected final static Color modifyColor = new Color(null, 229, 229, 229);
	protected final static Color otherColor = new Color(null, 255, 255, 255);
	
	protected RevisionNode revisionNode;
		
	protected Label revisionLabel;
	protected PathsFigure pathsFigure;
	
	/*
	 * TODO should be removed when correct collapse/expand
	 * visualization is implemented
	 */
	protected Label expandCollapseStatusLabel;
	
	protected Color originalBgColor;
	
	public RevisionFigure(RevisionNode revisionNode) {
		this.revisionNode = revisionNode;
		
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
	    	   
	    this.expandCollapseStatusLabel = new Label();
	    this.add(this.expandCollapseStatusLabel);
	    
	    //init color
	    ReviosionNodeType type = revisionNode.pathRevision.type;
		RevisionNodeAction action = revisionNode.pathRevision.action;		
	    Color color;
		if (ReviosionNodeType.TRUNK.equals(type)) {
			color = trunkColor;			
		} else if (ReviosionNodeType.BRANCH.equals(type)) {
			color = branchColor;
		} else if (ReviosionNodeType.TAG.equals(type)) {
			color = tagColor;
		} else if (RevisionNodeAction.ADD.equals(action) || RevisionNodeAction.COPY.equals(action)) {
			color = createOrCopyColor;
		} else if (RevisionNodeAction.RENAME.equals(action)) {
			color = renameColor;
		} else if (RevisionNodeAction.DELETE.equals(action)) {
			color = deleteColor;			
		} else if (RevisionNodeAction.MODIFY.equals(action)) {
			color = modifyColor;
		} else {
			color = otherColor;
		}		
		this.setBackgroundColor(this.originalBgColor = color);			
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
		
		this.updateExpandCollapseStatus();
		
		this.setPreferredSize(RevisionFigure.FIGURE_WIDTH, RevisionFigure.LINE_HEIGHT * pathParts.size() + 1 
				/*TODO delete after removing expandCollapseStatus*/+ RevisionFigure.LINE_HEIGHT);
	}
	
	public void updateExpandCollapseStatus() {					
		String status =
			/*"next: " +*/ "T" + this.getCollapseString(this.revisionNode.isNextCollapsed()) + " " + 
			/*"copy_to: " +*/ "R" + this.getCollapseString(this.revisionNode.isCopiedToCollapsed()) + " " +
			/*"previous: " +*/ "B" + this.getCollapseString(this.revisionNode.isPreviousCollapsed()) + " " +			
			/*"copy_from: " +*/ "L" + this.getCollapseString(this.revisionNode.isCopiedFromCollapsed());						
			
			this.expandCollapseStatusLabel.setText(status);		
	}
	
	protected String getCollapseString(boolean isCollapsed) {
		return isCollapsed ? "+" : "-";
	} 

	public void setSelected(boolean isSelected) {
		if (isSelected) {
			this.setBackgroundColor(org.eclipse.draw2d.ColorConstants.blue);
		} else {
			this.setBackgroundColor(this.originalBgColor);
		}		
	}	
}
