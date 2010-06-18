/*******************************************************************************
 * Copyright (c) 2005-2010 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo (Polarion Software) - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;

/**
 * SVN revision graph plugin preference names
 * 
 * @author Igor Burilo
 */
public class SVNRevisionGraphPreferences {

	public static final String CACHE_BASE = "preference.cache."; //$NON-NLS-1$
	
	public static final String CACHE_DIRECTORY_NAME = "directory"; //$NON-NLS-1$
	
	private SVNRevisionGraphPreferences() {	
	}
	
	public static void setDefaultValues(IPreferenceStore store) {
		store.setDefault(SVNRevisionGraphPreferences.fullCacheName(SVNRevisionGraphPreferences.CACHE_DIRECTORY_NAME), SVNRevisionGraphPlugin.instance().getStateLocation().toString());
	}

	public static String getDefaultCacheString(IPreferenceStore store, String shortName) {
		return store.getDefaultString(SVNRevisionGraphPreferences.fullCacheName(shortName));
	}
	
	public static String getCacheString(IPreferenceStore store, String shortName) {
		return store.getString(SVNRevisionGraphPreferences.fullCacheName(shortName));
	}
	
	public static void setCacheString(IPreferenceStore store, String shortName, String value) {
		store.setValue(SVNRevisionGraphPreferences.fullCacheName(shortName), value);
	}
	
	public static String fullCacheName(String shortName) {
		return SVNRevisionGraphPreferences.CACHE_BASE + shortName;
	}
}
