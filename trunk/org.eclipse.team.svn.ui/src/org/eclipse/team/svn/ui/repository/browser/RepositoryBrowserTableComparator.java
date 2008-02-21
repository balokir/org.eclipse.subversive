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

package org.eclipse.team.svn.ui.repository.browser;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.svn.core.connector.SVNLock;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource.Information;
import org.eclipse.team.svn.ui.repository.model.RepositoryFictiveWorkingDirectory;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.team.svn.ui.utility.ColumnedViewerComparator;

/**
 * Table comparator for RepositoryBrowserTableViewer
 * 
 * @author Alexei Goncharov
 */
public class RepositoryBrowserTableComparator extends ColumnedViewerComparator {

	public RepositoryBrowserTableComparator(Viewer basedOn) {
		super(basedOn);
	}
	
	public int compare(Viewer viewer, Object row1, Object row2) {
		if (row1 instanceof RepositoryFictiveWorkingDirectory) {
			return -1;
		}
		if (row2 instanceof RepositoryFictiveWorkingDirectory) {
			return  1;
		}
		IRepositoryResource rowData1 = ((RepositoryResource)row1).getRepositoryResource();
		IRepositoryResource rowData2 = ((RepositoryResource)row2).getRepositoryResource();
		Information info1 = (rowData1).getInfo();
		Information info2 = (rowData2).getInfo();
		boolean cnd1 = rowData1 instanceof IRepositoryContainer;
        boolean cnd2 = rowData2 instanceof IRepositoryContainer;
        if (cnd1 && !cnd2) {
            return -1;
        }
        else if (cnd2 && !cnd1) {
            return 1;
        }
		if (column == RepositoryBrowserTableViewer.COLUMN_NAME) {
			String name1 = rowData1.getName();
			String name2 = rowData2.getName();
            return ColumnedViewerComparator.compare(name1, name2, this.isReversed());
		}
		else if (column == RepositoryBrowserTableViewer.COLUMN_REVISION) {
			try {
				Long c1 = new Long(rowData1.getRevision()); 
				Long c2 = new Long(rowData2.getRevision());
				return (this.isReversed()) ? c2.compareTo(c1) : c1.compareTo(c2);
			}
			catch (Exception ex) {
				// not interesting in this context, will never happen
			}
		} else if (info1 != null && info2 != null) {
			if (column == RepositoryBrowserTableViewer.COLUMN_LAST_CHANGE_DATE) {
				Long c1 = new Long(info1.lastChangedDate); 
				Long c2 = new Long(info2.lastChangedDate);
				return (this.isReversed()) ? c2.compareTo(c1) : c1.compareTo(c2);
			} else if (column == RepositoryBrowserTableViewer.COLUMN_LAST_CHANGE_AUTHOR) {
				String author1 = info1.lastAuthor;
				String author2 = info2.lastAuthor;
				author1 = (author1 != null) ? author1 : RepositoryBrowserTableViewer.noAuthor;
				author2 = (author2 != null) ? author2 : RepositoryBrowserTableViewer.noAuthor;
				return ColumnedViewerComparator.compare(author1, author2, this.isReversed());
			} else if (column == RepositoryBrowserTableViewer.COLUMN_LOCK_OWNER) {
				SVNLock lock1 = info1.lock;
				SVNLock lock2 = info2.lock;
				String lockOwner1 = (lock1 == null) ? "" : lock1.owner;
				String lockOwner2 = (lock2 == null) ? "" : lock2.owner;
				return ColumnedViewerComparator.compare(lockOwner1, lockOwner2, this.isReversed());
			} else if (column == RepositoryBrowserTableViewer.COLUMN_HAS_PROPS) {
				boolean hasProps1 = info1.hasProperties;
				boolean hasProps2 = info2.hasProperties;
				String c1 = (hasProps1) ? RepositoryBrowserTableViewer.hasProps : RepositoryBrowserTableViewer.noProps;
				String c2 = (hasProps2) ? RepositoryBrowserTableViewer.hasProps : RepositoryBrowserTableViewer.noProps;
				return ColumnedViewerComparator.compare(c1, c2, this.isReversed());
			} else if (column == RepositoryBrowserTableViewer.COLUMN_SIZE) {
				Long c1 = new Long (info1.fileSize);
				Long c2 = new Long (info2.fileSize);
				return (this.isReversed()) ? c2.compareTo(c1) : c1.compareTo(c2);
			}					
		}
		return 0;
	}

}
