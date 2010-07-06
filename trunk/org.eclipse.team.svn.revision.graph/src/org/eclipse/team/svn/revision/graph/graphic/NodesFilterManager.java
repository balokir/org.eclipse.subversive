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
package org.eclipse.team.svn.revision.graph.graphic;

import org.eclipse.team.svn.revision.graph.TopRightTraverseVisitor;
import org.eclipse.team.svn.revision.graph.graphic.AbstractRevisionNodeFilter.AndRevisionNodeFilter;

/**
 * 
 * @author Igor Burilo
 */
public class NodesFilterManager {

	protected AndRevisionNodeFilter filters = new AndRevisionNodeFilter();
	
	public void addFilter(AbstractRevisionNodeFilter filter) {
		this.filters.addFilter(filter);	
	}
	
	public void removeFilter(AbstractRevisionNodeFilter filter) {
		this.filters.removeFilter(filter);
	}
	
	public void applyFilters(RevisionNode startNode) {
		final AbstractRevisionNodeFilter filter = this.filters.filters.isEmpty() ? AbstractRevisionNodeFilter.ACCEPT_ALL_FILTER : this.filters;
		new TopRightTraverseVisitor.AllNodesVisitor() {
			protected void visit(RevisionNode node) {
				boolean isAccepted = filter.accept(node);
				node.setFiltered(!isAccepted);
			}
		}.traverse(startNode);
	}

}
