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
public class SVNDiffStatus {

	/**
	 * The first entry-related resource path or URL
	 */
	public final String path1;

	/**
	 * The second entry-related resource path or URL
	 */
	public final String path2;

	/**
	 * The entry kind (see {@link NodeKind})
	 */
	public final int nodeKind;

	/**
	 * The entries content diff status (see {@link SVNEntryStatus.Kind})
	 */
	public final int textStatus;

	/**
	 * The entries properties diff status (see {@link SVNEntryStatus.Kind})
	 */
	public final int propStatus;

	/**
	 * The {@link SVNDiffStatus} instance could be initialized only once because all fields are final
	 * 
	 * @param path1
	 *            The first entry-related resource path or URL
	 * @param path2
	 *            The second entry-related resource path or URL
	 * @param nodeKind
	 *            The entry kind (see {@link NodeKind})
	 * @param textStatus
	 *            The entries content diff status (see {@link SVNEntryStatus.Kind})
	 * @param propStatus
	 *            The entries properties diff status (see {@link SVNEntryStatus.Kind})
	 */
	public SVNDiffStatus(String path1, String path2, int nodeKind, int textStatus, int propStatus) {
		this.path1 = path1;
		this.path2 = path2;
		this.nodeKind = nodeKind;
		this.textStatus = textStatus;
		this.propStatus = propStatus;
	}

}
