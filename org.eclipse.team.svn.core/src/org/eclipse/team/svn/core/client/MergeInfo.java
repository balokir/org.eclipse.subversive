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
 * The merge information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class MergeInfo {
	private Map mergeSources;

	public MergeInfo() {
		this.mergeSources = new HashMap();
	}

	/**
	 * Add one or more RevisionRange objects to merge info.
	 * 
	 * @param mergeSrc
	 *            The merge source URL.
	 * @param range
	 *            List of RevisionRange objects to add.
	 */
	public void addRevisions(String mergeSrc, RevisionRange[] ranges) {
		List revisions = this.getRevisionList(mergeSrc);
		revisions.addAll(Arrays.asList(ranges));
	}

	/**
	 * Add a revision range to the merged revisions for a path.
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
		return pathSet == null ? null : (String[]) pathSet.toArray(new String[pathSet.size()]);
	}

	/**
	 * Get the revision ranges for the specified merge source URL.
	 * 
	 * @param mergeSrc
	 *            The merge source URL, or <code>null</code>.
	 * @return Array of RevisionRange objects, or <code>null</code>.
	 */
	public RevisionRange[] getRevisions(String mergeSrc) {
		List revisions = (List) this.mergeSources.get(mergeSrc);
		return revisions == null ? null : (RevisionRange[]) revisions.toArray(new RevisionRange[revisions.size()]);
	}

	protected List getRevisionList(String mergeSrc) {
		List revisions = (List) this.mergeSources.get(mergeSrc);
		if (revisions == null) {
			this.mergeSources.put(mergeSrc, revisions = new ArrayList());
		}
		return revisions;
	}

}
