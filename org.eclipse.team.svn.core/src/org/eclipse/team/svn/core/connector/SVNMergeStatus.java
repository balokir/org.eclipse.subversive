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

package org.eclipse.team.svn.core.connector;

/**
 * The status information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector
 * library is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to
 * do this is providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNMergeStatus extends SVNEntryStatus {
	/**
	 * The repository URL of the entry
	 */
	public final String url;

	/**
	 * The working copy path of the entry
	 */
	public final String path;

	/**
	 * The revision of the last change in the merged repository resource.
	 */
	public final long revision;

	/**
	 * The date of the last change in the merged repository resource.
	 */
	public final long date;

	/**
	 * The author of the last change in the merged repository resource.
	 */
	public final String author;

	/**
	 * The comment entered for the last change in the merged repository resource. Could be <code>null</code>.
	 */
	public final String comment;

	/**
	 * The {@link SVNMergeStatus} instance could be initialized only once because all fields are final
	 * 
	 * @param url
	 *            The repository URL of the entry
	 * @param path
	 *            The working copy path of the entry
	 * @param nodeKind
	 *            The entry kind (see {@link Kind})
	 * @param textStatus
	 *            The entry content merge status (see {@link SVNEntryStatus.Kind})
	 * @param propStatus
	 *            The entry properties merge status (see {@link SVNEntryStatus.Kind})
	 * @param revision
	 *            The revision of the last change in the merged repository resource.
	 * @param date
	 *            The date of the last change in the merged repository resource.
	 * @param author
	 *            The author of the last change in the merged repository resource.
	 * @param comment
	 *            The comment entered for the last change in the merged repository resource. Could be <code>null</code>.
	 */
	public SVNMergeStatus(String url, String path, int nodeKind, int textStatus, int propStatus, long revision, long date, String author, String comment) {
		super(nodeKind, textStatus, propStatus);
		this.url = url;
		this.path = path;
		this.revision = revision;
		this.date = date;
		this.author = author;
		this.comment = comment;
	}
}
