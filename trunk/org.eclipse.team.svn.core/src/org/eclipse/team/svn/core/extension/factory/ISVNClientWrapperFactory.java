/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.extension.factory;

import org.eclipse.team.svn.core.client.ISVNClientWrapper;

/**
 * Extension point factory for ISVNClientWrapper
 * 
 * @author Alexander Gurov
 */
public interface ISVNClientWrapperFactory {
	public static final String DEFAULT_ID = "org.eclipse.team.svn.client.javahl";
	public static final String CURRENT_COMPATIBILITY_VERSION = "1.1.2";
	
	/**
	 * Makes new SVN Client Library instance
	 * @return SVN Client Library instance
	 */
	public ISVNClientWrapper newInstance();
	
	/**
	 * Returns unique SVN Client library plug-in id
	 * @return SVN Client library plug-in id
	 */
	public String getId();
	/**
	 * Returns user-friendly SVN Client library plug-in name
	 * @return SVN Client library plug-in name
	 */
	public String getName();
	
	/**
	 * Returns SVN Client library plug-in version
	 * @return plug-in version
	 */
	public String getVersion();
	
	/**
	 * Returns SVN Client library plug-in API compatibility version
	 * @return plug-in version
	 */
	public String getCompatibilityVersion();
	
	/**
	 * Returns SVN Client library version
	 * @return client version
	 */
	public String getClientVersion();
	
	/**
	 * Tell revision change reporting for folders is allowed or not 
	 * @return true if allowed false otherwise
	 */
	public boolean isReportRevisionChangeAllowed();
	/**
	 * Tell compare folders is allowed or not
	 * @return true if allowed false otherwise
	 */
	public boolean isCompareFoldersAllowed();
	/**
	 * Tell interactive merge is allowed or not
	 * @return true if allowed false otherwise
	 */
	public boolean isInteractiveMergeAllowed();
	/**
	 * Tell atomic commit is allowed or not
	 * @return true if allowed false otherwise
	 */
	public boolean isAtomicCommitAllowed();
	/**
	 * Tell fetch locks is allowed or not
	 * @return true if allowed false otherwise
	 */
	public boolean isFetchLocksAllowed();
	/**
	 * Tell SSH settings is allowed or not
	 * @return true if allowed false otherwise
	 */
	public boolean isSSHOptionsAllowed();
	/**
	 * Tell proxy settings is allowed or not
	 * @return true if allowed false otherwise
	 */
	public boolean isProxyOptionsAllowed();

}
