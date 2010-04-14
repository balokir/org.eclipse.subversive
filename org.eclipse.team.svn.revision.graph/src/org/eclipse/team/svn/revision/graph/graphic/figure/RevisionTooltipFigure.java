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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.ui.utility.DateFormatter;

/**
 * Tooltip for revision node
 * 
 * @author Igor Burilo
 */
public class RevisionTooltipFigure extends Figure {

	protected final RevisionNode revisionNode;
	
	protected Label pathText;
	protected Label authorText;
	protected Label dateText;
	protected Label copyText;
	protected Label commentText;
	
	public RevisionTooltipFigure(RevisionNode revisionNode) {
		this.revisionNode = revisionNode;
		
		this.createControls();
		this.initControls();
				
		this.setBorder(new LineBorder(ColorConstants.white));
	}
	
	protected void createControls() {
		ToolbarLayout parentLayout = new ToolbarLayout();
		this.setLayoutManager(parentLayout);
		
		Figure parent = new Figure();
		this.add(parent);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayoutManager(layout);
		
		this.pathText = new Label();
		parent.add(this.pathText);
		GridData data = new GridData();
		data.horizontalAlignment = SWT.LEFT;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		layout.setConstraint(this.pathText, data);
		Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
		this.pathText.setFont(boldFont);
		
		//author
		Label authorLabel = new Label(SVNRevisionGraphMessages.RevisionTooltipFigure_Author);
		parent.add(authorLabel);
		data = new GridData();
		layout.setConstraint(authorLabel, data);
		
		this.authorText = new Label();
		parent.add(this.authorText);
		layout.setConstraint(this.authorText, new GridData());
		
		//date
		Label dateLabel = new Label(SVNRevisionGraphMessages.RevisionTooltipFigure_Date);
		parent.add(dateLabel);
		data = new GridData();
		layout.setConstraint(dateLabel, data);
		
		this.dateText = new Label();
		parent.add(this.dateText);
		layout.setConstraint(this.dateText, new GridData());
		
		//copied from
		if (this.revisionNode.getCopiedFrom() != null) {
			Label copyLabel = new Label(SVNRevisionGraphMessages.RevisionTooltipFigure_CopiedFrom);
			parent.add(copyLabel);
			data = new GridData();
			layout.setConstraint(copyLabel, data);
			
			this.copyText = new Label();
			parent.add(this.copyText);
			layout.setConstraint(this.copyText, new GridData());	
		}
		
		//comment
		Label commentLabel = new Label(SVNRevisionGraphMessages.RevisionTooltipFigure_Comment);
		parent.add(commentLabel);
		data = new GridData();
		data.horizontalAlignment = SWT.LEFT;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		layout.setConstraint(commentLabel, data);
		
		this.commentText = new Label();
		parent.add(this.commentText);
		data = new GridData();
		data.horizontalAlignment = SWT.LEFT;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		layout.setConstraint(this.commentText, data); 
	}
	
	protected void initControls() {
		this.pathText.setIcon(RevisionFigure.getRevisionNodeIcon(this.revisionNode));
		this.pathText.setText(this.revisionNode.getPath() + "@" + this.revisionNode.getRevision()); //$NON-NLS-1$
		
		String author = this.revisionNode.getAuthor();
		this.authorText.setText(author == null || author.length() == 0 ? SVNMessages.SVNInfo_NoAuthor : author);
		
		long date = this.revisionNode.getDate(); 
		this.dateText.setText(date == 0 ? SVNMessages.SVNInfo_NoDate : DateFormatter.formatDate(date));
		
		if (this.revisionNode.getCopiedFrom() != null) {
			RevisionNode copiedFrom = this.revisionNode.getCopiedFrom();
			this.copyText.setText(copiedFrom.getPath() + "@" + copiedFrom.getRevision()); //$NON-NLS-1$
		}
		
		String comment = this.revisionNode.getMessage();
		this.commentText.setText(comment == null || comment.length() == 0 ? SVNMessages.SVNInfo_NoComment : comment);
	}

}
