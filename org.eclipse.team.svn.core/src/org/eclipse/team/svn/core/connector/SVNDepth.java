package org.eclipse.team.svn.core.connector;

/**
 * Repository or working copy traversal depths enumeration
 */
public class SVNDepth {
	/**
	 * Depth undetermined or ignored.
	 */
	public static final int UNKNOWN = -2;

	/**
	 * Exclude (i.e, don't descend into) directory D.
	 */
	public static final int EXCLUDE = -1;

	/**
	 * Just the named file or folder without entries.
	 */
	public static final int EMPTY = 0;

	/**
	 * The folder and child files.
	 */
	public static final int FILES = 1;

	/**
	 * The folder and all direct child entries.
	 */
	public static final int IMMEDIATES = 2;

	/**
	 * The folder and all descendants at any depth.
	 */
	public static final int INFINITY = 3;

	public static final int infinityOrEmpty(boolean recurse) {
		return (recurse ? SVNDepth.INFINITY : SVNDepth.EMPTY);
	}

	public static final int infinityOrFiles(boolean recurse) {
		return (recurse ? SVNDepth.INFINITY : SVNDepth.FILES);
	}

	public static final int infinityOrImmediates(boolean recurse) {
		return (recurse ? SVNDepth.INFINITY : SVNDepth.IMMEDIATES);
	}
	
	public static final int unknownOrFiles(boolean recurse) {
		return (recurse ? SVNDepth.UNKNOWN : SVNDepth.FILES);
	}

}