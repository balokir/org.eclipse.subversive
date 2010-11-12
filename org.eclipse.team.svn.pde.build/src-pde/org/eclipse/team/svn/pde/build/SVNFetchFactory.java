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
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.build.Constants;
import org.eclipse.pde.build.IAntScript;
import org.eclipse.pde.build.IFetchFactory;
import org.eclipse.pde.internal.build.Utils;
import org.eclipse.team.svn.pde.build.IMapFileParser.FetchData;

/**
 * Implementation of SVN fetch task factory for PDE build
 * 
 * It understands our and SourceForge svn pde builders map file formats
 * 
 * @author Alexander Gurov
 */
public class SVNFetchFactory implements IFetchFactory {
	public static final String TARGET_FETCH_FROM_SVN = "FetchFromSVN"; //$NON-NLS-1$
	public static final String MAP_ENTRY_SEPARATOR = ","; //$NON-NLS-1$
	public static final String VALUE_PAIR_SEPARATOR = "="; //$NON-NLS-1$
	
	public static final String OVERRIDE_TAG = "SVN"; //$NON-NLS-1$

	public static final String KEY_URL = "url"; //$NON-NLS-1$
	public static final String KEY_PEG = "peg"; //$NON-NLS-1$
	public static final String KEY_TAG_PATH = "tagPath"; //$NON-NLS-1$
	public static final String KEY_REVISION = "revision"; //$NON-NLS-1$
	public static final String KEY_PATH = "path"; //$NON-NLS-1$
	public static final String KEY_USERNAME = "username"; //$NON-NLS-1$
	public static final String KEY_PASSWORD = "password"; //$NON-NLS-1$
	public static final String KEY_FORCE = "force"; //$NON-NLS-1$
	
	public static final String PROP_FILETOCHECK = "fileToCheck"; //$NON-NLS-1$
	public static final String PROP_ELEMENTNAME = "elementName"; //$NON-NLS-1$
	public static final String PROP_DESTINATIONFOLDER = "destinationFolder"; //$NON-NLS-1$
	public static final String PROP_URL = SVNFetchFactory.KEY_URL;
	public static final String PROP_PEG = SVNFetchFactory.KEY_PEG;
	public static final String PROP_REVISION = SVNFetchFactory.KEY_REVISION;
	public static final String PROP_TAG = IFetchFactory.KEY_ELEMENT_TAG;
	public static final String PROP_TAG_PATH = SVNFetchFactory.KEY_TAG_PATH;
	public static final String PROP_PATH = SVNFetchFactory.KEY_PATH;
	public static final String PROP_USERNAME = SVNFetchFactory.KEY_USERNAME;
	public static final String PROP_PASSWORD = SVNFetchFactory.KEY_PASSWORD;
	public static final String PROP_FORCE = KEY_FORCE;

	protected static Pattern tagPattern = Pattern.compile("[\\.a-zA-Z_0-9-]+"); //$NON-NLS-1$
	
	public SVNFetchFactory() {

	}

	public void addTargets(IAntScript script) {
		script.printTargetDeclaration(SVNFetchFactory.TARGET_FETCH_FROM_SVN, null, null, "${" + SVNFetchFactory.PROP_FILETOCHECK + "}", null); //$NON-NLS-1$ //$NON-NLS-2$
		this.printSVNTask("export", "${" + SVNFetchFactory.PROP_URL + "}/${" + SVNFetchFactory.PROP_TAG_PATH + "}/${" + SVNFetchFactory.PROP_PATH + "}", "${" + SVNFetchFactory.PROP_PEG + "}", "${" + SVNFetchFactory.PROP_REVISION + "}", "${" + SVNFetchFactory.PROP_DESTINATIONFOLDER + "}/${" + SVNFetchFactory.PROP_ELEMENTNAME + "}", "${" + SVNFetchFactory.PROP_USERNAME + "}", "${" + SVNFetchFactory.PROP_PASSWORD + "}", script, "${" + SVNFetchFactory.PROP_FORCE + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$
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
		
		/*
		original code doesn't support same plugin with multiple plugins
		to be downloaded into ${buildDirectory}/plugins/
		example:
			com.compuware.bundles.axis (1.2.1)
			com.compuware.bundles.axis (1.4.0)
		both specified in feature.xml and .map
		The fetch script generated by original code always trying to 
		download to one folder called
		${buildDirectory}/plugins/com.compuware.bundles.axis
		Do not like CVSFetchFactory, if you specify a version to the plugin in the map, for example, 'plugin@xxx,1.2.1=...'
		it will download to 
		${buildDirectory}/plugins/com.compuware.bundles.axis_1.2.1 and
		${buildDirectory}/plugins/com.compuware.bundles.axis_1.4.0 respectively.
		This is needed when we want to build multiple versions of same bundle in a project
		like Orbit or Compuware Commons. 
		The following patch will solve the problem. 
		*/
		// original code
		// params.put(SVNFetchFactory.PROP_ELEMENTNAME, element);				
		if(org.osgi.framework.Version.emptyVersion.equals(entryInfos.get("internal.matchedVersion"))) {  //$NON-NLS-1$
			params.put(SVNFetchFactory.PROP_ELEMENTNAME, element);
		} else {
			params.put(SVNFetchFactory.PROP_ELEMENTNAME, element + "_" + entryInfos.get("internal.matchedVersion")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		params.put(SVNFetchFactory.PROP_FILETOCHECK, locationToCheck.toString());
		params.put(SVNFetchFactory.PROP_DESTINATIONFOLDER, destination.removeLastSegments(1).toString());
		params.put(SVNFetchFactory.PROP_URL, (String)entryInfos.get(SVNFetchFactory.KEY_URL));
		if (entryInfos.containsKey(SVNFetchFactory.KEY_PEG)) {
			params.put(SVNFetchFactory.PROP_PEG, (String)entryInfos.get(SVNFetchFactory.KEY_PEG));
		}
		else {
			params.put(SVNFetchFactory.PROP_PEG, "HEAD"); //$NON-NLS-1$
		}
		if (entryInfos.containsKey(SVNFetchFactory.KEY_REVISION)) {
			params.put(SVNFetchFactory.PROP_REVISION, (String)entryInfos.get(SVNFetchFactory.KEY_REVISION));
		}
		else {
			params.put(SVNFetchFactory.PROP_REVISION, "HEAD"); //$NON-NLS-1$
		}
		params.put(SVNFetchFactory.PROP_TAG, (String)entryInfos.get(SVNFetchFactory.KEY_ELEMENT_TAG));
		params.put(SVNFetchFactory.PROP_TAG_PATH, (String)entryInfos.get(SVNFetchFactory.KEY_TAG_PATH));
		params.put(SVNFetchFactory.PROP_PATH, (String)entryInfos.get(SVNFetchFactory.KEY_PATH));
		String username = (String)entryInfos.get(SVNFetchFactory.KEY_USERNAME);
		params.put(SVNFetchFactory.PROP_USERNAME, username != null ? username : ""); //$NON-NLS-1$
		String password = (String)entryInfos.get(SVNFetchFactory.KEY_PASSWORD);
		params.put(SVNFetchFactory.PROP_PASSWORD, password != null ? password : ""); //$NON-NLS-1$
		params.put(SVNFetchFactory.PROP_FORCE, this.getBooleanValue(entryInfos, SVNFetchFactory.KEY_FORCE));
		
		this.printAvailableTask(locationToCheck.toString(), locationToCheck.toString(), script);
		if (IFetchFactory.ELEMENT_TYPE_PLUGIN.equals(type) || IFetchFactory.ELEMENT_TYPE_FRAGMENT.equals(type)) {
			this.printAvailableTask(locationToCheck.toString(), locationToCheck.removeLastSegments(1).append(Constants.BUNDLE_FILENAME_DESCRIPTOR).toString(), script);
		}
		
		script.printAntCallTask(SVNFetchFactory.TARGET_FETCH_FROM_SVN, true, params);
	}

	protected String getBooleanValue(Map entryInfos, String key) {
		String res = "false"; //$NON-NLS-1$
		String str = (String) entryInfos.get(key);
		if (str != null) {
			res = Boolean.valueOf(str).toString();
		}
		return res;		
	}
	
	public void generateRetrieveFilesCall(Map entryInfos, IPath destination, String[] files, IAntScript script) {
		String rootUrl = (String)entryInfos.get(SVNFetchFactory.KEY_URL);
		String pegRev = (String)entryInfos.get(SVNFetchFactory.KEY_PEG);
		String rev = (String)entryInfos.get(SVNFetchFactory.KEY_REVISION);
		
		String tag = (String)entryInfos.get(SVNFetchFactory.KEY_TAG_PATH);
		String path = (String)entryInfos.get(SVNFetchFactory.KEY_PATH);
		String baseUrl = rootUrl;
		if (tag != null && tag.length() > 0) {
			baseUrl += "/" + tag; //$NON-NLS-1$
		}
		if (path != null && path.length() > 0) {
			baseUrl += "/" + path; //$NON-NLS-1$
		}
		baseUrl += "/"; //$NON-NLS-1$
					
		String dest = destination.toString();
		String username = (String)entryInfos.get(SVNFetchFactory.KEY_USERNAME);
		String password = (String)entryInfos.get(SVNFetchFactory.KEY_PASSWORD);
		String force = this.getBooleanValue(entryInfos, SVNFetchFactory.KEY_FORCE);
						
		for (String fileName : files) {
			this.printSVNTask("cat", baseUrl + fileName, pegRev, rev, dest + "/" + fileName, username, password, script, force); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void parseMapFileEntry(String rawEntry, Properties overrideTags, Map entryInfos) throws CoreException {
		String []arguments = Utils.getArrayFromStringWithBlank(rawEntry, SVNFetchFactory.MAP_ENTRY_SEPARATOR);		
		IMapFileParser parser = SVNFetchFactory.getMapFileParser(arguments);
		FetchData data = parser.parse(rawEntry, arguments, overrideTags);
						
		//fill entryInfo
		entryInfos.put(SVNFetchFactory.KEY_URL, data.url);
		entryInfos.put(SVNFetchFactory.KEY_PATH, data.path);
		if (data.tag != null && data.tag.length() > 0) {
			int ind = data.tag.lastIndexOf("/"); //$NON-NLS-1$
			String tagValue = ind > 0 ? data.tag.substring(ind + 1) : data.tag;
			String tagPath = data.tag;
									
			//validate tag
			if (tagValue != null && !SVNFetchFactory.tagPattern.matcher(tagValue).matches()) {
				throw new RuntimeException("Tag doesn't match to pattern. Tag: " + tagValue + ", pattern: " + SVNFetchFactory.tagPattern.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			}	
			if (tagValue != null) {
				entryInfos.put(SVNFetchFactory.KEY_ELEMENT_TAG, tagValue);	
			}				
			entryInfos.put(SVNFetchFactory.KEY_TAG_PATH, tagPath);
		}		
		
		if (data.revision != null) {
			entryInfos.put(SVNFetchFactory.KEY_REVISION, data.revision);	
		}		
		if (data.peg != null) {
			entryInfos.put(SVNFetchFactory.KEY_PEG, data.peg);	
		}		
		if (data.username != null) {
			entryInfos.put(SVNFetchFactory.KEY_USERNAME, data.username);	
		}
		if (data.password != null) {
			entryInfos.put(SVNFetchFactory.KEY_PASSWORD, data.password);	
		}
		if (data.force != null) {
			entryInfos.put(SVNFetchFactory.KEY_FORCE, data.force);	
		}		
	}
	
	public static IMapFileParser getMapFileParser(String[] arguments) {
		boolean isDefault = false;
		//default handler contains key=value pair, where key is 'url' for second element
		String arg = arguments[0];					
		int index = arg.indexOf(SVNFetchFactory.VALUE_PAIR_SEPARATOR);
		if (index != -1) {
			String key = arg.substring(0, index);
			if (SVNFetchFactory.KEY_URL.equals(key)) {
				isDefault = true;
			}
		}		
		return isDefault ? IMapFileParser.DEFAULT : IMapFileParser.SOURCE_FORGE_PARSER;
	}
		
	protected void printSVNTask(String command, String url, String pegRev, String rev, String dest, String username, String password, IAntScript script) {
		this.printSVNTask(command, url, pegRev, rev, dest, username, password, script, "false"); //$NON-NLS-1$
	}
	
	protected void printSVNTask(String command, String url, String pegRev, String rev, String dest, String username, String password, IAntScript script, String force) {		
		script.printTabs();
		script.print("<svn"); //$NON-NLS-1$
		script.printAttribute("command", command, false); //$NON-NLS-1$
		script.printAttribute("url", url, false); //$NON-NLS-1$
		script.printAttribute("pegRev", pegRev, false); //$NON-NLS-1$
		script.printAttribute("rev", rev, false); //$NON-NLS-1$
		script.printAttribute("dest", dest, false); //$NON-NLS-1$
		script.printAttribute("username", username, false); //$NON-NLS-1$
		script.printAttribute("password", password, false); //$NON-NLS-1$
		script.printAttribute("force", force, false); //$NON-NLS-1$
		script.println("/>"); //$NON-NLS-1$
	}
	
	protected void printAvailableTask(String property, String file, IAntScript script) {
		script.printTabs();
		script.print("<available"); //$NON-NLS-1$
		script.printAttribute("property", property, true); //$NON-NLS-1$
		script.printAttribute("file", file, false); //$NON-NLS-1$
		script.println("/>"); //$NON-NLS-1$
	}
	
}
