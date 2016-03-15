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

package org.eclipse.team.svn.core.extension.options;

import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.team.svn.core.connector.ISVNCredentialsPrompt;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.utility.ILoggedOperationFactory;

/**
 * FIXME Introduce integration API changes
 * 
 * This interface allows us to provide repository management options to IRepositoryLocation instances
 * 
 * @author Alexander Gurov
 */
public interface IOptionProvider {
	/**
	 * Preferred SVN connector plug-in ID
	 */
	public static String SVN_CONNECTOR_ID = "svnConnectorId"; //$NON-NLS-1$
	/**
	 * Default trunk folder name
	 */
	public static String DEFAULT_TRUNK_NAME = "defaultTrunkName"; //$NON-NLS-1$
	/**
	 * Default branches folder name
	 */
	public static String DEFAULT_BRANCHES_NAME = "defaultBranchesName"; //$NON-NLS-1$
	/**
	 * Default tags folder name
	 */
	public static String DEFAULT_TAGS_NAME = "defaultTagsName"; //$NON-NLS-1$
	/**
	 * <code>true</code> if persistent SSH connections are enabled, <code>false<code> or <code>null</code> otherwise
	 */
	public static String PERSISTENT_SSH_ENABLED = "persistentSSHEnabled"; //$NON-NLS-1$
	/**
	 * <code>true</code> if SVN status cache is enabled, <code>false<code> or <code>null</code> otherwise
	 */
	public static String SVN_CACHE_ENABLED = "svnCacheEnabled"; //$NON-NLS-1$
	/**
	 * <code>true</code> if "text" MIME-type should be set, <code>false<code> or <code>null</code> otherwise
	 */
	public static String TEXT_MIME_TYPE_REQUIRED = "textMIMETypeRequired"; //$NON-NLS-1$
	/**
	 * <code>true</code> if auto-share is enabled, <code>false<code> or <code>null</code> otherwise
	 */
	public static String AUTOMATIC_PROJECT_SHARE_ENABLED = "automaticProjectShareEnabled"; //$NON-NLS-1$
	
	public static final IOptionProvider DEFAULT = new AbstractOptionProvider() {
		public String getId() {
			return "org.eclipse.team.svn.core.optionprovider"; //$NON-NLS-1$
		}
	};
	
	/**
	 * Returns option provider ID
	 * @return {@link String}
	 */
	public String getId();
	
	/**
	 * Returns a set of option provider identifiers this one is superior to or <code>null</code> if there are none.
	 * @return String []
	 */
	public String []getCoveredProviders();
	
	/**
	 * Returns read-only files modification validator
	 * @return read-only files modification validator
	 */
	public FileModificationValidator getFileModificationValidator();
	/**
	 * Provides credentials prompt call-back
	 * @return credentials prompt call-back
	 */
	public ISVNCredentialsPrompt getCredentialsPrompt();
	/**
	 * Provide logged operation factory which allows to override exceptions handling
	 * @return logged operation factory instance
	 */
	public ILoggedOperationFactory getLoggedOperationFactory();
	/**
	 * Installs additional handlers into project set processing workflow
	 * @param op project set processing workflow
	 */
	public void addProjectSetCapabilityProcessing(CompositeOperation op);
	/**
	 * Returns set of automatic properties
	 * @param template resource name template
	 * @return set of properties
	 */
	public SVNProperty[] getAutomaticProperties(String template);
	
	/**
	 * Provides access to internationalization strings
	 * @return nationalized value
	 */
	public String getResource(String key);

	/**
	 * Returns <code>true</code> if the option is a <code>boolean</code> value and set to <code>true</code>
	 * @param key the option's name
	 * @return
	 */
	public boolean is(String key);

	/**
	 * Returns <code>true</code> if the option is set and is not <code>null</code>
	 * @param key the option's name
	 * @return
	 */
	public boolean has(String key);
	
	/**
	 * Returns the string value of the option. If the value is not a string returns <code>null</code>;
	 * @param key the option's name
	 * @return
	 */
	public String getString(String key);
	
	/**
	 * Returns the specified option value
	 * @param key option name
	 * @return
	 */
	public Object get(String key);
}
