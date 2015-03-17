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

package org.eclipse.team.svn.test;

import java.util.ResourceBundle;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Alexander Gurov
 */
public class TestPlugin extends AbstractUIPlugin {
	private static TestPlugin plugin;
	
	public TestPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This function returns plugin resource bundle
	 * @return java.lang.ResourceBundle
	 */
    public ResourceBundle getResourceBundle() {
        return Platform.getResourceBundle(this.getBundle());
    }
    
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static TestPlugin instance() {
		return plugin;
	}

}
