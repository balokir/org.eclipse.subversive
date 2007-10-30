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

import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.operation.UnreportableException;

/**
 * Extension point factory for ISVNClientWrapper
 * 
 * @author Alexander Gurov
 */
public interface ISVNClientWrapperFactory {
	public static final String DEFAULT_ID = "org.polarion.team.svn.client.javahl";
	public static final String CURRENT_COMPATIBILITY_VERSION = "0.7.0";
	
	public static int SVNAPI_1_0_x = 0;
	public static int SVNAPI_1_1_x = 1;
	public static int SVNAPI_1_2_x = 2;
	public static int SVNAPI_1_3_x = 3;
	public static int SVNAPI_1_4_x = 4;
	public static int SVNAPI_1_5_x = 5;
	
	public static final ISVNClientWrapperFactory EMPTY = new ISVNClientWrapperFactory() {
		public ISVNClientWrapper newInstance() {
			throw new UnreportableException(this.getName());
		}
		public boolean isSSHOptionsAllowed() {
			return false;
		}
		public boolean isReportRevisionChangeAllowed() {
			return false;
		}
		public boolean isProxyOptionsAllowed() {
			return false;
		}
		public boolean isAtomicCommitAllowed() {
			return false;
		}
		public String getVersion() {
			return "";
		}
		public String getName() {
			return SVNTeamPlugin.instance().getResource(this.getId());
		}
		public String getId() {
			return "Error.NoSVNClient";
		}
		public String getCompatibilityVersion() {
			return ISVNClientWrapperFactory.CURRENT_COMPATIBILITY_VERSION;
		}
		public String getClientVersion() {
			return "";
		}
		public int getSVNAPIVersion() {
			return ISVNClientWrapperFactory.SVNAPI_1_5_x;
		}
	};
	
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
	 * Tell atomic commit is allowed or not
	 * @return true if allowed false otherwise
	 */
	public boolean isAtomicCommitAllowed();
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
	/**
	 * Tell which SVN API version supported
	 * @return API version Id
	 */
	public int getSVNAPIVersion();

}
