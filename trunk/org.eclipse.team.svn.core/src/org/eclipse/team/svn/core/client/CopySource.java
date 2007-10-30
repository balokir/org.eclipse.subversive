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

/**
 * Replacement for org.tigris.subversion.javahl.CopySource
 * 
 * @author Alexander Gurov
 */
public class CopySource {
	/**
	 * The source path or URL.
	 */
	public final String path;

	/**
	 * The source revision.
	 */
	public final Revision revision;

	/**
	 * The peg revision.
	 */
	public final Revision pegRevision;

	/**
	 * Create a new instance.
	 * 
	 * @param path
	 * @param revision
	 *            The source revision.
	 * @param pegRevision
	 *            The peg revision. Typically interpreted as
	 *            {@link org.tigris.subversion.javahl.Revision#HEAD} when
	 *            <code>null</code>.
	 */
	public CopySource(String path, Revision revision, Revision pegRevision) {
		this.path = path;
		this.revision = revision;
		this.pegRevision = pegRevision;
	}
}
