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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import org.eclipse.team.svn.revision.graph.graphic.NodeMergeData;
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
		
	protected Label incomingMergeText;
	protected Label outgoingMergeText;
	
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
		
		Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
		
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
		this.pathText.setFont(boldFont);
				
		//author
		Label authorLabel = new Label(SVNRevisionGraphMessages.RevisionTooltipFigure_Author);
		parent.add(authorLabel);
		data = new GridData();
		layout.setConstraint(authorLabel, data);
		authorLabel.setFont(boldFont);
		
		this.authorText = new Label();
		parent.add(this.authorText);
		layout.setConstraint(this.authorText, new GridData());
		
		//date
		Label dateLabel = new Label(SVNRevisionGraphMessages.RevisionTooltipFigure_Date);
		parent.add(dateLabel);
		data = new GridData();
		layout.setConstraint(dateLabel, data);
		dateLabel.setFont(boldFont);		
		
		this.dateText = new Label();
		parent.add(this.dateText);
		layout.setConstraint(this.dateText, new GridData());
		
		//copied from
		if (this.revisionNode.getCopiedFrom() != null) {
			Label copyLabel = new Label(SVNRevisionGraphMessages.RevisionTooltipFigure_CopiedFrom);
			parent.add(copyLabel);
			data = new GridData();
			layout.setConstraint(copyLabel, data);
			copyLabel.setFont(boldFont);
			
			this.copyText = new Label();
			parent.add(this.copyText);
			layout.setConstraint(this.copyText, new GridData());	
		}
		
		//incoming merge
		if (this.revisionNode.hasIncomingMerges()) {
			Label mergedFromLabel = new Label(SVNRevisionGraphMessages.RevisionTooltipFigure_IncomingMerge);
			parent.add(mergedFromLabel);
			data = new GridData();
			data.horizontalAlignment = SWT.LEFT;
			data.grabExcessHorizontalSpace = true;
			data.horizontalSpan = 2;
			layout.setConstraint(mergedFromLabel, data);			
			mergedFromLabel.setFont(boldFont);
			
			this.incomingMergeText = new Label();
			parent.add(this.incomingMergeText);
			data = new GridData();
			data.horizontalAlignment = SWT.LEFT;
			data.grabExcessHorizontalSpace = true;
			data.horizontalSpan = 2;
			layout.setConstraint(this.incomingMergeText, data);
		}
		
		//outgoing merge
		if (this.revisionNode.hasOutgoingMerges()) {
			Label mergedToLabel = new Label(SVNRevisionGraphMessages.RevisionTooltipFigure_OutgoingMerge);
			parent.add(mergedToLabel);
			data = new GridData();
			data.horizontalAlignment = SWT.LEFT;
			data.grabExcessHorizontalSpace = true;
			data.horizontalSpan = 2;
			layout.setConstraint(mergedToLabel, data);
			mergedToLabel.setFont(boldFont);			
			
			this.outgoingMergeText = new Label();
			parent.add(this.outgoingMergeText);
			data = new GridData();
			data.horizontalAlignment = SWT.LEFT;
			data.grabExcessHorizontalSpace = true;
			data.horizontalSpan = 2;
			layout.setConstraint(this.outgoingMergeText, data);
		}
		
		//comment
		Label commentLabel = new Label(SVNRevisionGraphMessages.RevisionTooltipFigure_Comment);
		parent.add(commentLabel);
		data = new GridData();
		data.horizontalAlignment = SWT.LEFT;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		layout.setConstraint(commentLabel, data);
		commentLabel.setFont(boldFont);
		
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
				
		//incoming merge
		if (this.revisionNode.hasIncomingMerges()) {
			StringBuilder str = new StringBuilder();
			NodeMergeData[] mergedData = this.revisionNode.getIncomingMerges();
						
			/*
			 * As there can be many revisions then we sort and
			 * group them and limit revisions number in one row
			 */
			
			//as first row contains path, then revisions count should be less
			final int maxRevisionsInFirstRow = 5;
			final int maxRevisionsInRow = 15;
								
			for (int i = 0; i < mergedData.length; i ++) {
				str.append(mergedData[i].path).append(": "); //$NON-NLS-1$
				
				boolean isFirstRow = true;
				List<Range> ranges = Range.getRanges(mergedData[i].getRevisions());
				for (int j = 0, n = ranges.size(); j < n; j ++) {
					str.append(ranges.get(j));
																				
					if (j != n - 1) {
						str.append(","); //$NON-NLS-1$
						
						if (isFirstRow && ((j + 1) % maxRevisionsInFirstRow == 0)) {
							str.append("\n"); //$NON-NLS-1$
							isFirstRow = false;
						} else if (!isFirstRow && ((j + 1) % maxRevisionsInRow == 0)) {
							str.append("\n"); //$NON-NLS-1$
						}
					}										
				}												
				
				if (i != mergedData.length - 1) {
					str.append("\n"); //$NON-NLS-1$
				}
			}
			this.incomingMergeText.setText(str.toString());
		}
		
		//outgoing merge
		if (this.revisionNode.hasOutgoingMerges()) {
			StringBuilder str = new StringBuilder();
			NodeMergeData[] mergedData = this.revisionNode.getOutgoingMerges();
			for (int i = 0; i < mergedData.length; i ++) {
				str.append(mergedData[i].path).append(": "); //$NON-NLS-1$
				
				long[] revisions = mergedData[i].getRevisions();
				Arrays.sort(revisions);
				for (int j = 0, n = revisions.length; j < n; j ++) {
					str.append(revisions[j]);
					if (j != n - 1) {
						str.append(","); //$NON-NLS-1$
					}
				}
								
				if (i != mergedData.length - 1) {
					str.append("\n"); //$NON-NLS-1$
				}
			}
												
			this.outgoingMergeText.setText(str.toString());
		}				
		
		String comment = this.revisionNode.getMessage();
		this.commentText.setText(comment == null || comment.length() == 0 ? SVNMessages.SVNInfo_NoComment : comment);
	}
	
	protected static class Range {
		long start;
		long end;
		
		Range(long start) {
			this.end = this.start = start;
		}		
		public String toString() {
			return this.start != this.end ? (this.start + "-" + this.end) : String.valueOf(this.start);  //$NON-NLS-1$
		}
		static List<Range> getRanges(long[] revisions) {
			if (revisions.length == 0) {
				return Collections.emptyList();				
			}			
			Arrays.sort(revisions);
			List<Range> ranges = new ArrayList<Range>();
			Range range = new Range(revisions[0]);
			ranges.add(range);
			for (int i = 1; i < revisions.length; i ++) {
				long rev = revisions[i];
				if (rev -1 != range.end) {
					range = new Range(rev);
					ranges.add(range);
				} else {
					range.end = rev;
				}
			}			
			return ranges;
		}
	}

}
