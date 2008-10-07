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

package org.eclipse.team.svn.ui.history.data;

import org.eclipse.team.svn.core.connector.SVNRevision;

/**
 * Class for changed path data to be used in models.
 * Contains the needed data for unique item changed in the commit.
 * 
 * @author Alexei Goncharov
 */
public class SVNChangedPathData {
	
	/**
	 * Action performed to resource.
	 */
	public final char action;
	
	/**
	 * Name of the resource in changed path.
	 */
	public final String resourceName;
	
	/**
	 * Path of the resource in changed path.
	 * Can be empty for ROOT.
	 */
	public final String resourcePath;
			
	/**
	 * Previous resource path, if it had been copied.
	 * Can be empty if the resource hadn't changed its destination.
	 */
	public final String copiedFromPath;
	
	/**
	 * Previous resource revision, if it had been copied.
	 * Can be {@link SVNRevision.INVALID_REVISION_NUMBER} if the resource hadn't changed its destination.
	 */
	public final long copiedFromRevision;

	/**
	 * The {@link SVNChangedPathData} instance could be initialized only once because all fields are final
	 * 
	 * @param action
	 *            - action performed to resource
	 * @param resourceName
	 *            - name of the resource in changed path
	 * @param resourcePath
	 *            - path of the resource in changed path
	 * @param copiedFromPath
	 *            - previous resource path, if it had been copied
	 * @param copiedFromRevision
	 *            - previous resource revision, if it had been copied
	 */
	public SVNChangedPathData(char action,
						String resourceName,
						String resourcePath,
						String copiedFromPath,
						long copiedFromRevision) {
		this.action = action;
		this.resourceName = resourceName;
		this.resourcePath = resourcePath;
		this.copiedFromPath = copiedFromPath;
		this.copiedFromRevision = copiedFromRevision;
	}
	
	/**
	 * Method to get full path of the resource. Used in tree building.
	 * 
	 * @return full resource path
	 */
	public String getFullResourcePath() {
		return this.resourcePath + (this.resourcePath.length() > 0 ? "/" : "") + this.resourceName;
	}
}
