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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * 
 * @author Igor Burilo
 */
public class ChangesNotifier {
	
	//used by RevisionRootNode to notify that filtering was applied 
	public final static String FILTER_NODES_PROPERTY = "filterNodes"; //$NON-NLS-1$
	
	//used by RevisionRootNode to notify that nodes expanded/collapsed
	public final static String EXPAND_COLLAPSE_NODES_PROPERTY = "expandCollapseNodes"; //$NON-NLS-1$
	
	//used by RevisionRootNode to notify that model was refreshed
	public final static String REFRESH_NODES_PROPERTY = "refreshNodes"; //$NON-NLS-1$
	
	//used by RevisionNode to notify that expanded/collapsed was called on node
	public final static String EXPAND_COLLAPSE_ON_NODE_PROPERTY = "expandCollapseOnNode"; //$NON-NLS-1$
	
	//used by RevisionNode to notify that its connections were changed
	public final static String REFRESH_NODE_CONNECTIONS_PROPERTY = "refreshNodeConnections"; //$NON-NLS-1$
	
	//used by RevisionNode to notify that its merge source connections were changed
	public final static String REFRESH_NODE_MERGE_SOURCE_CONNECTIONS_PROPERTY = "refreshMergeSourceConnections"; //$NON-NLS-1$
	
	//used by RevisionNode to notify that its merge target connections were changed
	public final static String REFRESH_NODE_MERGE_TARGET_CONNECTIONS_PROPERTY = "refreshMergeTargetConnections"; //$NON-NLS-1$
	
	//used by RevisionNode to notify that truncate path property was changed
	public final static String TRUNCATE_NODE_PATH_PROPERTY = "truncateNodePath"; //$NON-NLS-1$
	
	protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.listeners.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.listeners.removePropertyChangeListener(listener);
	}
	
	protected void firePropertyChange(String prop, Object old, Object newValue) {
		this.listeners.firePropertyChange(prop, old, newValue);
	}	
	
}
