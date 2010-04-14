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

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.SchemeBorder;
import org.eclipse.draw2d.SchemeBorder.Scheme;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.ParagraphTextLayout;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.PathRevision.ReviosionNodeType;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Figure for revision node
 * 
 * @author Igor Burilo
 */
public class RevisionFigure extends Figure {

	protected final static int FIGURE_WIDTH = 200;
			
	public final static Color TRUNK_COLOR;
	public final static Color BRANCH_COLOR;
	public final static Color TAG_COLOR;	
	public final static Color CREATE_OR_COPY_COLOR;	
	public final static Color RENAME_COLOR;
	public final static Color DELETE_COLOR;	
	public final static Color MODIFY_COLOR;
	public final static Color OTHER_COLOR;
	public final static Color SELECTED_COLOR;
	
	public final static Image TRUNK_IMAGE;
	public final static Image BRANCH_IMAGE;
	public final static Image TAG_IMAGE;
	public final static Image ADD_IMAGE;
	public final static Image DELETE_IMAGE;
	public final static Image MODIFY_IMAGE;
	public final static Image RENAME_IMAGE;
	public final static Image OTHER_IMAGE;
	
	protected RevisionNode revisionNode;
	protected String path;
	
	protected Color originalBgColor;
	protected Border originalBorder;
	
	protected Label revisionFigure;
	protected TextFlow pathTextFlow;
	protected Label commentFigure;
	
	static {
		//images
		TRUNK_IMAGE = SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/trunk.gif").createImage(); //$NON-NLS-1$
		SVNRevisionGraphPlugin.disposeOnShutdown(TRUNK_IMAGE);
		
		BRANCH_IMAGE = SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/branch.gif").createImage(); //$NON-NLS-1$
		SVNRevisionGraphPlugin.disposeOnShutdown(BRANCH_IMAGE);
		
		TAG_IMAGE = SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/tag.gif").createImage(); //$NON-NLS-1$
		SVNRevisionGraphPlugin.disposeOnShutdown(TAG_IMAGE);
		
		ADD_IMAGE = SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/add.gif").createImage(); //$NON-NLS-1$
		SVNRevisionGraphPlugin.disposeOnShutdown(ADD_IMAGE);
		
		DELETE_IMAGE = SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/delete.gif").createImage(); //$NON-NLS-1$
		SVNRevisionGraphPlugin.disposeOnShutdown(DELETE_IMAGE);
		
		MODIFY_IMAGE = SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/modify.gif").createImage(); //$NON-NLS-1$
		SVNRevisionGraphPlugin.disposeOnShutdown(MODIFY_IMAGE);
		
		RENAME_IMAGE = SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/rename.gif").createImage(); //$NON-NLS-1$
		SVNRevisionGraphPlugin.disposeOnShutdown(RENAME_IMAGE);
		
		OTHER_IMAGE = SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/other.gif").createImage(); //$NON-NLS-1$
		SVNRevisionGraphPlugin.disposeOnShutdown(OTHER_IMAGE);
		
		//colors
		TRUNK_COLOR = new Color(UIMonitorUtility.getDisplay(), 0, 255, 128);
		SVNRevisionGraphPlugin.disposeOnShutdown(TRUNK_COLOR);
		
		BRANCH_COLOR = new Color(UIMonitorUtility.getDisplay(), 84, 159, 252);
		SVNRevisionGraphPlugin.disposeOnShutdown(BRANCH_COLOR);
		
		TAG_COLOR = new Color(UIMonitorUtility.getDisplay(), 255, 255, 128);
		SVNRevisionGraphPlugin.disposeOnShutdown(TAG_COLOR);
		
		CREATE_OR_COPY_COLOR = new Color(UIMonitorUtility.getDisplay(), 198, 255, 198);
		SVNRevisionGraphPlugin.disposeOnShutdown(CREATE_OR_COPY_COLOR);
		
		RENAME_COLOR = new Color(UIMonitorUtility.getDisplay(), 128, 128, 255);
		SVNRevisionGraphPlugin.disposeOnShutdown(RENAME_COLOR);
		
		DELETE_COLOR = new Color(UIMonitorUtility.getDisplay(), 255, 140, 140);
		SVNRevisionGraphPlugin.disposeOnShutdown(DELETE_COLOR);
		
		MODIFY_COLOR = new Color(UIMonitorUtility.getDisplay(), 192, 192, 192);
		SVNRevisionGraphPlugin.disposeOnShutdown(MODIFY_COLOR);
		
		OTHER_COLOR = new Color(UIMonitorUtility.getDisplay(), 245, 245, 245);
		SVNRevisionGraphPlugin.disposeOnShutdown(OTHER_COLOR);
		
		SELECTED_COLOR = new Color(UIMonitorUtility.getDisplay(), 47, 104, 200);
		SVNRevisionGraphPlugin.disposeOnShutdown(SELECTED_COLOR);
	}
	
	public RevisionFigure(RevisionNode revisionNode, String path) {
		this.revisionNode = revisionNode;
		this.path = path;
		
		this.createControls();								
		this.initControls();
						
		this.setBorder(this.originalBorder = new SchemeBorder(new Scheme(
				new Color[]{ColorConstants.button},
				new Color[]{ColorConstants.buttonDarkest})));
		
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
			comment = comment.replaceAll("\r\n|\r|\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			comment = SVNMessages.SVNInfo_NoComment;
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
		
	public void setSelected(boolean isSelected) {
		if (isSelected) {
			this.setBackgroundColor(SELECTED_COLOR);
			this.setForegroundColor(ColorConstants.white);
			//this.setBorder(new FocusBorder());
		} else {
			this.setBackgroundColor(this.originalBgColor);
			this.setForegroundColor(ColorConstants.black);
			//this.setBorder(this.originalBorder);
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
			color = TRUNK_COLOR;			
		} else if (ReviosionNodeType.BRANCH.equals(type)) {
			color = BRANCH_COLOR;
		} else if (ReviosionNodeType.TAG.equals(type)) {
			color = TAG_COLOR;
		} else if (RevisionNodeAction.ADD.equals(action) || RevisionNodeAction.COPY.equals(action)) {
			color = CREATE_OR_COPY_COLOR;
		} else if (RevisionNodeAction.RENAME.equals(action)) {
			color = RENAME_COLOR;
		} else if (RevisionNodeAction.DELETE.equals(action)) {
			color = DELETE_COLOR;
		} else if (RevisionNodeAction.MODIFY.equals(action)) {
			color = MODIFY_COLOR;
		} else {
			color = OTHER_COLOR;
		}
		return color;
	}
	
	public static Image getRevisionNodeIcon(RevisionNode revisionNode) {
	    ReviosionNodeType type = revisionNode.getType();
		RevisionNodeAction action = revisionNode.getAction();			    
	    Image nodeIcon = null;
		if (ReviosionNodeType.TRUNK.equals(type)) {			
			nodeIcon = TRUNK_IMAGE;
		} else if (ReviosionNodeType.BRANCH.equals(type)) {
			nodeIcon = BRANCH_IMAGE;
		} else if (ReviosionNodeType.TAG.equals(type)) {
			nodeIcon = TAG_IMAGE;
		} else if (RevisionNodeAction.ADD.equals(action) || RevisionNodeAction.COPY.equals(action)) {
			nodeIcon = ADD_IMAGE;
		} else if (RevisionNodeAction.RENAME.equals(action)) {
			nodeIcon = RENAME_IMAGE;
		} else if (RevisionNodeAction.DELETE.equals(action)) {
			nodeIcon = DELETE_IMAGE;
		} else if (RevisionNodeAction.MODIFY.equals(action)) {
			nodeIcon = MODIFY_IMAGE;
		} else {
			nodeIcon = OTHER_IMAGE;
		}
		return nodeIcon;
	}
}
