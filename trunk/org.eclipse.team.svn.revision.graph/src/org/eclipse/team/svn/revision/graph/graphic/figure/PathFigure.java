/*******************************************************************************
 * Copyright (c) 2005-2010 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo (Polarion Software) - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.graphic.figure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

/**
 * Figure for node path. (It doesn't support icon)
 * 
 * It allows to show path in full or truncated modes. In full mode
 * path is splitted on lines if its width is more than node width.
 * In truncated mode path is shown in one line and has format:
 * /first segment.../end segments
 * where 'first segment' is always one and there can be several end segments.
 * In truncated mode we also highlight parts of path which are different from
 * its so-called parent path   
 * 
 * @author Igor Burilo
 */
public class PathFigure extends Label {

	protected final static String PATH_SEPARATOR = "/"; //$NON-NLS-1$
	
	//can be null
	protected String parentPath;
	protected boolean isShowFullPath;

	protected String subStringText;
	//index where part different from parent path starts
	protected int subStringTextIndex = -1;

	protected String[] multiLineSubstringText = new String[0];
	
	//for multi line text	
	protected int verticalTextGap = 0;

	//remember width of node
	protected int figureWidth = -1;

	//colors
	protected Color defaultColor = ColorConstants.black;
	protected Color reverseDefaultColor = ColorConstants.white;
	
	protected Color commonPathPartsColor = ColorConstants.gray;
	protected Color reverseCommonPathPartsColor = FigureUtilities.lighter(this.commonPathPartsColor);
	
	protected Color differentPathPartsColor = ColorConstants.black;
	protected Color reverseDifferentPathPartsColor = ColorConstants.white;	
	
	protected boolean isSelected;
	
	public PathFigure() {
		super();
		this.setLabelAlignment(PositionConstants.LEFT);
		this.setTextAlignment(PositionConstants.TOP);
	}
	
	public void setShowFullPath(boolean isShowFullPath) {
		this.isShowFullPath = isShowFullPath;
		
		revalidate();
		repaint();
	}
	
	public void setPaths(String path, String parentPath) {
		this.parentPath = parentPath;
		//calls repaint
		this.setText(path);		
	}
	
	public void setSelected(boolean isSelected) {
		if (this.isSelected == isSelected) {
			return;
		}
		this.isSelected = isSelected;		
		repaint();
	}
	
	protected void paintFigure(Graphics graphics) {		
		Rectangle bounds = getBounds();
		graphics.translate(bounds.x, bounds.y);		
		Point textLocation = getTextLocation();		
				
		if (this.isShowFullPath) {	
			Color color = this.isSelected ? this.reverseDefaultColor : this.defaultColor;
			graphics.setForegroundColor(color);
			
			String[] lines = this.getMultiLineSubStringText();
			for (int i = 0; i < lines.length; i ++) {												
				graphics.drawText(lines[i], textLocation);
				if (i != lines.length - 1) {				
					int offset = FigureUtilities.getStringExtents(lines[i], getFont()).height + this.verticalTextGap; 				
					textLocation = new Point(textLocation.x, textLocation.y + offset);
				}
			}
		} else {
			Color commonColor;
			Color diffColor;
			if (this.isSelected) {
				commonColor = this.reverseCommonPathPartsColor;
				diffColor = this.reverseDifferentPathPartsColor;
			} else {
				commonColor = this.commonPathPartsColor;
				diffColor = this.differentPathPartsColor;
			}			
			graphics.setForegroundColor(commonColor);
			
			String text = getSubStringText();						
			if (this.subStringTextIndex != -1) {
				String firstPathPart = text.substring(0, this.subStringTextIndex); 
				graphics.drawText(firstPathPart, textLocation);
				try {
					//draw path with different colors
					graphics.setForegroundColor(diffColor);
					int offset = FigureUtilities.getStringExtents(firstPathPart, getFont()).width;
					textLocation = new Point(textLocation.x + offset, textLocation.y);															
					String secondPathPart = text.substring(this.subStringTextIndex); 
					graphics.drawText(secondPathPart, textLocation);
				} finally {
					graphics.setForegroundColor(commonColor);
				}
			} else {				
				graphics.drawText(text, textLocation);	
			}									
		}	
		
		graphics.translate(-bounds.x, -bounds.y);
	}	
	
	public Dimension getPreferredSize(int wHint, int hHint) {		
		int oldFigureWidth = this.figureWidth;
		if (this.figureWidth != wHint && wHint != -1) {
			this.figureWidth = wHint; 
		}
		
		if (this.isShowFullPath) {
			if (prefSize == null || oldFigureWidth == -1 && this.figureWidth != -1) {								
				prefSize = new Dimension(0, 0);
				String[] paths = this.getMultiLineSubStringText(figureWidth);
				for (String path : paths) {
					Dimension lineSize = FigureUtilities.getStringExtents(path, this.getFont());
					prefSize.width = Math.max(prefSize.width, lineSize.width);
					prefSize.height += lineSize.height;
				}
				
				//add insets
				Insets insets = getInsets();
				prefSize.expand(insets.getWidth(), insets.getHeight());
				
				//add vertical text spacing 
				prefSize.height += this.verticalTextGap * (paths.length - 1);
												
				if (getLayoutManager() != null) {
					prefSize.union(getLayoutManager().getPreferredSize(this, wHint, hHint));
				}
			}
			
			if (wHint >= 0 && wHint < prefSize.width) {
				Dimension minSize = getMinimumSize(wHint, hHint);
				Dimension result = prefSize.getCopy();
				result.width = Math.min(result.width, wHint);
				result.width = Math.max(minSize.width, result.width);
				return result;
			}
			return prefSize;									
		} else {
			return super.getPreferredSize(wHint, hHint);	
		}		
	}
	
	protected String[] getMultiLineSubStringText() {
		return this.getMultiLineSubStringText(this.getSize().width);
	}
	
	protected String[] getMultiLineSubStringText(int width) {
		if (this.multiLineSubstringText.length > 0) {
			return this.multiLineSubstringText;
		}
		
		String[] pathParts = this.splitPath(this.getPath());
		List<List<String>> lines = new ArrayList<List<String>>();		
		List<String> line = null;
		for (int i = 0; i < pathParts.length; i ++) {
			if (line == null) {
				line = new ArrayList<String>();
				lines.add(line);
			}
			line.add(pathParts[i]);
			if (line.size() > 1) {
				String linePath = this.createPath(line.toArray(new String[0]));
				if (this.isTruncate(linePath, width)) {
					line.remove(line.size() - 1);				
					line = null;					
					i --;
				}
			}
		}
		
		//transform lines to result
		this.multiLineSubstringText = new String[lines.size()];
		for (int i = 0; i < lines.size(); i ++) {
			List<String> lineList = lines.get(i);
			this.multiLineSubstringText[i] = this.createPath(lineList.toArray(new String[0]));
		}
		return this.multiLineSubstringText;
	}
	
	@Override
	public String getSubStringText() {
		if (this.subStringText != null) {
			return this.subStringText;
		}		
		
		int deletedCount = 0;
		String[] strParts = this.splitPath(this.getPath());
		LinkedList<String> parts = new LinkedList<String>(Arrays.asList(strParts));
		if (getPreferredSize().width - getSize().width > 0) {																								
			if (parts.size() > 2) {
				do {
					//construct path				
					String path = this.createPath(parts.toArray(new String[0]));				
					if (this.isTruncate(path)) {
						if (deletedCount == 0) {
							parts.remove(1);
							parts.add(1, this.getTruncationString());
						} else {
							parts.remove(2);
						}
						deletedCount ++;
					} else {
						break;
					}
				} while (parts.size() > 3);
			}
			this.subStringText = this.createPath(parts.toArray(new String[0]));
		} else {
			this.subStringText = getPath();	
		}

		/*
		 * there can be cases where minimum allowed path is more that node width,
		 * but we don't truncate it on purpose
		 */
						
		this.calculateNotEqualParts(deletedCount, parts);
		
		return this.subStringText;
	}
	
	protected void calculateNotEqualParts(int deletedCount, List<String> newPathParts) {		
		//calculate start index for different path parts
		if (this.parentPath != null) {
			String[] pathParts = this.splitPath(this.getPath());
			String[] parentParts = this.splitPath(this.parentPath);
			int notEqualSegment = -1;
			for (int i = 0, n = Math.min(pathParts.length, parentParts.length); i < n; i ++) {
				String pathPart = pathParts[i];
				String parentPart = parentParts[i];
				if (!pathPart.equals(parentPart)) {
					notEqualSegment = i;
					break;
				}
			}
			//transform old segment to new one as we could make truncate
			if (notEqualSegment != -1) {
				int startSegment;
				if (deletedCount != 0) {
					if (notEqualSegment == 0) {
						startSegment = 0;
					} else if (notEqualSegment >= 1 && notEqualSegment <= deletedCount) {
						startSegment = 1;
					} else {
						startSegment = notEqualSegment - deletedCount + 1; 
					}
				} else {
					startSegment = notEqualSegment;
				}
				
				//convert segment to index	
				String tmpPath = this.createPath(newPathParts.toArray(new String[0]), startSegment);					
				this.subStringTextIndex = tmpPath.length();
			}
		} else {
			this.subStringTextIndex = 0;
		}
	} 
	
	protected String createPath(String[] parts) {
		return this.createPath(parts, parts.length);
	}
	
	protected String createPath(String[] parts, int length) {
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < length; i ++) {
			tmp.append(PATH_SEPARATOR).append(parts[i]);
		}
		return tmp.toString();
	}

	protected String[] splitPath(String path) {
		if (path.startsWith(PATH_SEPARATOR)) {
			path = path.substring(1);
		}
		return path.split(PATH_SEPARATOR);
	}
	
	protected String getPath() {
		return this.getText();
	}
	
	protected boolean isTruncate(String path) {
		return this.isTruncate(path, this.getSize().width);
	}
	
	protected boolean isTruncate(String path, int width) {
		int textWidth = FigureUtilities.getStringExtents(path, getFont()).width;
		return width < textWidth;		
	}
	
	@Override
	public void invalidate() {
		this.subStringText = null;
		this.subStringTextIndex = -1;
		this.multiLineSubstringText = new String[0];
		
		super.invalidate();				
	}
}
