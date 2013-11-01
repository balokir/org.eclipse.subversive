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
 * Patch call-back interface
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public interface ISVNPatchCallback {
	/**
	 * This method will be called by the connector library for an every single patched file.
	 * 
	 * @param pathFromPatchfile
	 *            the path in the patch file
	 * @param patchPath
	 *            the path of the patch
	 * @param rejectPath
	 *            the path of the reject file
	 * @return <code>true</code> to filter out the prospective patch
	 */
	public boolean singlePatch(String pathFromPatchfile, String patchPath, String rejectPath);
}
