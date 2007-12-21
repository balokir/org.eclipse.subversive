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

package org.eclipse.team.svn.mylyn;

import org.eclipse.core.runtime.Platform;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Entry point.
 * 
 * @author Alexander Gurov
 */
public class SVNMylynIntegrationPlugin extends AbstractUIPlugin {
	private static SVNMylynIntegrationPlugin instance;
	
    public static SVNMylynIntegrationPlugin instance() {
    	while (SVNMylynIntegrationPlugin.instance == null) {
    		try {Thread.sleep(100);} catch (InterruptedException ex) {break;}
    	}
    	return SVNMylynIntegrationPlugin.instance;
    }
    
	public void start(BundleContext context) throws Exception {
		super.start(context);
		SVNMylynIntegrationPlugin.instance = this;
	}
	
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}
	
    public String getResource(String key) {
        return FileUtility.getResource(Platform.getResourceBundle(this.getBundle()), key);
    }
    
}
