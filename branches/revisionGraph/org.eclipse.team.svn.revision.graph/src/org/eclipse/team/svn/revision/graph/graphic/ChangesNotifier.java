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

	public final static String REFRESH_CONNECTIONS_PROPERTY = "refresh";
	public final static String LAYOUT_PROPERTY = "layout";
	
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
