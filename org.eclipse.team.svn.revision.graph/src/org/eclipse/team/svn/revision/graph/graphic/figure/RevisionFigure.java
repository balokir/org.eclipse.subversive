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

import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.ParagraphTextLayout;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.PathRevision.ReviosionNodeType;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;

/**
 * Figure for revision node
 * 
 * TODO make correct background colors
 * 
 * TODO correctly work with system resources (see all places)
 * 
 * TODO customize colors
 * 
 * @author Igor Burilo
 */
public class RevisionFigure extends RoundedRectangle {

	protected final static int FIGURE_WIDTH = 200;
			
	protected final static Color trunkColor = new Color(null, 188, 255, 188);
	protected final static Color branchColor = new Color(null, 229, 255, 229);
	protected final static Color tagColor = new Color(null, 239, 252, 162);	
	protected final static Color createOrCopyColor = new Color(null, 229, 255, 229);	
	protected final static Color renameColor = new Color(null, 229, 229, 255);
	protected final static Color deleteColor = new Color(null, 255, 229, 229);	
	protected final static Color modifyColor = new Color(null, 229, 229, 229);
	protected final static Color otherColor = new Color(null, 255, 255, 255);
	
	protected final static Image trunkImg = SVNRevisionGraphPlugin.instance().getImageDescriptor("/icons/trunk.gif").createImage();
	protected final static Image branchImg = SVNRevisionGraphPlugin.instance().getImageDescriptor("/icons/branch.gif").createImage();
	protected final static Image tagImg = SVNRevisionGraphPlugin.instance().getImageDescriptor("/icons/tag.gif").createImage();
	protected final static Image addImg = SVNRevisionGraphPlugin.instance().getImageDescriptor("/icons/add.gif").createImage();
	protected final static Image deleteImg = SVNRevisionGraphPlugin.instance().getImageDescriptor("/icons/delete.gif").createImage();
	protected final static Image modifyImg = SVNRevisionGraphPlugin.instance().getImageDescriptor("/icons/modify.gif").createImage();
	protected final static Image renameImg = SVNRevisionGraphPlugin.instance().getImageDescriptor("/icons/rename.gif").createImage();
	protected final static Image otherImg = SVNRevisionGraphPlugin.instance().getImageDescriptor("/icons/other.gif").createImage();
	
	protected RevisionNode revisionNode;
	protected String path;
	
	protected Color originalBgColor;
	
	protected Label revisionFigure;
	protected TextFlow pathTextFlow;
	protected Label commentFigure;
	
	public RevisionFigure(RevisionNode revisionNode, String path) {
		this.revisionNode = revisionNode;
		this.path = path;
		
		this.createControls();								
		this.initControls();
		
		//non-transparent
		this.setOpaque(true);
	}
	
	protected void createControls() {		
		GridLayout layout = new GridLayout();		
		//layout.marginHeight = layout.marginWidth = 2;
		//layout.horizontalSpacing = layout.verticalSpacing = 3; 		
		this.setLayoutManager(layout);												
		
		this.revisionFigure = new Label();
		this.add(revisionFigure);
		GridData data = new GridData();
		data.horizontalAlignment = SWT.CENTER;
		data.grabExcessHorizontalSpace = true;
		layout.setConstraint(this.revisionFigure, data);		
		Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
		this.revisionFigure.setFont(boldFont);		

		//path
		if (this.revisionNode.getAction() == RevisionNodeAction.ADD || 
			this.revisionNode.getAction() == RevisionNodeAction.COPY ||
			this.revisionNode.getAction() == RevisionNodeAction.RENAME) {
			
			//wrap path using text layout
			FlowPage pathFlowPageFigure = new FlowPage();
			data = new GridData();
			data.widthHint = RevisionFigure.FIGURE_WIDTH - 10;
			data.horizontalAlignment = SWT.CENTER;
			data.grabExcessHorizontalSpace = true;
			layout.setConstraint(pathFlowPageFigure, data);
			
			this.pathTextFlow = new TextFlow();		
			this.pathTextFlow.setLayoutManager(new ParagraphTextLayout(this.pathTextFlow, ParagraphTextLayout.WORD_WRAP_SOFT));						
			pathFlowPageFigure.add(this.pathTextFlow);				
			this.add(pathFlowPageFigure);			
		}									
		
		//comment			
		this.commentFigure = new Label();
		this.add(commentFigure);
		data = new GridData();
		data.widthHint = RevisionFigure.FIGURE_WIDTH - 10;
		data.horizontalAlignment = SWT.BEGINNING;							
		layout.setConstraint(this.commentFigure, data);
		this.commentFigure.setLabelAlignment(PositionConstants.LEFT);
	}			
	
	protected void initControls() {
		this.revisionFigure.setText(String.valueOf(this.revisionNode.getRevision()));
		
		if (this.pathTextFlow != null) {
			this.pathTextFlow.setText(this.path);	
		}				

		String comment = this.revisionNode.getMessage();
		if (comment != null && comment.length() > 0) {
			comment = comment.replaceAll("\r\n|\r|\n", " ");
		} else {
			comment = "[no comment]";
		}					
		this.commentFigure.setText(comment);
		
		//init color and node icon		
	    Color color = RevisionFigure.getRevisionNodeColor(this.revisionNode);
	    Image nodeIcon = RevisionFigure.getRevisionNodeIcon(this.revisionNode);	    		
		this.setBackgroundColor(this.originalBgColor = color);				
		this.revisionFigure.setIcon(nodeIcon);					
	}
	
	public void init() {
		this.setPreferredSize(RevisionFigure.FIGURE_WIDTH, this.getPreferredSize().height);
	}
	
	//TODO make correct selection presentation
	public void setSelected(boolean isSelected) {
		if (isSelected) {
			this.setBackgroundColor(org.eclipse.draw2d.ColorConstants.blue);			
		} else {
			this.setBackgroundColor(this.originalBgColor);
		}		
	}	
	
	public RevisionNode getRevisionNode() {
		return this.revisionNode;
	}
	
	public static Color getRevisionNodeColor(RevisionNode revisionNode) {
	    ReviosionNodeType type = revisionNode.getType();
		RevisionNodeAction action = revisionNode.getAction();		
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
		return color;
	}
	
	public static Image getRevisionNodeIcon(RevisionNode revisionNode) {
	    ReviosionNodeType type = revisionNode.getType();
		RevisionNodeAction action = revisionNode.getAction();			    
	    Image nodeIcon = null;
		if (ReviosionNodeType.TRUNK.equals(type)) {			
			nodeIcon = trunkImg;
		} else if (ReviosionNodeType.BRANCH.equals(type)) {
			nodeIcon = branchImg;
		} else if (ReviosionNodeType.TAG.equals(type)) {
			nodeIcon = tagImg;
		} else if (RevisionNodeAction.ADD.equals(action) || RevisionNodeAction.COPY.equals(action)) {
			nodeIcon = addImg;
		} else if (RevisionNodeAction.RENAME.equals(action)) {
			nodeIcon = renameImg;
		} else if (RevisionNodeAction.DELETE.equals(action)) {
			nodeIcon = deleteImg;
		} else if (RevisionNodeAction.MODIFY.equals(action)) {
			nodeIcon = modifyImg;
		} else {
			nodeIcon = otherImg;
		}
		return nodeIcon;
	}
}
