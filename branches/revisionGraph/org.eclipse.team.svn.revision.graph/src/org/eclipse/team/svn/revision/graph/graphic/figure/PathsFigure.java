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
import org.eclipse.draw2d.ToolbarLayout;

/**
 * 
 * @author Igor Burilo
 */
public class PathsFigure extends Figure {

	public PathsFigure()
	{
		ToolbarLayout layout = new ToolbarLayout();
		//layout.setMinorAlignment(ToolbarLayout.ALIGN_BOTTOMRIGHT);
		//layout.setMajorAlignment(FlowLayout.ALIGN_RIGHTBOTTOM);
		//layout.setStretchMinorAxis(false);
		//layout.setHorizontal(false);			
		
		setLayoutManager(layout);										
		
		//setBorder(new ColumnFigureBorder());
		//setBackgroundColor(ColorConstants.tooltipBackground);
		//setForegroundColor(ColorConstants.blue);
		setOpaque(true);
		
		//this.setBorder(new LineBorder(ColorConstants.black, 3));
	}
}
