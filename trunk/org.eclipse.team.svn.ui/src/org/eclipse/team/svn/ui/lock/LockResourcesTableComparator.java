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

package org.eclipse.team.svn.ui.lock;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.svn.ui.composite.LockResourceSelectionComposite;
import org.eclipse.team.svn.ui.utility.ColumnedViewerComparator;

/**
 * Lock resources table comparator
 * 
 * @author Igor Burilo
 */
public class LockResourcesTableComparator extends ColumnedViewerComparator {

	public LockResourcesTableComparator(Viewer tableViewer) {
		super(tableViewer);		
	}

	public int compareImpl(Viewer viewer, Object row1, Object row2) {
		LockResource data1 = (LockResource)row1;
		LockResource data2 = (LockResource)row2;
		switch (this.column) {
			case LockResourceSelectionComposite.COLUMN_NAME: {
				return ColumnedViewerComparator.compare(data1.getName(), data2.getName());
			}
			case LockResourceSelectionComposite.COLUMN_PATH: {
				return ColumnedViewerComparator.compare(data1.getPath(), data2.getPath());
			}
			case LockResourceSelectionComposite.COLUMN_OWNER: {
				return ColumnedViewerComparator.compare(data1.getOwner(), data2.getOwner());
			}
			case LockResourceSelectionComposite.COLUMN_STATE : {
				return data1.getLockStatus().compareTo(data2.getLockStatus());
			}
			case LockResourceSelectionComposite.COLUMN_DATE : {
				return data1.getCreationDate().compareTo(data2.getCreationDate());
			}		
		}
		return 0;
	}

}
