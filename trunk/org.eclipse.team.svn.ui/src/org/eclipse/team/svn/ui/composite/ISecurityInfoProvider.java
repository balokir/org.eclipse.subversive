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

package org.eclipse.team.svn.ui.composite;

import org.eclipse.team.svn.core.resource.SSHSettings;
import org.eclipse.team.svn.core.resource.SSLSettings;

/**
 * This interface allows to transfer security information between repository location editor tabs
 * 
 * @author Alexander Gurov
 */
public interface ISecurityInfoProvider {
	public String getUsername();
	public void setUsername(String username);
	public String getPassword();
	public void setPassword(String password);
	
	public boolean isPasswordSaved();
	public void setPasswordSaved(boolean saved);
	
	public SSLSettings getSSLSettings();
	public SSHSettings getSSHSettings();
	
	public void commit();
}
