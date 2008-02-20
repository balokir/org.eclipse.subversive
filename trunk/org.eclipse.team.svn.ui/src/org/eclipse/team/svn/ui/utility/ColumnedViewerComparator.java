/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.utility;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Abstract columned viewer comparator
 * 
 * @author Alexei Goncharov
 */
public abstract class ColumnedViewerComparator extends ViewerComparator {
	protected int column;
    protected boolean reversed;
    protected Viewer basedOn;
    protected IPropertyChangeListener configurationListener;

	public static boolean CASE_INSENSITIVE = true;
	
	public ColumnedViewerComparator(Viewer basedOn, int column) {
		super();
		this.reversed = false;
		this.column = column;
		 final IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		 ColumnedViewerComparator.CASE_INSENSITIVE = SVNTeamPreferences.getBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_CASE_INSENSITIVE_TABLE_SORTING_NAME);
	        this.configurationListener = new IPropertyChangeListener() {
	        	public void propertyChange(PropertyChangeEvent event) {
	        		if (event.getProperty().equals(SVNTeamPreferences.fullBehaviourName(SVNTeamPreferences.BEHAVIOUR_CASE_INSENSITIVE_TABLE_SORTING_NAME))) {
	        			ColumnedViewerComparator.CASE_INSENSITIVE = SVNTeamPreferences.getBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_CASE_INSENSITIVE_TABLE_SORTING_NAME);
	        			if (!ColumnedViewerComparator.this.basedOn.getControl().isDisposed()) {
	        				ColumnedViewerComparator.this.basedOn.refresh();
	        			}
	        			else {
	        				store.removePropertyChangeListener(this);
	        			}
	        		}
	        	}
	        };
	        store.addPropertyChangeListener(this.configurationListener);

	}
	
	public boolean isReversed() {
		return this.reversed;
	}

	public void setReversed(boolean reversed) {
		this.reversed = reversed;
	}
	
	public int getColumnNumber() {
		return this.column;
	}
	
	public abstract int compare(Viewer viewer, Object row1, Object row2);
	
	public static int compare(String first, String second, boolean reversed) {
		if (reversed) {
			return ColumnedViewerComparator.CASE_INSENSITIVE ? second.compareToIgnoreCase(first) : second.compareTo(first);
		}
		return ColumnedViewerComparator.CASE_INSENSITIVE ? first.compareToIgnoreCase(second) : first.compareTo(second);
    }
	
}
