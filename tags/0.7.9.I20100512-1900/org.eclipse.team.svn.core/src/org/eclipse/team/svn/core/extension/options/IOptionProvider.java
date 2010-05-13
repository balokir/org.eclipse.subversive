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
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNCredentialsPrompt;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.utility.ILoggedOperationFactory;

/**
 * This interface allows us to provide repository management options to IRepositoryLocation instances
 * 
 * @author Alexander Gurov
 */
public interface IOptionProvider {
	public static final IOptionProvider DEFAULT = new IOptionProvider() {
		public ISVNCredentialsPrompt getCredentialsPrompt() {
			return null;
		}
		public ILoggedOperationFactory getLoggedOperationFactory() {
			return ILoggedOperationFactory.DEFAULT;
		}
		public void addProjectSetCapabilityProcessing(CompositeOperation op) {
		}
		public boolean isAutomaticProjectShareEnabled() {
			return false;
		}
		public FileModificationValidator getFileModificationValidator() {
			return null;
		}
		public boolean isSVNCacheEnabled() {
			return true;
		}
		public String getSVNConnectorId() {
			return ISVNConnectorFactory.DEFAULT_ID;
		}
		public String getDefaultBranchesName() {
			return "branches";
		}
		public String getDefaultTagsName() {
			return "tags";
		}
		public String getDefaultTrunkName() {
			return "trunk";
		}
		public SVNProperty[] getAutomaticProperties(String template) {
			return new SVNProperty[0];
		}
		public String getResource(String key) {
			return SVNMessages.getErrorString(key);
		}
	};
	
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
	 * Returns <code>true</code> if auto-share is enabled
	 * @return <code>true</code> if auto-share is enabled, false otherwise
	 */
	public boolean isAutomaticProjectShareEnabled();
	/**
	 * Returns preferred SVN connector plug-in id
	 * @return preferred SVN connector plug-in id
	 */
	public String getSVNConnectorId();
	/**
	 * Returns set of automatic properties
	 * @param template resource name template
	 * @return set of properties
	 */
	public SVNProperty[] getAutomaticProperties(String template);
	
	/**
	 * Tells if SVN meta-information cache is enabled
	 * @return <code>true</code> if cache is enabled, <code>false</code> otherwise
	 */
	public boolean isSVNCacheEnabled();
	
	/**
	 * Returns default trunk name
	 * @return default trunk name
	 */
	public String getDefaultTrunkName();
	/**
	 * Returns default branches name
	 * @return default branches name
	 */
	public String getDefaultBranchesName();
	/**
	 * Returns default tags name
	 * @return default tags name
	 */
	public String getDefaultTagsName();
	
	/**
	 * Provides access to internationalization strings
	 * @return nationalized value
	 */
	public String getResource(String key);
}
