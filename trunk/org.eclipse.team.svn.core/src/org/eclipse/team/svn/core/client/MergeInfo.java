/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Replacement for org.tigris.subversion.javahl.MergeInfo
 * 
 * @author Alexander Gurov
 */
public class MergeInfo {
	private Map mergeSources;

	public MergeInfo() {
		this.mergeSources = new HashMap();
	}

	/**
	 * Add one or more RevisionRange objects to merge info. If the merge source
	 * is already stored, the list of revisions is replaced.
	 * 
	 * @param mergeSrc
	 *            The merge source URL.
	 * @param range
	 *            List of RevisionRange objects to add.
	 * @throws SubversionException
	 *             If range list contains objects of type other than
	 *             RevisionRange.
	 */
    public void addRevisions(String mergeSrc, RevisionRange[] ranges) {
		List revisions = this.getRevisionList(mergeSrc);
		revisions.addAll(Arrays.asList(ranges));
	}

	/**
	 * Add a revision range to the merged revisions for a path. If the merge
	 * source already has associated revision ranges, add the revision range to
	 * the existing list.
	 * 
	 * @param mergeSrc
	 *            The merge source URL.
	 * @param range
	 *            The revision range to add.
	 */
	public void addRevisionRange(String mergeSrc, RevisionRange range) {
		List revisions = this.getRevisionList(mergeSrc);
		revisions.add(range);
	}

	/**
	 * Get the merge source URLs.
	 * 
	 * @return The merge source URLs.
	 */
	public String[] getPaths() {
		Set pathSet = this.mergeSources.keySet();
		if (pathSet == null) {
			return null;
		}
		return (String[]) pathSet.toArray(new String[pathSet.size()]);
	}

	/**
	 * Get the revision ranges for the specified merge source URL.
	 * 
	 * @param mergeSrc
	 *            The merge source URL, or <code>null</code>.
	 * @return List of RevisionRange objects, or <code>null</code>.
	 */
	public List getRevisions(String mergeSrc) {
		return (List) this.mergeSources.get(mergeSrc);
	}

	/**
	 * Get the RevisionRange objects for the specified merge source URL
	 * 
	 * @param mergeSrc
	 *            The merge source URL, or <code>null</code>.
	 * @return Array of RevisionRange objects, or <code>null</code>.
	 */
	public RevisionRange[] getRevisionRange(String mergeSrc) {
		List revisions = this.getRevisions(mergeSrc);
		if (revisions == null) {
			return null;
		}
		return (RevisionRange[]) revisions.toArray(new RevisionRange[revisions.size()]);
	}

	protected List getRevisionList(String mergeSrc) {
		List revisions = this.getRevisions(mergeSrc);
		if (revisions == null) {
			this.mergeSources.put(mergeSrc, revisions = new ArrayList());
		}
		return revisions;
	}
	
}
