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

/**
 * Copy source information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNEntryReference {
	/**
	 * The copy source path or URL.
	 */
	public final String path;

	/**
	 * The copy source peg revision. If null could be treated as {@link SVNRevision#HEAD} or ignored depending on calling context.
	 */
	public final SVNRevision pegRevision;

	/**
	 * The {@link SVNEntryRevisionReference} instance could be initialized only once because all fields are final
	 * 
	 * @param path
	 *            the source path
	 */
	public SVNEntryReference(String path) {
		this(path, null);
	}

	/**
	 * The {@link SVNEntryRevisionReference} instance could be initialized only once because all fields are final
	 * 
	 * @param path
	 *            the source path
	 * @param pegRevision
	 *            the source peg revision
	 */
	public SVNEntryReference(String path, SVNRevision pegRevision) {
		this.path = path;
		this.pegRevision = pegRevision;
	}

	public String toString() {
		return this.pegRevision == null ? this.path : (this.path + "@" + this.pegRevision.toString());
	}
	
}
