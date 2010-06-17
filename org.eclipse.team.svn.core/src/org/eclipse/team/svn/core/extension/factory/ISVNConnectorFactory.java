/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.extension.factory;

import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.UnreportableException;

/**
 * Extension point factory for ISVNClientWrapper
 * 
 * @author Alexander Gurov
 */
public interface ISVNConnectorFactory {
	public static final String DEFAULT_ID = "org.eclipse.team.svn.connector.svnkit15"; //$NON-NLS-1$
	public static final String CURRENT_COMPATIBILITY_VERSION = "0.7.9.I20100617-1700"; //$NON-NLS-1$
	
	/**
	 * Enumeration of connector API compatibility levels
	 */
	public static class APICompatibility {
		/**
		 * Compatibility level for the connector library is not specified
		 */
		public static final int SVNAPI_NOT_SPECIFIED = -1;
		/**
		 * SVN 1.0 compatible API is supported by the connector
		 */
		public static final int SVNAPI_1_0_x = 0;
		/**
		 * SVN 1.1 compatible API is supported by the connector
		 */
		public static final int SVNAPI_1_1_x = 1;
		/**
		 * SVN 1.2 compatible API is supported by the connector
		 */
		public static final int SVNAPI_1_2_x = 2;
		/**
		 * SVN 1.3 compatible API is supported by the connector
		 */
		public static final int SVNAPI_1_3_x = 3;
		/**
		 * SVN 1.4 compatible API is supported by the connector
		 */
		public static final int SVNAPI_1_4_x = 4;
		/**
		 * SVN 1.5 compatible API is supported by the connector
		 */
		public static final int SVNAPI_1_5_x = 5;
		/**
		 * SVN 1.6 compatible API is supported by the connector
		 */
		public static final int SVNAPI_1_6_x = 6;
	}
	
	/**
	 * Enumeration of optional feature masks 
	 */
	public static class OptionalFeatures {
		/**
		 * No optional features supported
		 */
		public static final int NO_OPTIONAL_FEATURES = 0;
		/**
		 * All optional features supported
		 */
		public static final int ALL_OPTIONAL_FEATURES = ~NO_OPTIONAL_FEATURES;
		/**
		 * Direct SSH settings specification is supported by connector
		 */
		public static final int SSH_SETTINGS = 0x01;
		/**
		 * Direct PROXY settings specification is supported by connector
		 */
		public static final int PROXY_SETTINGS = 0x02;
		/**
		 * Atomic cross-working copy commit is supported by connector 
		 */
		public static final int ATOMIC_X_COMMIT = 0x04;
		/**
		 * Creating of repository is supported by connector
		 */
		public static final int CREATE_REPOSITORY = 0x08;	
		/**
		 * Features supported by SVNKit connectors
		 */
		public static final int SVN_KIT_FEATURES = SSH_SETTINGS | PROXY_SETTINGS | ATOMIC_X_COMMIT;
		/**
		 * Features supported by JavaHL connectors
		 */
		public static final int JAVAHL_FEATURES = CREATE_REPOSITORY;
	}
	
	public static final ISVNConnectorFactory EMPTY = new ISVNConnectorFactory() {
		public ISVNConnector newInstance() {
			throw new UnreportableException(this.getName());
		}
		public int getSupportedFeatures() {
			return OptionalFeatures.NO_OPTIONAL_FEATURES;
		}
		public String getVersion() {
			return ""; //$NON-NLS-1$
		}
		public String getName() {
			return SVNMessages.getErrorString(this.getId());
		}
		public String getId() {
			return "Error_NoSVNClient"; //$NON-NLS-1$
		}
		public String getCompatibilityVersion() {
			return ISVNConnectorFactory.CURRENT_COMPATIBILITY_VERSION;
		}
		public String getClientVersion() {
			return ""; //$NON-NLS-1$
		}
		public int getSVNAPIVersion() {
			return APICompatibility.SVNAPI_NOT_SPECIFIED;
		}
	};
	
	/**
	 * Makes new SVN Client Library instance
	 * @return SVN Client Library instance
	 */
	public ISVNConnector newInstance();
	
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
	 * @return connector version
	 */
	public String getClientVersion();
	
	/**
	 * Returns supported optional features set 
	 * @return supported optional features set
	 */
	public int getSupportedFeatures();
	/**
	 * Tell which SVN API version supported
	 * @return API version Id
	 */
	public int getSVNAPIVersion();

}
