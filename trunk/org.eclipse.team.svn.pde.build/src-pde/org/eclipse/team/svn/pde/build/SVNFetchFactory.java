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

package org.eclipse.team.svn.pde.build;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.build.Constants;
import org.eclipse.pde.build.IAntScript;
import org.eclipse.pde.build.IFetchFactory;
import org.eclipse.pde.internal.build.Utils;

/**
 * Implementation of SVN fetch task factory for PDE build
 * 
 * @author Alexander Gurov
 */
public class SVNFetchFactory implements IFetchFactory {
	public static final String TARGET_FETCH_FROM_SVN = "FetchFromSVN";
	public static final String MAP_ENTRY_SEPARATOR = ",";
	public static final String VALUE_PAIR_SEPARATOR = "=";
	
	public static final String OVERRIDE_TAG = "SVN";

	public static final String KEY_URL = "url";
	public static final String KEY_PEG = "peg";
	public static final String KEY_REVISION = "revision";
	public static final String KEY_PATH = "path";
	public static final String KEY_USERNAME = "username";
	public static final String KEY_PASSWORD = "password";
	
	public static final String PROP_FILETOCHECK = "fileToCheck";
	public static final String PROP_ELEMENTNAME = "elementName";
	public static final String PROP_DESTINATIONFOLDER = "destinationFolder";
	public static final String PROP_URL = SVNFetchFactory.KEY_URL;
	public static final String PROP_PEG = SVNFetchFactory.KEY_PEG;
	public static final String PROP_REVISION = SVNFetchFactory.KEY_REVISION;
	public static final String PROP_TAG = IFetchFactory.KEY_ELEMENT_TAG;
	public static final String PROP_PATH = SVNFetchFactory.KEY_PATH;
	public static final String PROP_USERNAME = SVNFetchFactory.KEY_USERNAME;
	public static final String PROP_PASSWORD = SVNFetchFactory.KEY_PASSWORD;

	public SVNFetchFactory() {

	}

	public void addTargets(IAntScript script) {
		script.printTargetDeclaration(SVNFetchFactory.TARGET_FETCH_FROM_SVN, null, null, "${" + SVNFetchFactory.PROP_FILETOCHECK + "}", null);
		this.printSVNTask("export", "${" + SVNFetchFactory.PROP_URL + "}/${" + SVNFetchFactory.PROP_TAG + "}/${" + SVNFetchFactory.PROP_PATH + "}", "${" + SVNFetchFactory.PROP_PEG + "}", "${" + SVNFetchFactory.PROP_REVISION + "}", "${" + SVNFetchFactory.PROP_DESTINATIONFOLDER + "}/${" + SVNFetchFactory.PROP_ELEMENTNAME + "}", "${" + SVNFetchFactory.PROP_USERNAME + "}", "${" + SVNFetchFactory.PROP_PASSWORD + "}", script);
		script.printTargetEnd();
	}

	public void generateRetrieveElementCall(Map entryInfos, IPath destination, IAntScript script) {
		String type = (String) entryInfos.get(KEY_ELEMENT_TYPE);
		String element = (String) entryInfos.get(KEY_ELEMENT_NAME);

		HashMap<String, String> params = new HashMap<String, String>();
		
		IPath locationToCheck = (IPath)destination.clone();
		if (type.equals(ELEMENT_TYPE_FEATURE)) {
			locationToCheck = locationToCheck.append(Constants.FEATURE_FILENAME_DESCRIPTOR);
		}
		else if (type.equals(ELEMENT_TYPE_PLUGIN)) {
			locationToCheck = locationToCheck.append(Constants.PLUGIN_FILENAME_DESCRIPTOR);
		}
		else if (type.equals(ELEMENT_TYPE_FRAGMENT)) {
			locationToCheck = locationToCheck.append(Constants.FRAGMENT_FILENAME_DESCRIPTOR);
		}
		else if (type.equals(ELEMENT_TYPE_BUNDLE)) {
			locationToCheck = locationToCheck.append(Constants.BUNDLE_FILENAME_DESCRIPTOR);
		}
		params.put(SVNFetchFactory.PROP_ELEMENTNAME, element);
		params.put(SVNFetchFactory.PROP_FILETOCHECK, locationToCheck.toString());
		params.put(SVNFetchFactory.PROP_DESTINATIONFOLDER, destination.removeLastSegments(1).toString());
		params.put(SVNFetchFactory.PROP_URL, (String)entryInfos.get(SVNFetchFactory.KEY_URL));
		if (entryInfos.containsKey(SVNFetchFactory.KEY_PEG)) {
			params.put(SVNFetchFactory.PROP_PEG, (String)entryInfos.get(SVNFetchFactory.KEY_PEG));
		}
		else {
			params.put(SVNFetchFactory.PROP_PEG, "HEAD");
		}
		if (entryInfos.containsKey(SVNFetchFactory.KEY_REVISION)) {
			params.put(SVNFetchFactory.PROP_REVISION, (String)entryInfos.get(SVNFetchFactory.KEY_REVISION));
		}
		else {
			params.put(SVNFetchFactory.PROP_REVISION, "HEAD");
		}
		params.put(SVNFetchFactory.PROP_TAG, (String)entryInfos.get(SVNFetchFactory.KEY_ELEMENT_TAG));
		params.put(SVNFetchFactory.PROP_PATH, (String)entryInfos.get(SVNFetchFactory.KEY_PATH));
		String username = (String)entryInfos.get(SVNFetchFactory.KEY_USERNAME);
		params.put(SVNFetchFactory.PROP_USERNAME, username != null ? username : "");
		String password = (String)entryInfos.get(SVNFetchFactory.KEY_PASSWORD);
		params.put(SVNFetchFactory.PROP_PASSWORD, password != null ? password : "");
		
		this.printAvailableTask(locationToCheck.toString(), locationToCheck.toString(), script);
		if (IFetchFactory.ELEMENT_TYPE_PLUGIN.equals(type) || IFetchFactory.ELEMENT_TYPE_FRAGMENT.equals(type)) {
			this.printAvailableTask(locationToCheck.toString(), locationToCheck.removeLastSegments(1).append(Constants.BUNDLE_FILENAME_DESCRIPTOR).toString(), script);
		}
		
		script.printAntCallTask(SVNFetchFactory.TARGET_FETCH_FROM_SVN, true, params);
	}

	public void generateRetrieveFilesCall(Map entryInfos, IPath destination, String[] files, IAntScript script) {
		String rootUrl = (String)entryInfos.get(SVNFetchFactory.KEY_URL);
		String pegRev = (String)entryInfos.get(SVNFetchFactory.KEY_PEG);
		String rev = (String)entryInfos.get(SVNFetchFactory.KEY_REVISION);
		
		String tag = (String)entryInfos.get(IFetchFactory.KEY_ELEMENT_TAG);
		
		String path = (String)entryInfos.get(SVNFetchFactory.KEY_PATH);
		
		String baseUrl = rootUrl + "/" + tag + "/" + path + "/";
		String dest = destination.toString();
		
		String username = (String)entryInfos.get(SVNFetchFactory.KEY_USERNAME);
		String password = (String)entryInfos.get(SVNFetchFactory.KEY_PASSWORD);
		for (String fileName : files) {
			this.printSVNTask("cat", baseUrl + fileName, pegRev, rev, dest + "/" + fileName, username, password, script);
		}
	}

	/*
	 * Map file entry format:
	 * mapEntry
	 * 	:	elementType '@' elementID (',' elementVersion)? = svnContent
	 * 	;
	 * elementType
	 * 	:	'bundle' | 'feature' | 'plugin' | 'fragment'
	 * 	;
	 * elementID
	 * 	:	... //plug-in, feature, fragment or bundle ID
	 * 	;
	 * elementVersion
	 *  :	... //plug-in, feature, fragment or bundle version
	 *  ;
	 * svnContent
	 * 	:	'SVN' (',' arg)+
	 * 	;
	 * arg
	 * 	:	key '=' value
	 * 	;
	 * key
	 * 	:	'url'		// project root URL
	 * 	|	'tag'		// optional tag name (trunk, tags/some_name etc.)
	 * 	|	'path'		// optional element, path relative to project root URL
	 * 	|	'revision'	// optional element, revision
	 * 	|	'peg'		// optional element, peg revision
	 * 	|	'username'
	 * 	|	'password'
	 * 	;
	 */
	public void parseMapFileEntry(String rawEntry, Properties overrideTags, Map entryInfos) throws CoreException {
		String []arguments = Utils.getArrayFromStringWithBlank(rawEntry, SVNFetchFactory.MAP_ENTRY_SEPARATOR);
		
		// check entry count here....
		
		for (String argument : arguments) {
			int idx = argument.indexOf(SVNFetchFactory.VALUE_PAIR_SEPARATOR);
			if (idx != -1) {
				String key = argument.substring(0, idx);
				String value = argument.substring(idx + 1);
				entryInfos.put(key, value);
			}
		}
		
		if (overrideTags != null) {
			String overrideTag = overrideTags.getProperty(SVNFetchFactory.OVERRIDE_TAG);
			if (overrideTag != null && overrideTag.length() > 0) {
				entryInfos.put(IFetchFactory.KEY_ELEMENT_TAG, overrideTag);
			}
		}
		// handle optional path
		String path = (String)entryInfos.get(SVNFetchFactory.KEY_PATH);
		if (path == null) {
			entryInfos.put(SVNFetchFactory.KEY_PATH, entryInfos.get(IFetchFactory.KEY_ELEMENT_NAME));
		}
		// handle optional tag
		String tag = (String)entryInfos.get(IFetchFactory.KEY_ELEMENT_TAG);
		if (tag == null) {
			entryInfos.put(IFetchFactory.KEY_ELEMENT_TAG, "");
		}
	}

	protected void printSVNTask(String command, String url, String pegRev, String rev, String dest, String username, String password, IAntScript script) {
		script.printTabs();
		script.print("<svn");
		script.printAttribute("command", command, false);
		script.printAttribute("url", url, false);
		script.printAttribute("pegRev", pegRev, false);
		script.printAttribute("rev", rev, false);
		script.printAttribute("dest", dest, false);
		script.printAttribute("username", username, false);
		script.printAttribute("password", password, false);
		script.println("/>");
	}
	
	protected void printAvailableTask(String property, String file, IAntScript script) {
		script.printTabs();
		script.print("<available");
		script.printAttribute("property", property, true);
		script.printAttribute("file", file, false);
		script.println("/>");
	}
	
}
