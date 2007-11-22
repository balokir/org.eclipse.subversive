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

package org.eclipse.team.svn.core.extension.options;

import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.ISVNCredentialsPrompt;
import org.eclipse.team.svn.core.client.SVNProperty;
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
		public boolean getReportRevisionChange() {
			return false;
		}
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
			return SVNTeamPlugin.instance().getResource(key);
		}
	};
	
	public FileModificationValidator getFileModificationValidator();
	public ISVNCredentialsPrompt getCredentialsPrompt();
	public boolean getReportRevisionChange();
	public ILoggedOperationFactory getLoggedOperationFactory();
	public void addProjectSetCapabilityProcessing(CompositeOperation op);
	public boolean isAutomaticProjectShareEnabled();
	public String getSVNConnectorId();
	public SVNProperty[] getAutomaticProperties(String template);
	
	public boolean isSVNCacheEnabled();
	
	public String getDefaultTrunkName();
	public String getDefaultBranchesName();
	public String getDefaultTagsName();
	
	public String getResource(String key);
}
