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

package org.eclipse.team.svn.tests;

import java.net.URL;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Alexander Gurov
 */
public class TestPlugin extends AbstractUIPlugin {
	private static TestPlugin plugin;
	private String location;
	private SVNRemoteStorage remoteStorage;
	
	public TestPlugin() {
		super();
	}

	public String getLocation() {
	    return this.location;
	}
	
    public ResourceBundle getResourceBundle() {
        return Platform.getResourceBundle(this.getBundle());
    }
    
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		//NOTE incompatible with Eclipse 3.0, compatible one is: 		URL url = Platform.asLocalURL(context.getBundle().getEntry("/"));
		URL url = FileLocator.toFileURL(context.getBundle().getEntry("/"));
		this.location = url.getFile();
		if (this.location.startsWith("/")) {
		    this.location = this.location.substring(1);
		}
		this.remoteStorage = SVNRemoteStorage.instance();
		this.remoteStorage.initialize(this.getStateLocation());
		
		SVNTeamPlugin.instance().setOptionProvider(null);

		TestPlugin.plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	public static TestPlugin instance() {
		while (plugin == null) {
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
		}
		
		return plugin;
	}

}
