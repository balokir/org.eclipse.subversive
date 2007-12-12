/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Gabor Liptak - Speedup Pattern's usage
 *******************************************************************************/

package org.eclipse.team.svn.core.utility;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.internal.preferences.Base64;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.Team;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNEntryCallback;
import org.eclipse.team.svn.core.connector.ISVNEntryInfoCallback;
import org.eclipse.team.svn.core.connector.ISVNEntryStatusCallback;
import org.eclipse.team.svn.core.connector.ISVNLogEntryCallback;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.ISVNProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNPropertyCallback;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNEntry;
import org.eclipse.team.svn.core.connector.SVNEntryInfo;
import org.eclipse.team.svn.core.connector.SVNEntryReference;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNEntry.NodeKind;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.options.IIgnoreRecommendations;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.resource.ProxySettings;
import org.eclipse.team.svn.core.resource.SSHSettings;
import org.eclipse.team.svn.core.resource.SSLSettings;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * SVN Utility functions
 * 
 * @author Alexander Gurov
 */
public final class SVNUtility {
	private static String svnFolderName = null;
	
	public static Map parseSVNExternalsProperty(String property) {
		if (property == null) {
			return Collections.EMPTY_MAP;
		}
		Map retVal = new HashMap();
		String []externals = property.trim().split("[\\n]+"); // it seems different clients have different behaviours wrt trailing whitespace.. so trim() to be safe
		if (externals.length > 0) {
			for (int i = 0; i < externals.length; i++) {
				String []parts = externals[i].split("[\\t ]+");
				// 2 - name + URL
				// 3 - name + -rRevision + URL
				// 4 - name + -r Revision + URL
				if (parts.length < 2 || parts.length > 4) {
					throw new UnreportableException("Malformed external, " + parts.length + ", " + externals[i]);
				}
				String name = parts[0];  // hmm, we aren't handle the case were the name does not match the remote name, ie.   "foo  http://server/trunk/bar"..
				String url = (parts.length == 2 ? parts[1] : (parts.length == 4 ? parts[3] : parts[2])).trim(); // trim() to deal with Windows CR characters..
				
				try {
					url = SVNUtility.decodeURL(url);
				}
				catch (IllegalArgumentException ex) {
					// the URL is not encoded
				}
			    url = SVNUtility.normalizeURL(url);
				// see if we can find a matching repository location:
				int revision = SVNRevision.INVALID_REVISION_NUMBER;
				try {
					if (parts.length == 4) {
						revision = Integer.parseInt(parts[2]);
					}
					else if (parts.length == 3) {
						revision = Integer.parseInt(parts[1].substring(2));
					}
				}
				catch (Exception ex) {
					throw new UnreportableException("Malformed external, " + parts.length + ", " + externals[i]);
				}
				retVal.put(name, new SVNEntryRevisionReference(url, null, revision != SVNRevision.INVALID_REVISION_NUMBER ? SVNRevision.fromNumber(revision) : null));
			}
		}
		return retVal;
	}
	
	public static boolean useSingleReferenceSignature(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2) {
		int kind1 = reference1.revision.getKind();
		int kind2 = reference2.revision.getKind();
		if ((kind1 == SVNRevision.Kind.BASE || kind1 == SVNRevision.Kind.WORKING) && (kind2 == SVNRevision.Kind.BASE || kind2 == SVNRevision.Kind.WORKING)) {
			return false;
		}
		return reference1.path.equals(reference2.path) && (reference1.pegRevision == reference2.pegRevision || (reference1.pegRevision != null && reference1.pegRevision.equals(reference2.pegRevision)));
	}

	public static SVNEntryRevisionReference getEntryRevisionReference(IRepositoryResource resource) {
		return new SVNEntryRevisionReference(SVNUtility.encodeURL(resource.getUrl()), resource.getPegRevision(), resource.getSelectedRevision());
	}
	
	public static SVNEntryReference getEntryReference(IRepositoryResource resource) {
		return new SVNEntryReference(SVNUtility.encodeURL(resource.getUrl()), resource.getPegRevision());
	}
	
	public static SVNProperty []properties(ISVNConnector proxy, SVNEntryRevisionReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException {
		final SVNProperty[][] retVal = new SVNProperty[1][];
		proxy.properties(reference, Depth.EMPTY, new ISVNPropertyCallback() {
			public void next(String path, SVNProperty[] data) {
				retVal[0] = data;
			}
		}, monitor);
		return retVal[0];
	}
	
	public static SVNEntryStatus []status(ISVNConnector proxy, String path, int depth, boolean onServer, boolean getAll, boolean noIgnore, boolean ignoreExternals, ISVNProgressMonitor monitor) throws SVNConnectorException {
		final ArrayList statuses = new ArrayList();
		proxy.status(path, depth, onServer, getAll, noIgnore, ignoreExternals, new ISVNEntryStatusCallback() {
			public void next(SVNEntryStatus status) {
				statuses.add(status);
			}
		}, monitor);
		return (SVNEntryStatus [])statuses.toArray(new SVNEntryStatus[statuses.size()]);
	}
	
	public static SVNEntryStatus []diffStatus(ISVNConnector proxy, SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, int depth, boolean ignoreAncestry, ISVNProgressMonitor monitor) throws SVNConnectorException {
		final ArrayList statuses = new ArrayList();
		proxy.diffStatus(reference1, reference2, depth, ignoreAncestry, new ISVNEntryStatusCallback() {
			public void next(SVNEntryStatus status) {
				statuses.add(status);
			}
		}, monitor);
		return (SVNEntryStatus [])statuses.toArray(new SVNEntryStatus[statuses.size()]);
	}
	
	public static SVNEntryStatus []diffStatus(ISVNConnector proxy, SVNEntryReference reference, SVNRevision revision1, SVNRevision revision2, int depth, boolean ignoreAncestry, ISVNProgressMonitor monitor) throws SVNConnectorException {
		final ArrayList statuses = new ArrayList();
		proxy.diffStatus(reference, revision1, revision2, depth, ignoreAncestry, new ISVNEntryStatusCallback() {
			public void next(SVNEntryStatus status) {
				statuses.add(status);
			}
		}, monitor);
		return (SVNEntryStatus [])statuses.toArray(new SVNEntryStatus[statuses.size()]);
	}
	
	public static SVNEntryStatus[] mergeStatus(ISVNConnector proxy, SVNEntryReference reference, SVNRevisionRange []revisions, String path, int depth, boolean ignoreAncestry, ISVNProgressMonitor monitor) throws SVNConnectorException {
		final ArrayList statuses = new ArrayList();
		proxy.mergeStatus(reference, revisions, path, depth, ignoreAncestry, new ISVNEntryStatusCallback() {
			public void next(SVNEntryStatus status) {
				statuses.add(status);
			}
		}, monitor);
		return (SVNEntryStatus [])statuses.toArray(new SVNEntryStatus[statuses.size()]);
	}
	
	public static SVNEntry []list(ISVNConnector proxy, SVNEntryRevisionReference reference, int depth, int direntFields, boolean fetchLocks, ISVNProgressMonitor monitor) throws SVNConnectorException {
		final ArrayList entries = new ArrayList();
		proxy.list(reference, depth, direntFields, fetchLocks, new ISVNEntryCallback() {
			public void next(SVNEntry entry) {
				entries.add(entry);
			}
		}, monitor);
		return (SVNEntry [])entries.toArray(new SVNEntry[entries.size()]);
	}
	
	public static SVNLogEntry []logEntries(ISVNConnector proxy, SVNEntryReference reference, SVNRevision revisionStart, SVNRevision revisionEnd, boolean stopOnCopy, boolean discoverPath, String[] revProps, long limit, ISVNProgressMonitor monitor) throws SVNConnectorException {
		final ArrayList entries = new ArrayList();
		proxy.logEntries(reference, revisionStart, revisionEnd, stopOnCopy, discoverPath, false, revProps, limit, new ISVNLogEntryCallback() {
			public void next(SVNLogEntry log, boolean hasChildren) {
				entries.add(log);
			}
		}, monitor);
		return (SVNLogEntry [])entries.toArray(new SVNLogEntry[entries.size()]);
	}
	
	public static SVNEntryInfo []info(ISVNConnector proxy, SVNEntryRevisionReference reference, int depth, ISVNProgressMonitor monitor) throws SVNConnectorException {
		final ArrayList infos = new ArrayList();
		proxy.info(reference, depth, new ISVNEntryInfoCallback() {
			public void next(SVNEntryInfo info) {
				infos.add(info);
			}
		}, monitor);
		return (SVNEntryInfo [])infos.toArray(new SVNEntryInfo[infos.size()]);
	}
	
	public static String getStatusText(String status) {
		if (status == null) {
			status = "NotExists";
		}
		return SVNTeamPlugin.instance().getResource("Status." + status);
	}
	
	public static String getOldRoot(String oldUrl, IRepositoryResource []rootChildren) {
		for (int i = 0; i < rootChildren.length; i++) {
			String childName = rootChildren[i].getName();
			int idx = oldUrl.indexOf(childName);
			if (idx > 0 && oldUrl.charAt(idx - 1) == '/' && (oldUrl.endsWith(childName) || oldUrl.charAt(idx + childName.length()) == '/')) {
				return oldUrl.substring(0, idx - 1);
			}
		}
		return null;
	}
	
	public static IRepositoryRoot getTrunkLocation(IRepositoryResource resource) {
		return SVNUtility.getRootLocation(resource, resource.getRepositoryLocation().getTrunkLocation());
	}
	
	public static IRepositoryRoot getBranchesLocation(IRepositoryResource resource) {
		return SVNUtility.getRootLocation(resource, resource.getRepositoryLocation().getBranchesLocation());
	}
	
	public static IRepositoryRoot getTagsLocation(IRepositoryResource resource) {
		return SVNUtility.getRootLocation(resource, resource.getRepositoryLocation().getTagsLocation());
	}
	
	public static IRepositoryContainer getProposedTrunk(IRepositoryLocation location) {
		return location.asRepositoryContainer(SVNUtility.getProposedTrunkLocation(location), false);
	}
	
	public static IRepositoryContainer getProposedBranches(IRepositoryLocation location) {
		return location.asRepositoryContainer(SVNUtility.getProposedBranchesLocation(location), false);
	}
	
	public static IRepositoryContainer getProposedTags(IRepositoryLocation location) {
		return location.asRepositoryContainer(SVNUtility.getProposedTagsLocation(location), false);
	}
	
	public static String getProposedTrunkLocation(IRepositoryLocation location) {
		String baseUrl = location.getUrl();
		return location.isStructureEnabled() ? (baseUrl + "/" + location.getTrunkLocation()) : baseUrl;
	}
	
	public static String getProposedBranchesLocation(IRepositoryLocation location) {
		String baseUrl = location.getUrl();
		return location.isStructureEnabled() ? (baseUrl + "/" + location.getBranchesLocation()) : baseUrl;
	}
	
	public static String getProposedTagsLocation(IRepositoryLocation location) {
		String baseUrl = location.getUrl();
		return location.isStructureEnabled() ? (baseUrl + "/" + location.getTagsLocation()) : baseUrl;
	}
	
	public static IRepositoryRoot []findRoots(String resourceUrl, boolean longestOnly) {
		IPath url = new Path(resourceUrl);
		IRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryLocation []locations = storage.getRepositoryLocations();
		ArrayList roots = new ArrayList();
		for (int i = 0; i < locations.length; i++) {
			if (new Path(locations[i].getUrl()).isPrefixOf(url) || // performance optimization: repository root URL detection [if is not cached] requires interaction with a remote host
				new Path(locations[i].getRepositoryRootUrl()).isPrefixOf(url)) {
				SVNUtility.addRepositoryRoot(roots, (IRepositoryRoot)locations[i].asRepositoryContainer(resourceUrl, false).getRoot(), longestOnly);
			}
		}
		IRepositoryRoot []repositoryRoots = (IRepositoryRoot [])roots.toArray(new IRepositoryRoot[roots.size()]);
		if (!longestOnly) {
			FileUtility.sort(repositoryRoots, new Comparator() {
				public int compare(Object o1, Object o2) {
					IRepositoryRoot first = (IRepositoryRoot)o1;
					IRepositoryRoot second = (IRepositoryRoot)o2;
					return second.getUrl().compareTo(first.getUrl());
				}
			});
		}
		return repositoryRoots;
	}
	
	private static void addRepositoryRoot(List container, IRepositoryRoot root, boolean longestOnly) {
		if (longestOnly && container.size() > 0) {
			int cnt = new Path(root.getUrl()).segmentCount();
			int cnt2 = new Path(((IRepositoryRoot)container.get(0)).getUrl()).segmentCount();
			if (cnt > cnt2) {
				container.clear();
				container.add(root);
			}
			else if (cnt == cnt2) {
				container.add(root);
			}
		}
		else {
			container.add(root);
		}
	}
	
	public static synchronized String getSVNFolderName() {
		if (SVNUtility.svnFolderName == null) {
			String name = FileUtility.getEnvironmentVariables().get("SVN_ASP_DOT_NET_HACK") != null ? "_svn" : ".svn";
			SVNUtility.svnFolderName = System.getProperty("javasvn.admindir", name);
		}
		return SVNUtility.svnFolderName;
	}
	
	public static String getResourceParent(IRepositoryResource resource) {
		String parent = "";
		String url = resource.getUrl();
		String rootUrl = resource.getRoot().getUrl();
		if (url.equals(rootUrl)) {
			return "";
		}
		parent = url.substring(rootUrl.length(), url.length() - resource.getName().length() - 1);
		return parent;
	}
	
	public static IRepositoryResource copyOf(IRepositoryResource resource) {
		String url = resource.getUrl();
		return resource instanceof IRepositoryFile ? (IRepositoryResource)resource.asRepositoryFile(url, false) : resource.asRepositoryContainer(url, false);
	}
	
	public static IRepositoryResource []makeResourceSet(IRepositoryResource upPoint, String relativeReference, boolean isFile) {
		String url = SVNUtility.normalizeURL(upPoint.getUrl() + "/" + relativeReference);
		IRepositoryLocation location = upPoint.getRepositoryLocation();
		IRepositoryResource downPoint = isFile ? (IRepositoryResource)location.asRepositoryFile(url, false) : location.asRepositoryContainer(url, false);
		downPoint.setPegRevision(upPoint.getPegRevision());
		downPoint.setSelectedRevision(upPoint.getSelectedRevision());
		return SVNUtility.makeResourceSet(upPoint, downPoint);
	}
	
	public static IRepositoryResource []makeResourceSet(IRepositoryResource upPoint, IRepositoryResource downPoint) {
		ArrayList resourceSet = new ArrayList();
		while (downPoint != null && !downPoint.equals(upPoint)) {
			resourceSet.add(0, downPoint);
			downPoint = downPoint.getParent();
		}
		return (IRepositoryResource [])resourceSet.toArray(new IRepositoryResource[resourceSet.size()]);
	}
	
	public static URL getSVNUrl(String url) throws MalformedURLException {
		return SVNUtility.getSVNUrlStreamHandler(url).getURL();
	}
	
	public static SVNURLStreamHandler getSVNUrlStreamHandler(String url) throws MalformedURLException {
		SVNURLStreamHandler retVal = new SVNURLStreamHandler();
		new URL(null, url, retVal);
		return retVal;
	}
	
	public static String base64Encode(String data) {
		if (data == null) {
			return null;
		}
		return new String(Base64.encode(data.getBytes()));
	}
	
	public static String base64Decode(String encoded) {
		if (encoded == null) {
			return null;
		}
		return new String(Base64.decode(encoded.getBytes()));
	}

	public synchronized static void addSVNNotifyListener(ISVNConnector proxy, ISVNNotificationCallback listener) {
		Notify2Composite composite = (Notify2Composite)proxy.getNotificationCallback();
		if (composite == null) {
			proxy.setNotificationCallback(composite = new Notify2Composite());
		}
		composite.add(listener);
	}

	public synchronized static void removeSVNNotifyListener(ISVNConnector proxy, ISVNNotificationCallback listener) {
		Notify2Composite composite = (Notify2Composite)proxy.getNotificationCallback();
		if (composite != null) {
			composite.remove(listener);
		}
	}
	
	public static void reorder(SVNEntryStatus []statuses, final boolean parent2Child) {
		FileUtility.sort(statuses, new Comparator() {
			public int compare(Object o1, Object o2) {
				String s1 = ((SVNEntryStatus)o1).path;
				String s2 = ((SVNEntryStatus)o2).path;
				return parent2Child ? s1.compareTo(s2) : s2.compareTo(s1);
			}
			
			public boolean equals(Object obj) {
				return false;
			}
		});
	}
	
	public static String encodeURL(String url) {
		try {
			url = SVNUtility.normalizeURL(url);
			int idx = url.startsWith("file:///") ? "file:///".length() : (url.startsWith("file://") ? (url.indexOf("/", "file://".length()) + 1) : (url.indexOf("://") + 3));
			idx = url.indexOf("/", idx);
			if (idx == -1) {
				return url;
			}
			String retVal = url.substring(0, idx);
			StringTokenizer tok = new StringTokenizer(url.substring(idx), "/ ", true);
			// user name should be never encoded
			idx = retVal.indexOf('@');
			if (idx != -1) {
				String protocol = retVal.substring(0, retVal.indexOf("://") + 3);
				String serverPart = retVal.substring(idx);
				retVal = protocol + retVal.substring(protocol.length(), idx) + serverPart;
			}
			while (tok.hasMoreTokens()) {
				String token = tok.nextToken();
				if (token.equals("/")) {
					retVal += token;
				}
				else if (token.equals(" ")) {
					retVal += "%20";
				}
				else {
					retVal += URLEncoder.encode(token, "UTF-8");
				}
			}
			return retVal;
		}
		catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static String decodeURL(String url) {
		try {
			url = SVNUtility.normalizeURL(url);
			int idx = url.startsWith("file:///") ? "file:///".length() : (url.startsWith("file://") ? (url.indexOf("/", "file://".length()) + 1) : (url.indexOf("://") + 3));
			idx = url.indexOf("/", idx);
			if (idx == -1) {
				return url;
			}
			String retVal = url.substring(0, idx);
			StringTokenizer tok = new StringTokenizer(url.substring(idx), "/+", true);
			// user name should be never decoded
			idx = retVal.indexOf('@');
			if (idx != -1) {
				String protocol = retVal.substring(0, retVal.indexOf("://") + 3);
				String serverPart = retVal.substring(idx);
				retVal = protocol + retVal.substring(protocol.length(), idx) + serverPart;
			}
			while (tok.hasMoreTokens()) {
				String token = tok.nextToken();
				if (token.equals("/") || token.equals("+")) {
					retVal += token;
				}
				else {
					retVal += URLDecoder.decode(token, "UTF-8");
				}
			}
			return retVal;
		}
		catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static String normalizeURL(String url) {
	    StringTokenizer tokenizer = new StringTokenizer(PatternProvider.replaceAll(url, "([\\\\])+", "/"), "/", false);
	    String retVal = "";
	    while (tokenizer.hasMoreTokens()) {
	        String token = tokenizer.nextToken();
	        retVal += retVal.length() == 0 ? token : ("/" + token);
	    }
	    int idx = retVal.indexOf(':') + 1;
	    return idx == 0 ? retVal : retVal.substring(0, idx) + (url.startsWith("file:///") ? "//" : "/") + retVal.substring(idx);
	}
	
	public static Exception validateRepositoryLocation(IRepositoryLocation location) {
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			proxy.list(new SVNEntryRevisionReference(SVNUtility.encodeURL(location.getUrl()), null, null), Depth.EMPTY, SVNEntry.Fields.ALL, false, new ISVNEntryCallback() {
				public void next(SVNEntry entry) {
				}
			}, new SVNNullProgressMonitor());
		} 
		catch (Exception e) {
			return e;
		}
		finally {
			location.releaseSVNProxy(proxy);
			location.dispose();
		}
		return null;
	}
	
	public static void configureProxy(ISVNConnector proxy, IRepositoryLocation location) {
		proxy.setUsername(location.getUsername());
		proxy.setPassword(location.getPassword());
		
		ProxySettings proxySettings = location.getProxySettings();
	    if (proxySettings.isEnabled()) {
	    	proxy.setProxy(proxySettings.getHost(), proxySettings.getPort(), proxySettings.isAuthenticationEnabled() ? proxySettings.getUsername() : "", proxySettings.isAuthenticationEnabled() ? proxySettings.getPassword() : "");
	    }
	    else {
	    	proxy.setProxy(null, -1, null, null);
	    }
	    
	    SSLSettings sslSettings = location.getSSLSettings();
	    if (sslSettings.isAuthenticationEnabled()) {
		    proxy.setClientSSLCertificate(sslSettings.getCertificatePath(), sslSettings.getPassPhrase());
	    }
	    else {
	    	proxy.setClientSSLCertificate(null, null);
	    }
	    
	    SSHSettings sshSettings = location.getSSHSettings();
	    if (!sshSettings.isUseKeyFile()) {
	    	proxy.setSSHCredentials(location.getUsername(), location.getPassword(), sshSettings.getPort());
	    }
	    else if (sshSettings.getPrivateKeyPath().length() > 0) {
	    	proxy.setSSHCredentials(location.getUsername(), sshSettings.getPrivateKeyPath(), sshSettings.getPassPhrase(), sshSettings.getPort());
	    }
	    else {
	    	proxy.setSSHCredentials(null, null, null, -1);
	    }
	}
	
	public static SVNEntryStatus getSVNInfoForNotConnected(IResource root) {
		IPath location = FileUtility.getResourcePath(root);
		IPath checkedPath = root.getType() == IResource.FILE ? location.removeLastSegments(1) : location;
		if (!checkedPath.append(SVNUtility.getSVNFolderName()).toFile().exists()) {
			return null;
		}
		ISVNConnector proxy = CoreExtensionsManager.instance().getSVNConnectorFactory().newInstance();
		try {
			SVNEntryStatus []st = SVNUtility.status(proxy, location.toString(), Depth.IMMEDIATES, false, true, false, false, new SVNNullProgressMonitor());
			if (st != null && st.length > 0) {
				SVNUtility.reorder(st, true);
				return st[0].url == null ? null : st[0];
			}
			else {
				return null;
			}
		}
		catch (Exception ex) {
			return null;
		}
		finally {
			proxy.dispose();
		}
	}
	
	public static String getPropertyForNotConnected(IResource root, String propertyName) {
		String location = FileUtility.getWorkingCopyPath(root);
		ISVNConnector proxy = CoreExtensionsManager.instance().getSVNConnectorFactory().newInstance();
		try {
			SVNProperty data = proxy.propertyGet(new SVNEntryRevisionReference(location, null, SVNRevision.WORKING), propertyName, new SVNNullProgressMonitor());
			return data == null ? null : (data.value != null ? data.value : new String(data.data));
		}
		catch (Exception ex) {
			return null;
		}
		finally {
			proxy.dispose();
		}
	}
	
    public static boolean isIgnored(IResource resource) {
		// Ignore WorkspaceRoot, derived and team-private resources and resources from TeamHints 
        if (resource instanceof IWorkspaceRoot || resource.isDerived() || 
        	FileUtility.isSVNInternals(resource) || Team.isIgnoredHint(resource) || SVNUtility.isMergeParts(resource)) {
        	return true;
        }
        try {
        	IIgnoreRecommendations []ignores = CoreExtensionsManager.instance().getIgnoreRecommendations();
        	for (int i = 0; i < ignores.length; i++) {
        		if (ignores[i].isAcceptableNature(resource) && ignores[i].isIgnoreRecommended(resource)) {
        			return true;
        		}
        	}
        }
        catch (Exception ex) {
        	// cannot be correctly processed in the caller context
        }
        return false;
    }

	public static Map splitWorkingCopies(IResource []resources) {
		Map wc2Resources = new HashMap();
		
		for (int i = 0; i < resources.length; i++) {
			IProject wcRoot = resources[i].getProject();

			List wcResources = (List)wc2Resources.get(wcRoot);
			if (wcResources == null) {
				wc2Resources.put(wcRoot, wcResources = new ArrayList());
			}
			wcResources.add(resources[i]);
		}

		return wc2Resources;
	}
	
	public static Map splitWorkingCopies(File []files) {
		Map wc2Resources = new HashMap();
		
		ISVNConnector proxy = CoreExtensionsManager.instance().getSVNConnectorFactory().newInstance();
		try {
			Map file2info = new HashMap();
			for (int i = 0; i < files.length; i++) {
				file2info.put(files[i], SVNUtility.getSVNInfo(files[i], proxy));
			}
			
			ArrayList restOfFiles = new ArrayList(Arrays.asList(files));
			while (restOfFiles.size() > 0) {
				File current = (File)restOfFiles.get(0);
				SVNEntryInfo info = (SVNEntryInfo)file2info.get(current);
				Object []wcRoot = SVNUtility.getWCRoot(proxy, current, info);
				
				List wcResources = (List)wc2Resources.get(wcRoot[0]);
				if (wcResources == null) {
					wc2Resources.put(wcRoot[0], wcResources = new ArrayList());
				}
				
				Path rootPath = new Path(((File)wcRoot[0]).getAbsolutePath());
				Path rootInfoPath = new Path(((SVNEntryInfo)wcRoot[1]).url);
				for (Iterator it = restOfFiles.iterator(); it.hasNext(); ) {
					File checked = (File)it.next();
					if (rootPath.isPrefixOf(new Path(checked.getAbsolutePath()))) {
						SVNEntryInfo checkedInfo = (SVNEntryInfo)file2info.get(checked);
						if (rootInfoPath.isPrefixOf(new Path(checkedInfo.url))) {
							wcResources.add(checked);
							it.remove();
						}
					}
				}
			}
		}
		finally {
			proxy.dispose();
		}

		return wc2Resources;
	}
	
	private static Object []getWCRoot(ISVNConnector proxy, File node, SVNEntryInfo info) {
		File oldRoot = node;
		SVNEntryInfo oldInfo = info;
		
		node = node.getParentFile();
		while (node != null) {
			SVNEntryInfo rootInfo = SVNUtility.getSVNInfo(node, proxy);
			if (rootInfo != null) {
				if (oldInfo == null) {
					oldInfo = rootInfo;
				}
				else if (!new Path(rootInfo.url).isPrefixOf(new Path(oldInfo.url))) {
					return new Object[] {oldRoot, oldInfo};
				}
				oldRoot = node;
				node = node.getParentFile();
			}
			else if (oldInfo != null) {
				return new Object[] {oldRoot, oldInfo};
			}
		}
		
		if (oldInfo == null) {
			String errMessage = SVNTeamPlugin.instance().getResource("Error.NonSVNPath");
			throw new RuntimeException(MessageFormat.format(errMessage, new String[] {oldRoot.getAbsolutePath()}));
		}
		return new Object[] {oldRoot, oldInfo};
	}
	
	public static SVNEntryInfo getSVNInfo(File root) {
		ISVNConnector proxy = CoreExtensionsManager.instance().getSVNConnectorFactory().newInstance();
		try {
			return SVNUtility.getSVNInfo(root, proxy);
		}
		finally {
			proxy.dispose();
		}
	}
	
	public static SVNEntryInfo getSVNInfo(File root, ISVNConnector proxy) {
		if (root.exists()) {
			File svnMeta = root.isDirectory() ? root : root.getParentFile();
			svnMeta = new File(svnMeta.getAbsolutePath() + "/" + SVNUtility.getSVNFolderName());
			if (svnMeta.exists()) {
				try {
					SVNEntryInfo []st = SVNUtility.info(proxy, new SVNEntryRevisionReference(root.getAbsolutePath(), null, SVNRevision.BASE), Depth.EMPTY, new SVNNullProgressMonitor());
					return st != null && st.length != 0 ? st[0] : null;
				}
				catch (Exception ex) {
					return null;
				}
			}
		}
		return null;
	}
	
	public static String []asURLArray(IRepositoryResource []resources, boolean encode) {
	    String []retVal = new String[resources.length];
		for (int i = 0; i < resources.length; i++) {
			retVal[i] = encode ? SVNUtility.encodeURL(resources[i].getUrl()) : resources[i].getUrl();
		}
		return retVal;
	}
	
	public static Map splitRepositoryLocations(IRepositoryResource []resources) throws Exception {
		Map repository2Resources = new HashMap();
		for (int i = 0; i < resources.length; i++) {
			IRepositoryLocation location = resources[i].getRepositoryLocation();

			List tResources = (List)repository2Resources.get(location);
			if (tResources == null) {
				repository2Resources.put(location, tResources = new ArrayList());
			}
			tResources.add(resources[i]);
		}
		return SVNUtility.combineLocationsByUUID(repository2Resources);
	}
	
	public static Map splitRepositoryLocations(IResource []resources) throws Exception{
	   	Map repository2Resources = new HashMap();	   	
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		for (int i = 0; i < resources.length; i++) {
		    IRepositoryLocation location = storage.getRepositoryLocation(resources[i]);

			List tResources = (List)repository2Resources.get(location);
			if (tResources == null) {
				repository2Resources.put(location, tResources = new ArrayList());
			}
			tResources.add(resources[i]);
		}
		return SVNUtility.combineLocationsByUUID(repository2Resources);
	}
	
	public static Map splitRepositoryLocations(File []files) throws Exception{
	   	Map repository2Resources = new HashMap();	   	
		for (int i = 0; i < files.length; i++) {
		    IRepositoryResource resource = SVNFileStorage.instance().asRepositoryResource(files[i], false);
		    IRepositoryLocation location = resource.getRepositoryLocation();

			List tResources = (List)repository2Resources.get(location);
			if (tResources == null) {
				repository2Resources.put(location, tResources = new ArrayList());
			}
			tResources.add(files[i]);
		}
		return SVNUtility.combineLocationsByUUID(repository2Resources);
	}
	
	public static int getNodeKind(String path, int kind, boolean ignoreNone) {
		File f = new File(path);
		if (f.exists()) {
			return f.isDirectory() ? NodeKind.DIR : NodeKind.FILE;
		}
		else if (kind == NodeKind.DIR) {
			return NodeKind.DIR;
		}
		else if (kind == NodeKind.FILE) {
			return NodeKind.FILE;
		}
		// ignore files absent in the WC base and WC working. But what is the reason why it is reported ?
		if (ignoreNone) {
			return NodeKind.NONE;
		}
		String errMessage = SVNTeamPlugin.instance().getResource("Error.UnrecognizedNodeKind");
		throw new RuntimeException(MessageFormat.format(errMessage, new String[] {String.valueOf(kind), path}));
	}
	
	public static IRepositoryResource []shrinkChildNodes(IRepositoryResource []resources) {
		Set roots = new HashSet(Arrays.asList(resources));
		for (int i = 0; i < resources.length; i++) {
			if (SVNUtility.hasRoots(roots, resources[i])) {
				roots.remove(resources[i]);
			}
		}
		return (IRepositoryResource [])roots.toArray(new IRepositoryResource[roots.size()]);
	}
	
	public static IRepositoryResource []getCommonParents(IRepositoryResource []resources) {
		Map byRepositoryRoots = new HashMap();
		for (int i = 0; i < resources.length; i++) {
			IRepositoryResource root = resources[i].getRoot();
			List tmp = (List)byRepositoryRoots.get(root);
			if (tmp == null) {
				byRepositoryRoots.put(root, tmp = new ArrayList());
			}
			tmp.add(resources[i]);
		}
		IRepositoryResource []retVal = new IRepositoryResource[byRepositoryRoots.size()];
		int i = 0;
		for (Iterator it = byRepositoryRoots.values().iterator(); it.hasNext(); i++) {
			List tmp = (List)it.next();
			retVal[i] = SVNUtility.getCommonParent((IRepositoryResource [])tmp.toArray(new IRepositoryResource[tmp.size()]));
		}
		return retVal;
	}
	
	public static SVNEntryInfo getLocationInfo(IRepositoryLocation location) throws Exception {
		ISVNConnector proxy = location.acquireSVNProxy();
		SVNEntryInfo []infos = null;
		try {
		    infos = SVNUtility.info(proxy, SVNUtility.getEntryRevisionReference(location.getRoot()), Depth.EMPTY, new SVNNullProgressMonitor());
		}
		finally {
		    location.releaseSVNProxy(proxy);
		}
		return infos != null && infos.length > 0 ? infos[0] : null;
	}
	
	public static String getAscendant(IRepositoryResource resource) {
		String pathUpToRoot = SVNUtility.getPathUpToRoot(resource);
		int idx = pathUpToRoot.indexOf('/');
		return idx == -1 ? pathUpToRoot : pathUpToRoot.substring(0, idx);
	}
	
	public static String getDescendant(IRepositoryResource resource) {
		String pathUpToRoot = SVNUtility.getPathUpToRoot(resource);
		int idx = pathUpToRoot.lastIndexOf('/');
		return idx == -1 ? pathUpToRoot : pathUpToRoot.substring(idx + 1);
	}
	
	public static String getPathUpToRoot(IRepositoryResource resource) {
		IRepositoryResource root = resource.getRoot();
		return root == resource ? resource.getName() : resource.getUrl().substring(root.getUrl().length() + 1);
	}

	private static boolean isMergeParts(IResource resource) {
		String ext = resource.getFileExtension();
		return ext != null && ext.matches("r(\\d)+");
	}
	    
	private static Map combineLocationsByUUID(Map repository2Resources) throws Exception{
	    Map locationUtility2Resources = new HashMap();
	    for (Iterator it = repository2Resources.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			IRepositoryLocation location = (IRepositoryLocation)entry.getKey();
			List tResources = (List)entry.getValue();
	        RepositoryLocationUtility locationUtility = new RepositoryLocationUtility(location);
		    List tResources2 = (List)locationUtility2Resources.get(locationUtility);
			if (tResources2 == null) {
			    locationUtility2Resources.put(locationUtility, tResources2 = new ArrayList());
			}
			tResources2.addAll(tResources);
		}
	    repository2Resources.clear();		
		for (Iterator it = locationUtility2Resources.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			RepositoryLocationUtility locationUtility = (RepositoryLocationUtility)entry.getKey();
			repository2Resources.put(locationUtility.location, (List)entry.getValue());
		}	
	    return repository2Resources;
	}
	
	private static boolean hasRoots(Set roots, IRepositoryResource node) {
		while ((node = node.getParent()) != null) {
			if (roots.contains(node)) {
				return true;
			}
		}
		return false;
	}
	
	private static IRepositoryResource getCommonParent(IRepositoryResource []resources) {
		if (resources == null || resources.length == 0) {
			return null;
		}
		IRepositoryResource base = resources[0].getParent();
		while (base != null) {	// can be null for resources from different repositories
			int startsCnt = 0;
			String baseUrl = base.getUrl();
			for (int i = 0; i < resources.length; i++) {
				if (resources[i].getUrl().startsWith(baseUrl)) {
					startsCnt++;
				}
			}
			if (startsCnt == resources.length) {
				break;
			}
			else {
				base = base.getParent();
			}
		}
		return base;
	}
	
	private static IRepositoryRoot getRootLocation(IRepositoryResource resource, String rootName) {
		IRepositoryLocation location = resource.getRepositoryLocation();
		IRepositoryRoot root = (IRepositoryRoot)resource.getRoot();
		if (!location.isStructureEnabled()) {
			return root;
		}
		
		int rootKind = root.getKind();
		if (rootKind == IRepositoryRoot.KIND_ROOT) {
			return (IRepositoryRoot)root.asRepositoryContainer(rootName, false);
		}
		else if (rootKind == IRepositoryRoot.KIND_LOCATION_ROOT) {
			IRepositoryResource parent = root.getParent();
			if (parent == null) {
				return (IRepositoryRoot)root.asRepositoryContainer(rootName, false);
			}
			IRepositoryRoot tmp = (IRepositoryRoot)parent.getRoot();
			rootKind = tmp.getKind();
			if (rootKind == IRepositoryRoot.KIND_ROOT) {
				return (IRepositoryRoot)root.asRepositoryContainer(rootName, false);
			}
			root = tmp;
		}
		IRepositoryResource rootParent = root.getParent();
		return (IRepositoryRoot)rootParent.asRepositoryContainer(rootName, false);
	}
	
	private SVNUtility() {
		
	}

}
