/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.mapping;

import java.util.Comparator;

import org.eclipse.team.internal.core.subscribers.ChangeSet;

public class SVNChangeSetComparator implements Comparator<ChangeSet> {
	private SVNChangeSetSorter fSorter= new SVNChangeSetSorter();

	public int compare(ChangeSet o1, ChangeSet o2) {
		return this.fSorter.compare(null, o1, o2);
	}
}
