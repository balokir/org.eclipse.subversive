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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;

/**
 * Filter for revision nodes
 * 
 * @author Igor Burilo
 */
public abstract class AbstractRevisionNodeFilter {

	public abstract boolean accept(RevisionNode node);
	
	public static AbstractRevisionNodeFilter ACCEPT_ALL_FILTER = new AbstractRevisionNodeFilter() {
		public boolean accept(RevisionNode node) {
			return true;
		}
	};
	
	/**
	 * Filter out modified nodes which don't have copy to
	 */
	public static AbstractRevisionNodeFilter SIMPLE_MODE_FILTER = new AbstractRevisionNodeFilter() {
		
		public boolean accept(RevisionNode node) {
			if (node.getAction() == RevisionNodeAction.MODIFY) {
				//if node has collapses then don't filter it out
				if (node.isNextCollapsed() || 
					node.isPreviousCollapsed() ||
					node.isCopiedFromCollapsed() ||
					node.isCopiedToCollapsed() ||
					node.isRenameCollapsed()) {
					return true;
				}
				if (node.getCopiedTo().length == 0) {
					return false;
				}
			}
			return true;
		}		
	};
	
	public static class AndRevisionNodeFilter extends AbstractRevisionNodeFilter {

		protected Set<AbstractRevisionNodeFilter> filters = new HashSet<AbstractRevisionNodeFilter>();	
		
		public void addFilter(AbstractRevisionNodeFilter filter) {
			this.filters.add(filter);
		}
		
		public void removeFilter(AbstractRevisionNodeFilter filter) {
			this.filters.remove(filter);
		}				
		
		public boolean accept(RevisionNode node) {
			if (!this.filters.isEmpty()) {
				for (AbstractRevisionNodeFilter filter : this.filters) {
					if (!filter.accept(node)) {
						return false;
					}
				}
			} 
			return true;
		}		
	};
	
}
