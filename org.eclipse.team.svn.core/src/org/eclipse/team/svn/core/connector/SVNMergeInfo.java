/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.connector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The merge information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector
 * library is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to
 * do this is providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNMergeInfo {
	/**
	 * Kind of merge info log classes
	 */
	public enum LogKind {
		/**
		 * Does not exist
		 */
		ELIGIBLE(0),
		/**
		 * Exists, but uninteresting
		 */
		MERGED(1);
		
		public final int id;
		
		private LogKind(int id) {
			this.id = id;
		}
	}
	
	/**
	 * The three ways to request mergeinfo affecting a given path
	 * @since 1.9
	 */
	public enum Inheritance {
		/**
		 * Explicit mergeinfo only
		 */
		EXPLICIT(0),
		/**
		 * Explicit mergeinfo, or if that doesn't exist, the inherited
         * mergeinfo from a target's nearest (path-wise, not history-wise)
         * ancestor
		 */
		INHERITED(1),
		/**
		 * Mergeinfo inherited from a target's nearest (path-wise,
         * not history-wise) ancestor, regardless of whether target
         * has explicit mergeinfo
		 */
		NEAREST_ANCESTOR(2);
		
		public final int id;
		
		private Inheritance(int id) {
			this.id = id;
		}
	}
	
	private Map<String, List<SVNRevisionRange>> mergeSources;

	public SVNMergeInfo() {
		this.mergeSources = new HashMap<String, List<SVNRevisionRange>>();
	}

	/**
	 * Add one or more RevisionRange objects to merge info.
	 * 
	 * @param mergeSrc
	 *            The merge source URL.
	 * @param range
	 *            List of RevisionRange objects to add.
	 */
	public void addRevisions(String mergeSrc, SVNRevisionRange[] ranges) {
		List<SVNRevisionRange> revisions = this.getRevisionList(mergeSrc);
		revisions.addAll(Arrays.asList(ranges));
	}
	
	/*
    public void addRevisions(String mergeSrc, RevisionRangeList ranges)
    {
        addRevisions(mergeSrc, ranges.getRanges());
    }
	 */

	/**
	 * Add a revision range to the merged revisions for a path.
	 * 
	 * @param mergeSrc
	 *            The merge source URL.
	 * @param range
	 *            The revision range to add.
	 */
	public void addRevisionRange(String mergeSrc, SVNRevisionRange range) {
		List<SVNRevisionRange> revisions = this.getRevisionList(mergeSrc);
		revisions.add(range);
	}

	/**
	 * Get the merge source URLs.
	 * 
	 * @return The merge source URLs.
	 */
	public String[] getPaths() {
		Set<String> pathSet = this.mergeSources.keySet();
		return pathSet == null ? null : pathSet.toArray(new String[pathSet.size()]);
	}

	/**
	 * Get the revision ranges for the specified merge source URL.
	 * 
	 * @param mergeSrc
	 *            The merge source URL, or <code>null</code>.
	 * @return Array of RevisionRange objects, or <code>null</code>.
	 */
	public SVNRevisionRange[] getRevisions(String mergeSrc) {
		List<SVNRevisionRange> revisions = this.mergeSources.get(mergeSrc);
		return revisions == null ? null : revisions.toArray(new SVNRevisionRange[revisions.size()]);
	}
	
    /**
     * Like {@link #getReivsionRange}, but returns a {@link RevisionRangeList}.
     * @since 1.9
     */
    /*public RevisionRangeList getRevisionRangeList(String mergeSrc)
    {
        return new RevisionRangeList(getRevisionRange(mergeSrc));
    }*/
	
    /**
     * Parse the <code>svn:mergeinfo</code> property to populate the
     * merge source URLs and revision ranges of this instance.
     * @param mergeinfo <code>svn:mergeinfo</code> property value.
     */
    /*public void loadFromMergeinfoProperty(String mergeinfo)
    {
        if (mergeinfo == null)
            return;
        StringTokenizer st = new StringTokenizer(mergeinfo, "\n");
        while (st.hasMoreTokens())
        {
            parseMergeinfoLine(st.nextToken());
        }
    }
    
    private void parseMergeinfoLine(String line)
    {
        int colon = line.indexOf(':');
        if (colon > 0)
        {
            String pathElement = line.substring(0, colon);
            String revisions = line.substring(colon + 1);
            parseRevisions(pathElement, revisions);
        }
    }
    
    private void parseRevisions(String path, String revisions)
    {
        List<RevisionRange> rangeList = this.getRevisions(path);
        StringTokenizer st = new StringTokenizer(revisions, ",");
        while (st.hasMoreTokens())
        {
            String revisionElement = st.nextToken();
            RevisionRange range = new RevisionRange(revisionElement);
            if (rangeList == null)
                rangeList = new ArrayList<RevisionRange>();
            rangeList.add(range);
        }
        if (rangeList != null)
            setRevisionList(path, rangeList);
    }
    private void setRevisionList(String mergeSrc, List<RevisionRange> range)
    {
        mergeSources.put(mergeSrc, range);
    }
    */

	protected List<SVNRevisionRange> getRevisionList(String mergeSrc) {
		List<SVNRevisionRange> revisions = this.mergeSources.get(mergeSrc);
		if (revisions == null) {
			this.mergeSources.put(mergeSrc, revisions = new ArrayList<SVNRevisionRange>());
		}
		return revisions;
	}

}
