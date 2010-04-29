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
package org.eclipse.team.svn.revision.graph.graphic.editpart;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.editparts.ScalableRootEditPart;

/**
 * Add decoration layer as we'll show expand/collapse in it 
 * 
 * @author Igor Burilo
 */
public class GraphScalableRootEditPart extends ScalableRootEditPart {

	public final static String DECORATION_LAYER = "Decoration Layer"; //$NON-NLS-1$
	
	@Override
	protected LayeredPane createPrintableLayers() {	
		LayeredPane pane = new LayeredPane();
		
		Layer layer = new Layer();
		layer.setLayoutManager(new StackLayout());
		pane.add(layer, PRIMARY_LAYER);

		layer = new ConnectionLayer();
		layer.setPreferredSize(new Dimension(5, 5));
		pane.add(layer, CONNECTION_LAYER);
		
		layer = new Layer();		
		pane.add(layer, DECORATION_LAYER);
		
		return pane;
	}
		
}
