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

package org.eclipse.team.svn.mylyn;

import org.eclipse.core.runtime.Platform;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Mylyn integration entry point.
 * 
 * @author Alexander Gurov
 */
public class SVNMylynIntegrationPlugin extends AbstractUIPlugin {
	public static final String ID = "org.eclipse.team.svn.mylyn";
	
	private static SVNMylynIntegrationPlugin instance;
	
	public SVNMylynIntegrationPlugin() {
		super();
		SVNMylynIntegrationPlugin.instance = this;
	}
	
    public static SVNMylynIntegrationPlugin instance() {
    	return SVNMylynIntegrationPlugin.instance;
    }
    
    public String getResource(String key) {
        return FileUtility.getResource(Platform.getResourceBundle(this.getBundle()), key);
    }
    
}
