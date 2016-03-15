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
 * The abstract implementation of the IOptionProvider interface
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractOptionProvider implements IOptionProvider {
	public String []getCoveredProviders() {
		return null;
	}
	
	public ISVNCredentialsPrompt getCredentialsPrompt() {
		return null;
	}
	
	public ILoggedOperationFactory getLoggedOperationFactory() {
		return ILoggedOperationFactory.DEFAULT;
	}
	
	public void addProjectSetCapabilityProcessing(CompositeOperation op) {
	}
	
	public FileModificationValidator getFileModificationValidator() {
		return null;
	}
	
	public SVNProperty[] getAutomaticProperties(String template) {
		return new SVNProperty[0];
	}
	
	public String getResource(String key) {
		return SVNMessages.getErrorString(key);
	}
	
	public boolean is(String key) {
		Object value = this.get(key);
		return value instanceof Boolean && ((Boolean)value).booleanValue();
	}

	public boolean has(String key) {
		return this.get(key) != null;
	}
	
	public String getString(String key) {
		Object value = this.get(key);
		return value instanceof String ? (String)value : null;
	}
	
	public Object get(String key) {
		if (IOptionProvider.SVN_CONNECTOR_ID.equals(key)) {
			return this.getSVNConnectorId();
		}
		if (IOptionProvider.DEFAULT_TRUNK_NAME.equals(key)) {
			return this.getDefaultTrunkName();
		}
		if (IOptionProvider.DEFAULT_BRANCHES_NAME.equals(key)) {
			return this.getDefaultBranchesName();
		}
		if (IOptionProvider.DEFAULT_TAGS_NAME.equals(key)) {
			return this.getDefaultTagsName();
		}
		if (IOptionProvider.PERSISTENT_SSH_ENABLED.equals(key)) {
			return this.isPersistentSSHEnabled();
		}
		if (IOptionProvider.TEXT_MIME_TYPE_REQUIRED.equals(key)) {
			return this.isTextMIMETypeRequired();
		}
		if (IOptionProvider.SVN_CACHE_ENABLED.equals(key)) {
			return this.isSVNCacheEnabled();
		}
		if (IOptionProvider.AUTOMATIC_PROJECT_SHARE_ENABLED.equals(key)) {
			return this.isAutomaticProjectShareEnabled();
		}
		return null;
	}
	
	protected String getSVNConnectorId() {
		return ISVNConnectorFactory.DEFAULT_ID;
	}
	
	protected String getDefaultTrunkName() {
		return "trunk"; //$NON-NLS-1$
	}
	
	protected String getDefaultBranchesName() {
		return "branches"; //$NON-NLS-1$
	}
	
	protected String getDefaultTagsName() {
		return "tags"; //$NON-NLS-1$
	}
	
	protected boolean isAutomaticProjectShareEnabled() {
		return false;
	}
	
	protected boolean isSVNCacheEnabled() {
		return true;
	}
	
	protected boolean isTextMIMETypeRequired() {
		return true;
	}
	
	protected boolean isPersistentSSHEnabled() {
		return true;
	}
}
