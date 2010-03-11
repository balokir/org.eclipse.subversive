/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import org.eclipse.core.internal.preferences.Base64;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.Team;
import org.eclipse.team.svn.core.PathForURL;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.SVNTeamProvider;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNDiffStatusCallback;
import org.eclipse.team.svn.core.connector.ISVNEntryCallback;
import org.eclipse.team.svn.core.connector.ISVNEntryInfoCallback;
import org.eclipse.team.svn.core.connector.ISVNEntryStatusCallback;
import org.eclipse.team.svn.core.connector.ISVNLogEntryCallback;
import org.eclipse.team.svn.core.connector.ISVNMergeStatusCallback;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.ISVNProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNPropertyCallback;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNDiffStatus;
import org.eclipse.team.svn.core.connector.SVNEntry;
import org.eclipse.team.svn.core.connector.SVNEntryInfo;
import org.eclipse.team.svn.core.connector.SVNEntryReference;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNMergeStatus;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.ISVNConnector.Options;
import org.eclipse.team.svn.core.connector.SVNEntry.Kind;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.extension.options.IIgnoreRecommendations;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.resource.IRevisionLink;
import org.eclipse.team.svn.core.resource.SSHSettings;
import org.eclipse.team.svn.core.resource.SSLSettings;
import org.eclipse.team.svn.core.svnstorage.SVNCachedProxyCredentialsManager;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRevisionLink;

/**
 * SVN Utility functions
 * 
 * @author Alexander Gurov
 */
public final class SVNUtility {
	private static String svnFolderName = null;

	public static IRepositoryResource asRepositoryResource(String url, boolean isFolder) {
		if (!SVNUtility.isValidSVNURL(url)) {
			return null;
		}
		IRepositoryRoot []roots = SVNUtility.findRoots(url, true);
		IRepositoryResource retVal = null;
		if (roots.length > 0) {
			retVal = isFolder ? roots[0].asRepositoryContainer(url, false) : roots[0].asRepositoryFile(url, false); 
		}
		else {
			IRepositoryLocation location = SVNRemoteStorage.instance().newRepositoryLocation();
			SVNUtility.initializeRepositoryLocation(location, url);
			retVal = isFolder ? location.asRepositoryContainer(url, false) : location.asRepositoryFile(url, false);
		}
		return retVal;
	}
	
	public static void initializeRepositoryLocation(IRepositoryLocation location, String url) {
		location.setStructureEnabled(true);
		location.setTrunkLocation(CoreExtensionsManager.instance().getOptionProvider().getDefaultTrunkName());
		location.setBranchesLocation(CoreExtensionsManager.instance().getOptionProvider().getDefaultBranchesName());
		location.setTagsLocation(CoreExtensionsManager.instance().getOptionProvider().getDefaultTagsName());
		IPath urlPath = SVNUtility.createPathForSVNUrl(url);
		if (urlPath.lastSegment().equals(location.getTrunkLocation())) {
			url = urlPath.removeLastSegments(1).toString();
		}
		location.setUrl(url);
	}
	
	public static IRepositoryResource getCopiedFrom(IResource resource) {
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
		if (local.isCopied()) {
			IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(resource);
			ISVNConnector proxy = location.acquireSVNProxy();
			final SVNChangeStatus []st = new SVNChangeStatus[1];
			try {
				final String path = FileUtility.getWorkingCopyPath(resource);
				proxy.status(path, ISVNConnector.Depth.EMPTY, ISVNConnector.Options.INCLUDE_UNCHANGED | Options.IGNORE_EXTERNALS, null, new ISVNEntryStatusCallback() {
					public void next(SVNChangeStatus status) {
						if (path.equals(status.path)) {
							st[0] = status;
						}
					}
				}, new SVNNullProgressMonitor());
			}
			catch (SVNConnectorException ex) {
				return null;
			}
			finally {
				location.releaseSVNProxy(proxy);
			}
			
			if (st[0] != null) {
				String url = st[0].urlCopiedFrom;
				if (url == null) {
					IResource parent = resource.getParent();
					if (parent != null && parent.getType() != IResource.ROOT) {
						IRepositoryResource tmp = SVNUtility.getCopiedFrom(parent);
						if (tmp != null) {
							url = tmp.getUrl() + "/" + resource.getName(); //$NON-NLS-1$
						}
					}
				}
				else {
					url = SVNUtility.decodeURL(url);
				}
				IRepositoryResource retVal = SVNRemoteStorage.instance().asRepositoryResource(location, url, resource.getType() == IResource.FILE);
				retVal.setSelectedRevision(SVNRevision.fromNumber(st[0].revisionCopiedFrom == SVNRevision.INVALID_REVISION_NUMBER ? st[0].revision : st[0].revisionCopiedFrom));
				return retVal;
			}
		}
		return null;
	}
	
	public static Map<String, SVNEntryRevisionReference> parseSVNExternalsProperty(String property, IRepositoryResource propertyHolder) {
		Map<String, SVNEntryRevisionReference> retVal = new HashMap<String, SVNEntryRevisionReference>();
		
		SVNExternalPropertyData[] externalsData = SVNExternalPropertyData.parse(property);
		for (SVNExternalPropertyData externalData : externalsData) {
			String url = externalData.url;
			SVNRevision revision = null;
			SVNRevision pegRevision = null;
			
			try {
				if (externalData.revision != null) {
					revision = SVNRevision.fromString(externalData.revision);
				}			
				if (externalData.pegRevision != null) {
					revision = SVNRevision.fromString(externalData.pegRevision);
				}				
			} catch (Exception ex) {
				throw new UnreportableException("Malformed external, " + externalData.toString()); //$NON-NLS-1$
			}
			
			url = SVNUtility.replaceRelativeExternalParts(url, propertyHolder);				
			
			try {
				url = SVNUtility.decodeURL(url);
			}
			catch (IllegalArgumentException ex) {
				// the URL is not encoded
			}
		    url = SVNUtility.normalizeURL(url);
		    
		    retVal.put(externalData.localPath, new SVNEntryRevisionReference(url, pegRevision, revision));
		}
		
		return retVal;
	}
	
	public static String replaceRelativeExternalParts(String url, IRepositoryResource resource) throws UnreportableException {
		if (SVNUtility.isValidSVNURL(url)) {
			return url;
		}
		
		if (url.startsWith("^/")) { //$NON-NLS-1$
			url = resource.getRepositoryLocation().getRepositoryRoot().getUrl() + url.substring(1);
		}
		else if (url.startsWith("//")) { //$NON-NLS-1$
			try {
				String protocol = SVNUtility.getSVNUrl(resource.getUrl()).getProtocol();
				if (resource.getUrl().indexOf(":///") != -1) { //$NON-NLS-1$
					url = protocol + ":/" + url; //$NON-NLS-1$
				}
				else {
					url = protocol + ":" + url; //$NON-NLS-1$
				}
			}
			catch (MalformedURLException e) {
				// cannot be thrown
			}
		}
		else if (url.startsWith("/")) { //$NON-NLS-1$
			String prefix = resource.getUrl();
			int idx = prefix.lastIndexOf("//"); //$NON-NLS-1$
			idx = prefix.indexOf('/', idx + 2);
			url = prefix.substring(0, idx) + url;
		}
		else if (url.startsWith("../")) { //$NON-NLS-1$
			IRepositoryResource prefix = resource;
			while (url.startsWith("../")) { //$NON-NLS-1$
				url = url.substring(3);
				prefix = prefix.getParent();
				if (prefix == null) {
					throw new UnreportableException("Malformed url: " + url); //$NON-NLS-1$
				}
			}
			url = prefix.getUrl() + "/" + url; //$NON-NLS-1$
		} else {
			throw new UnreportableException("Malformed url: " + url); //$NON-NLS-1$
		}
		return url;
	}
	
	public static SVNEntryReference asEntryReference(String url) {
		if (url == null) {
			return null;
		}
		int idx = url.lastIndexOf('@');
		SVNRevision peg = null;
		if (idx != -1) {
			try {
				peg = SVNRevision.fromString(url.substring(idx + 1));
				url = url.substring(0, idx);
			}
			catch (IllegalArgumentException ex) {
				// it is not a revision at the end of the URL
			}
		}
		return new SVNEntryReference(url, peg);
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
		proxy.getProperties(reference, Depth.EMPTY, null, new ISVNPropertyCallback() {
			public void next(String path, SVNProperty[] data) {
				retVal[0] = data;
			}
		}, monitor);
		return retVal[0];
	}
	
	public static SVNChangeStatus []status(ISVNConnector proxy, String path, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		final ArrayList<SVNChangeStatus> statuses = new ArrayList<SVNChangeStatus>();
		proxy.status(path, depth, options, null, new ISVNEntryStatusCallback() {
			public void next(SVNChangeStatus status) {
				statuses.add(status);
			}
		}, monitor);
		return statuses.toArray(new SVNChangeStatus[statuses.size()]);
	}
	
	public static void diffStatus(ISVNConnector proxy, final Collection<SVNDiffStatus> statuses, SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		proxy.diffStatus(reference1, reference2, depth, options, null, new ISVNDiffStatusCallback() {
			public void next(SVNDiffStatus status) {
				statuses.add(status);
			}
		}, monitor);
	}
	
	public static void diffStatus(ISVNConnector proxy, final Collection<SVNDiffStatus> statuses, SVNEntryReference reference, SVNRevision revision1, SVNRevision revision2, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		proxy.diffStatus(reference, revision1, revision2, depth, options, null, new ISVNDiffStatusCallback() {
			public void next(SVNDiffStatus status) {
				statuses.add(status);
			}
		}, monitor);
	}
	
	public static SVNMergeStatus[] mergeStatus(ISVNConnector proxy, SVNEntryReference reference, SVNRevisionRange []revisions, String path, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		final ArrayList<SVNMergeStatus> statuses = new ArrayList<SVNMergeStatus>();
		proxy.mergeStatus(reference, revisions, path, depth, options, new ISVNMergeStatusCallback() {
			public void next(SVNMergeStatus status) {
				statuses.add(status);
			}
		}, monitor);
		return statuses.toArray(new SVNMergeStatus[statuses.size()]);
	}
	
	public static SVNEntry []list(ISVNConnector proxy, SVNEntryRevisionReference reference, int depth, int direntFields, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		final ArrayList<SVNEntry> entries = new ArrayList<SVNEntry>();
		proxy.list(reference, depth, direntFields, options, new ISVNEntryCallback() {
			public void next(SVNEntry entry) {
				entries.add(entry);
			}
		}, monitor);
		return entries.toArray(new SVNEntry[entries.size()]);
	}
	
	public static SVNLogEntry []logEntries(ISVNConnector proxy, SVNEntryReference reference, SVNRevision revisionStart, SVNRevision revisionEnd, long options, String[] revProps, long limit, ISVNProgressMonitor monitor) throws SVNConnectorException {
		final ArrayList<SVNLogEntry> entries = new ArrayList<SVNLogEntry>();
		proxy.logEntries(reference, revisionStart, revisionEnd, revProps, limit, options, new ISVNLogEntryCallback() {
			private Stack<SVNLogEntry> mergeTreeBuilder = new Stack<SVNLogEntry>();
			
			public void next(SVNLogEntry log) {
				if (log.revision == SVNRevision.INVALID_REVISION_NUMBER) {
					if (!this.mergeTreeBuilder.isEmpty()) {
						log = this.mergeTreeBuilder.pop();						
						if (this.mergeTreeBuilder.isEmpty()) {
							entries.add(log);
						}
					}															
					return;
				}
				
				if (!this.mergeTreeBuilder.isEmpty()) {
					this.mergeTreeBuilder.peek().add(log);
				}
				else if (!log.hasChildren()) {
					entries.add(log);
				}
				if (log.hasChildren()) {
					this.mergeTreeBuilder.push(log);
				}
			}
		}, monitor);
		return entries.toArray(new SVNLogEntry[entries.size()]);
	}
	
	public static SVNEntryInfo []info(ISVNConnector proxy, SVNEntryRevisionReference reference, int depth, ISVNProgressMonitor monitor) throws SVNConnectorException {
		final ArrayList<SVNEntryInfo> infos = new ArrayList<SVNEntryInfo>();
		proxy.info(reference, depth, null, new ISVNEntryInfoCallback() {
			public void next(SVNEntryInfo info) {
				infos.add(info);
			}
		}, monitor);
		return infos.toArray(new SVNEntryInfo[infos.size()]);
	}
	
	public static String getStatusText(String status) {
		if (status == null) {
			status = "NotExists"; //$NON-NLS-1$
		}
		return SVNMessages.getString("Status_" + status); //$NON-NLS-1$
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
		return location.isStructureEnabled() ? (baseUrl + "/" + location.getTrunkLocation()) : baseUrl; //$NON-NLS-1$
	}
	
	public static String getProposedBranchesLocation(IRepositoryLocation location) {
		String baseUrl = location.getUrl();
		return location.isStructureEnabled() ? (baseUrl + "/" + location.getBranchesLocation()) : baseUrl; //$NON-NLS-1$
	}
	
	public static String getProposedTagsLocation(IRepositoryLocation location) {
		String baseUrl = location.getUrl();
		return location.isStructureEnabled() ? (baseUrl + "/" + location.getTagsLocation()) : baseUrl; //$NON-NLS-1$
	}
	
	public static IRepositoryRoot []findRoots(String resourceUrl, boolean longestOnly) {
		if (!SVNUtility.isValidSVNURL(resourceUrl)) {
			return new IRepositoryRoot[0];
		}
		IPath url = SVNUtility.createPathForSVNUrl(resourceUrl);
		IRepositoryLocation []locations = SVNRemoteStorage.instance().getRepositoryLocations();
		ArrayList<IRepositoryRoot> roots = new ArrayList<IRepositoryRoot>();
		for (int i = 0; i < locations.length; i++) {
			IPath locationUrl = SVNUtility.createPathForSVNUrl(locations[i].getUrl());
			if (url.segmentCount() < locationUrl.segmentCount() && !url.isPrefixOf(locationUrl)) {
				continue;
			}
			if (locationUrl.isPrefixOf(url) || // performance optimization: repository root URL detection [if is not cached] requires interaction with a remote host
				SVNUtility.createPathForSVNUrl(locations[i].getRepositoryRootUrl()).isPrefixOf(url)) {
				SVNUtility.addRepositoryRoot(roots, (IRepositoryRoot)locations[i].asRepositoryContainer(resourceUrl, false).getRoot(), longestOnly);
			}
		}
		IRepositoryRoot []repositoryRoots = roots.toArray(new IRepositoryRoot[roots.size()]);
		if (!longestOnly) {
			Arrays.sort(repositoryRoots, new Comparator<IRepositoryRoot>() {
				public int compare(IRepositoryRoot first, IRepositoryRoot second) {
					return second.getUrl().compareTo(first.getUrl());
				}
			});
		}
		return repositoryRoots;
	}
	
	private static void addRepositoryRoot(List<IRepositoryRoot> container, IRepositoryRoot root, boolean longestOnly) {
		if (longestOnly && container.size() > 0) {
			int cnt = SVNUtility.createPathForSVNUrl(root.getUrl()).segmentCount();
			int cnt2 = SVNUtility.createPathForSVNUrl(container.get(0).getUrl()).segmentCount();
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
			String name = FileUtility.getEnvironmentVariables().get("SVN_ASP_DOT_NET_HACK") != null ? "_svn" : ".svn"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			SVNUtility.svnFolderName = System.getProperty("javasvn.admindir", name); //$NON-NLS-1$
		}
		return SVNUtility.svnFolderName;
	}
	
	public static String getResourceParent(IRepositoryResource resource) {
		String parent = ""; //$NON-NLS-1$
		String url = resource.getUrl();
		String rootUrl = resource.getRoot().getUrl();
		if (url.equals(rootUrl)) {
			return ""; //$NON-NLS-1$
		}
		parent = url.substring(rootUrl.length(), url.length() - resource.getName().length() - 1);
		return parent;
	}
	
	public static IRepositoryResource copyOf(IRepositoryResource resource) {
		String url = resource.getUrl();
		return resource instanceof IRepositoryFile ? (IRepositoryResource)resource.asRepositoryFile(url, false) : resource.asRepositoryContainer(url, false);
	}
	
	public static IRevisionLink createRevisionLink(IRepositoryResource resource) {
		return new SVNRevisionLink(resource);
	}
	
	public static IRepositoryResource []makeResourceSet(IRepositoryResource upPoint, String relativeReference, boolean isFile) {
		String url = SVNUtility.normalizeURL(upPoint.getUrl() + "/" + relativeReference); //$NON-NLS-1$
		IRepositoryLocation location = upPoint.getRepositoryLocation();
		IRepositoryResource downPoint = isFile ? (IRepositoryResource)location.asRepositoryFile(url, false) : location.asRepositoryContainer(url, false);
		downPoint.setPegRevision(upPoint.getPegRevision());
		downPoint.setSelectedRevision(upPoint.getSelectedRevision());
		return SVNUtility.makeResourceSet(upPoint, downPoint);
	}
	
	public static IRepositoryResource []makeResourceSet(IRepositoryResource upPoint, IRepositoryResource downPoint) {
		ArrayList<IRepositoryResource> resourceSet = new ArrayList<IRepositoryResource>();
		while (downPoint != null && !downPoint.equals(upPoint)) {
			resourceSet.add(0, downPoint);
			downPoint = downPoint.getParent();
		}
		return resourceSet.toArray(new IRepositoryResource[resourceSet.size()]);
	}
	
	public static boolean isValidSVNURL(String url) {
		try {
			URL svnUrl = SVNUtility.getSVNUrl(url);
        	String host = svnUrl.getHost();
        	if (!host.matches("[a-zA-Z0-9_\\-]+(?:\\.[a-zA-Z0-9_\\-]+)*") && host.length() > 0 || //$NON-NLS-1$
        		host.length() == 0 && !"file".equals(svnUrl.getProtocol())) { //$NON-NLS-1$
                return false;
        	}      	
			return true;
		}
		catch (MalformedURLException e) {
			return false;
		}
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
	
	public static void reorder(SVNDiffStatus []statuses, final boolean parent2Child) {
		Arrays.sort(statuses, new Comparator<SVNDiffStatus>() {
			public int compare(SVNDiffStatus d1, SVNDiffStatus d2) {
				return parent2Child ? d1.pathPrev.compareTo(d2.pathPrev) : d2.pathPrev.compareTo(d1.pathPrev);
			}
			
			public boolean equals(Object obj) {
				return false;
			}
		});
	}
	
	public static void reorder(SVNChangeStatus []statuses, final boolean parent2Child) {
		Arrays.sort(statuses, new Comparator<SVNChangeStatus>() {
			public int compare(SVNChangeStatus o1, SVNChangeStatus o2) {
				String s1 = o1 != null ? o1.path : ""; //$NON-NLS-1$
				String s2 = o2 != null ? o2.path : ""; //$NON-NLS-1$
				return parent2Child ? s1.compareTo(s2) : s2.compareTo(s1);
			}
			
			public boolean equals(Object obj) {
				return false;
			}
		});
	}
	
	public static void reorder(IRepositoryResource []resources, final boolean parent2Child) {
		Arrays.sort(resources, new Comparator<IRepositoryResource>() {
			public int compare(IRepositoryResource o1, IRepositoryResource o2) {
				String s1 = o1.getUrl();
				String s2 = o2.getUrl();
				return parent2Child ? s1.compareTo(s2) : s2.compareTo(s1);
			}
			
			public boolean equals(Object obj) {
				return false;
			}
		});
	}
	
    private static final byte[] uri_char_validity = new byte[] {
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 1, 0, 0, 1, 0, 1, 1,   1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1,   1, 1, 1, 0, 0, 1, 0, 0,

        /* 64 */
        1, 1, 1, 1, 1, 1, 1, 1,   1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1,   1, 1, 1, 0, 0, 0, 0, 1,
        0, 1, 1, 1, 1, 1, 1, 1,   1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1,   1, 1, 1, 0, 0, 0, 1, 0,

        /* 128 */
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,

        /* 192 */
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
    };
    
    public static boolean isHexDigit(char ch) {
        return Character.isDigit(ch) ||
               (Character.toUpperCase(ch) >= 'A' && Character.toUpperCase(ch) <= 'F');
    }
    
    private static int hexValue(char ch) {
        if (Character.isDigit(ch)) {
            return ch - '0';
        }
        ch = Character.toUpperCase(ch);
        return (ch - 'A') + 0x0A;
    }
    
	public static String encodeURL(String url) {
		url = SVNUtility.normalizeURL(url);
		int idx = url.startsWith("file:///") ? "file:///".length() : (url.startsWith("file://") ? (url.indexOf("/", "file://".length()) + 1) : (url.indexOf("://") + 3)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		idx = url.indexOf("/", idx); //$NON-NLS-1$
		if (idx == -1) {
			return url;
		}
		String retVal = url.substring(0, idx);	
		String partToEncode = url.substring(idx);
		// user name should be never encoded
		idx = retVal.indexOf('@');
		if (idx != -1) {
			String protocol = retVal.substring(0, retVal.indexOf("://") + 3); //$NON-NLS-1$
			String serverPart = retVal.substring(idx);
			retVal = protocol + retVal.substring(protocol.length(), idx) + serverPart;
		}				
		retVal += doEncode(partToEncode);
		return retVal;
	}
	
	public static String decodeURL(String url) {
		url = SVNUtility.normalizeURL(url);
		int idx = url.startsWith("file:///") ? "file:///".length() : (url.startsWith("file://") ? (url.indexOf("/", "file://".length()) + 1) : (url.indexOf("://") + 3)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		idx = url.indexOf("/", idx); //$NON-NLS-1$
		if (idx == -1) {
			return url;
		}
		String retVal = url.substring(0, idx);
		String partToDecode = url.substring(idx);
		// user name should be never decoded
		idx = retVal.indexOf('@');
		if (idx != -1) {
			String protocol = retVal.substring(0, retVal.indexOf("://") + 3); //$NON-NLS-1$
			String serverPart = retVal.substring(idx);
			retVal = protocol + retVal.substring(protocol.length(), idx) + serverPart;
		}				
		retVal += doDecode(partToDecode);		
		return retVal;
	}
	        
	protected static String doEncode(String src) {
        StringBuffer sb = null;
        byte[] bytes;
        try {
            bytes = src.getBytes("UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            bytes = src.getBytes();
        }
        for (int i = 0; i < bytes.length; i++) {
            int index = bytes[i] & 0xFF;
            if (uri_char_validity[index] > 0) {
                if (sb != null) {
                    sb.append((char) bytes[i]);
                }
                continue;
            }
            if (sb == null) {
                sb = new StringBuffer();
                try {
                    sb.append(new String(bytes, 0, i, "UTF-8")); //$NON-NLS-1$
                } catch (UnsupportedEncodingException e) {
                    sb.append(new String(bytes, 0, i));
                }
            }
            sb.append("%"); //$NON-NLS-1$

            sb.append(Character.toUpperCase(Character.forDigit((index & 0xF0) >> 4, 16)));
            sb.append(Character.toUpperCase(Character.forDigit(index & 0x0F, 16)));
        }
        return sb == null ? src : sb.toString();
	}
	
	protected static String doDecode(String src) {
        // this is string in ASCII-US encoding.
        boolean query = false;
        boolean decoded = false;
        int length = src.length();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
        for(int i = 0; i < length; i++) {
            byte ch = (byte) src.charAt(i);
            if (ch == '?') {
                query = true;
            } else if (ch == '+' && query) {
                ch = ' ';
            } else if (ch == '%' && i + 2 < length &&
                       isHexDigit(src.charAt(i + 1)) &&
                       isHexDigit(src.charAt(i + 2))) {
                ch = (byte) (hexValue(src.charAt(i + 1))*0x10 + hexValue(src.charAt(i + 2)));
                decoded = true;
                i += 2;
            } else {
                // if character is not URI-safe try to encode it.
            }
            bos.write(ch);
        }
        if (!decoded) {
            return src;
        }
        try {
            return new String(bos.toByteArray(), "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
        }
        return src;
	}
	
	public static String normalizeURL(String url) {
		if (url == null) {
			return null;
		}
		url = url.trim();
		
		String prefix = ""; //$NON-NLS-1$
		final String[] knownPrefixes = new String[] {"http://", "https://", "svn://", "svn+ssh://", "file:///", "file://", "^/", "../", "//", "/"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
		for (int i = 0; i < knownPrefixes.length; i ++) {
			if (url.startsWith(knownPrefixes[i])) {
				prefix = knownPrefixes[i];
				url = url.substring(knownPrefixes[i].length());
				break;
			}
		}
				
		StringTokenizer tokenizer = new StringTokenizer(PatternProvider.replaceAll(url, "([\\\\])+", "/"), "/", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    StringBuffer retVal = new StringBuffer();
	    while (tokenizer.hasMoreTokens()) {
	        String token = tokenizer.nextToken();
	        retVal.append(retVal.length() == 0 ? token : ("/" + token)); //$NON-NLS-1$
	    }
	    if (!"".equals(prefix)) { //$NON-NLS-1$
	    	retVal.insert(0, prefix);
	    }	    	    
	    return retVal.toString();
	}
	
	public static Exception validateRepositoryLocation(IRepositoryLocation location) {
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			proxy.list(new SVNEntryRevisionReference(SVNUtility.encodeURL(location.getUrl()), null, null), Depth.EMPTY, SVNEntry.Fields.NONE, ISVNConnector.Options.NONE, new ISVNEntryCallback() {
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
		
		SSLSettings sslSettings = location.getSSLSettings();
		String host = "localhost"; //$NON-NLS-1$
		String protocol = "file"; //$NON-NLS-1$
		try {
			protocol = SVNUtility.getSVNUrl(location.getUrl()).getProtocol();
			if (!protocol.equals("file")) { //$NON-NLS-1$
				host = SVNUtility.getSVNUrl(location.getUrl()).getHost();
			}
		}
		catch (MalformedURLException ex) {
			//skip
		}
		IProxyService proxyService = SVNTeamPlugin.instance().getProxyService();
		String proxyType = protocol.equals("https") ? IProxyData.HTTPS_PROXY_TYPE : IProxyData.HTTP_PROXY_TYPE; //$NON-NLS-1$
    	SVNCachedProxyCredentialsManager proxyCredetialsManager = SVNRemoteStorage.instance().getProxyCredentialsManager();
		IProxyData proxyData = proxyService.getProxyDataForHost(host, proxyType);
	    if (proxyService.isProxiesEnabled() && proxyData != null) {
	    	proxy.setProxy(proxyData.getHost(), proxyData.getPort(), proxyCredetialsManager.getUsername(), proxyCredetialsManager.getPassword());
	    }
	    else {
	    	proxy.setProxy(null, -1, null, null);
	    }
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
	
	public static SVNChangeStatus getSVNInfoForNotConnected(IResource root) {
		IPath location = FileUtility.getResourcePath(root);
		IPath checkedPath = root.getType() == IResource.FILE ? location.removeLastSegments(1) : location;
		if (!checkedPath.append(SVNUtility.getSVNFolderName()).toFile().exists()) {
			return null;
		}
		ISVNConnector proxy = CoreExtensionsManager.instance().getSVNConnectorFactory().newInstance();
		try {
			SVNChangeStatus []st = SVNUtility.status(proxy, location.toString(), Depth.IMMEDIATES, ISVNConnector.Options.INCLUDE_UNCHANGED, new SVNNullProgressMonitor());
			if (st != null && st.length > 0) {
				SVNUtility.reorder(st, true);
				return st[0].url == null ? null : st[0];
			}
			return null;
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
			SVNProperty data = proxy.getProperty(new SVNEntryRevisionReference(location, null, SVNRevision.WORKING), propertyName, new SVNNullProgressMonitor());
			return data == null ? null : data.value;
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

	public static Map<IProject, List<IResource>> splitWorkingCopies(IResource []resources) {
		Map<IProject, List<IResource>> wc2Resources = new HashMap<IProject, List<IResource>>();
		
		for (int i = 0; i < resources.length; i++) {
			IProject wcRoot = resources[i].getProject();

			List<IResource> wcResources = wc2Resources.get(wcRoot);
			if (wcResources == null) {
				wc2Resources.put(wcRoot, wcResources = new ArrayList<IResource>());
			}
			wcResources.add(resources[i]);
		}

		return wc2Resources;
	}
	
	public static Map splitWorkingCopies(File []files) {
		Map wc2Resources = new HashMap();
		
		ISVNConnector proxy = CoreExtensionsManager.instance().getSVNConnectorFactory().newInstance();
		try {
			Map<File, SVNEntryInfo> file2info = new HashMap<File, SVNEntryInfo>();
			for (int i = 0; i < files.length; i++) {
				file2info.put(files[i], SVNUtility.getSVNInfo(files[i], proxy));
			}
			
			ArrayList<File> restOfFiles = new ArrayList<File>(Arrays.asList(files));
			while (restOfFiles.size() > 0) {
				File current = restOfFiles.get(0);
				SVNEntryInfo info = file2info.get(current);
				Object []wcRoot = SVNUtility.getWCRoot(proxy, current, info);
				
				List wcResources = (List)wc2Resources.get(wcRoot[0]);
				if (wcResources == null) {
					wc2Resources.put(wcRoot[0], wcResources = new ArrayList());
				}
				
				IPath rootPath = new Path(((File)wcRoot[0]).getAbsolutePath());
				IPath rootInfoPath = SVNUtility.createPathForSVNUrl(((SVNEntryInfo)wcRoot[1]).url);
				for (Iterator it = restOfFiles.iterator(); it.hasNext(); ) {
					File checked = (File)it.next();
					if (rootPath.isPrefixOf(new Path(checked.getAbsolutePath()))) {
						if (rootInfoPath.isPrefixOf(SVNUtility.createPathForSVNUrl(file2info.get(checked).url))) {
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
				else if (!SVNUtility.createPathForSVNUrl(rootInfo.url).isPrefixOf(SVNUtility.createPathForSVNUrl(oldInfo.url))) {
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
			String errMessage = SVNMessages.formatErrorString("Error_NonSVNPath", new String[] {oldRoot.getAbsolutePath()}); //$NON-NLS-1$
			throw new RuntimeException(errMessage);
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
			svnMeta = new File(svnMeta.getAbsolutePath() + "/" + SVNUtility.getSVNFolderName()); //$NON-NLS-1$
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
			return f.isDirectory() ? Kind.DIR : Kind.FILE;
		}
		else if (kind == Kind.DIR) {
			return Kind.DIR;
		}
		else if (kind == Kind.FILE) {
			return Kind.FILE;
		}
		// ignore files absent in the WC base and WC working. But what is the reason why it is reported ?
		if (ignoreNone) {
			return Kind.NONE;
		}
		String errMessage = SVNMessages.format("Error_UnrecognizedNodeKind", new String[] {String.valueOf(kind), path}); //$NON-NLS-1$
		throw new RuntimeException(errMessage);
	}
	
	public static IRepositoryResource []shrinkChildNodes(IRepositoryResource []resources) {
		Set<IRepositoryResource> roots = new HashSet<IRepositoryResource>(Arrays.asList(resources));
		for (int i = 0; i < resources.length; i++) {
			if (SVNUtility.hasRoots(roots, resources[i])) {
				roots.remove(resources[i]);
			}
		}
		return roots.toArray(new IRepositoryResource[roots.size()]);
	}
	
	public static IRepositoryResource []getCommonParents(IRepositoryResource []resources) {
		Map<IRepositoryResource, ArrayList> byRepositoryRoots = new HashMap<IRepositoryResource, ArrayList>();
		for (int i = 0; i < resources.length; i++) {
			IRepositoryResource root = resources[i].getRoot();
			ArrayList tmp = byRepositoryRoots.get(root);
			if (tmp == null) {
				byRepositoryRoots.put(root, tmp = new ArrayList());
			}
			tmp.add(resources[i]);
		}
		HashSet<IRepositoryResource> container = new HashSet<IRepositoryResource>();
		for (ArrayList tmp : byRepositoryRoots.values()) {
			IRepositoryResource parent = SVNUtility.getCommonParent((IRepositoryResource [])tmp.toArray(new IRepositoryResource[tmp.size()]));
			if (parent != null) {
				container.add(parent);
			}
		}
		return container.toArray(new IRepositoryResource[container.size()]);
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
	
	/**
	 * Compares two revisions
	 * 
	 * @return: 0 if equal, -1 if less, 1 if more
	 */
	public static int compareRevisions(SVNRevision first, SVNRevision second, SVNEntryRevisionReference referenceFirst, SVNEntryRevisionReference referenceSecond, ISVNConnector proxy)  throws SVNConnectorException {
		if (first.getKind() == SVNRevision.Kind.NUMBER && second.getKind() == SVNRevision.Kind.NUMBER) {
			SVNRevision.Number fromNumber = (SVNRevision.Number)first;
			SVNRevision.Number toNumber = (SVNRevision.Number)second;
			return fromNumber.getNumber() > toNumber.getNumber() ? 1 : (fromNumber.getNumber() == toNumber.getNumber() ? 0 : -1);
		}
		SVNRevision.Date fromDate = null;
		SVNRevision.Date toDate = null;
		if (first.getKind() == SVNRevision.Kind.DATE) {
			fromDate = (SVNRevision.Date)first;
		}
		else {
			SVNEntryInfo []entryInfo = SVNUtility.info(proxy, referenceFirst, Depth.UNKNOWN, new SVNNullProgressMonitor());
			fromDate = SVNRevision.fromDate(entryInfo[0].lastChangedDate);
		}
		if (second.getKind() == SVNRevision.Kind.DATE) {
			toDate = (SVNRevision.Date)second;
		}
		else {
			SVNEntryInfo []entryInfo = SVNUtility.info(proxy, referenceSecond, Depth.UNKNOWN, new SVNNullProgressMonitor());
			toDate = SVNRevision.fromDate(entryInfo[0].lastChangedDate);
		}
		return fromDate.getDate() > toDate.getDate() ? 1 : (fromDate.getDate() == toDate.getDate() ? 0 : -1);
	}

	private static boolean isMergeParts(IResource resource) {
		String ext = resource.getFileExtension();
		return ext != null && ext.matches("r(\\d)+"); //$NON-NLS-1$
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
			repository2Resources.put(locationUtility.location, entry.getValue());
		}	
	    return repository2Resources;
	}
	
	private static boolean hasRoots(Set<IRepositoryResource> roots, IRepositoryResource node) {
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
			IPath baseUrl = SVNUtility.createPathForSVNUrl(base.getUrl());
			for (int i = 0; i < resources.length; i++) {
				if (baseUrl.isPrefixOf(SVNUtility.createPathForSVNUrl(resources[i].getUrl()))) {
					startsCnt++;
				}
			}
			if (startsCnt == resources.length) {
				break;
			}
			base = base.getParent();
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
	
	/**
	 * @param resources
	 * @return projects which contain modifications on tag
	 */
	public static IProject[] getTagOperatedProjects(IResource[] resources) {
		Set<IProject> operatedProjects = new HashSet<IProject>();
		for (int i = 0; i < resources.length; i++) {
			IProject project = resources[i].getProject();
			if (project != null && !operatedProjects.contains(project)) {
				SVNTeamProvider provider = (SVNTeamProvider) RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID);
				if (provider != null && provider.isVerifyTagOnCommit() && ((IRepositoryRoot)SVNRemoteStorage.instance().asRepositoryResource(resources[i]).getRoot()).getKind() == IRepositoryRoot.KIND_TAGS) {
					operatedProjects.add(project);
				}
			}	
        }
		return operatedProjects.toArray(new IProject[0]);
	}
	
	public static String getDepthArg(int depth) {
		String depthArg = " --depth "; //$NON-NLS-1$
		if (depth == Depth.EMPTY) {
			return depthArg + "empty "; //$NON-NLS-1$
		}
		if (depth == Depth.INFINITY) {
			return depthArg + "infinity" ; //$NON-NLS-1$
		}
		if (depth == Depth.IMMEDIATES) {
			return depthArg + "immediates "; //$NON-NLS-1$
		}
		if (depth == Depth.UNKNOWN) {
			return ""; //$NON-NLS-1$
		}
		return depthArg + "files "; //$NON-NLS-1$
	}
	
	public static String getIgnoreExternalsArg(boolean ignoreExternals) {
		return ignoreExternals ? " --ignore-externals" : ""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Work with svn externals, e.g. parses
	 *
	 * 
	 * @author Igor Burilo
	 */
	public static class SVNExternalPropertyData {
		public String localPath;
		public String url;
		public String revision;
		public String pegRevision;
		public boolean isNewFormat;

		public SVNExternalPropertyData(String localPath, String url, String pegRevision, String revision, boolean isNewFormat) {				
			super();
			this.isNewFormat = isNewFormat;
			this.localPath = localPath;
			this.pegRevision = pegRevision;
			this.revision = revision;
			this.url = url;
		}
		
		private static String[] splitExternalOnParts(String str, boolean isCheckSpacesInLocalPath) {
			if (str == null) {
				return new String[0];
			}
			str = str.trim();
			
			if (!isCheckSpacesInLocalPath) {
				return str.trim().split("[\\t ]+"); //$NON-NLS-1$
			}
			
			//process that local path can be enclosed in quotes or contain the \ (backslash) character for escaping characters
			List<String> parts = new ArrayList<String>();			
			StringBuffer tmpString = new StringBuffer();
			boolean hasQuote = false; 
			for (int i = 0; i < str.length(); i ++) {
				char ch = str.charAt(i);
				if (ch == '\'' || ch == '"') {
					hasQuote = !hasQuote;
				} else if (ch == ' ' || ch == '\t') {
					if (hasQuote) {
						tmpString.append(ch);
					} else if (tmpString.length() > 0){
						parts.add(tmpString.toString());
						tmpString.setLength(0);
					}					
				} else if (ch == '\\' && i + 1 < str.length() - 1 && str.charAt(i + 1) == ' ') {
					//if space character is escaped with backslash(\) then doesn't separate string to parts here
					tmpString.append(' ');
					i ++;
				} else {
					tmpString.append(ch);	
				}				
			}
			parts.add(tmpString.toString());			
			return parts.toArray(new String[0]);
		}
		
		/**
		 * Parse external property and return result in raw format, i.e. 
		 * it doesn't process and encode url, it doesn't parse revisions etc.
		 * 
		 * @param property
		 * @return
		 */
		public static SVNExternalPropertyData[] parse(String property) {
			if (property == null) {
				return new SVNExternalPropertyData[0];
			}
			
			List<SVNExternalPropertyData> resList = new ArrayList<SVNExternalPropertyData>();
							
			String []externals = property.trim().split("[\\n|\\r\\n]+"); // it seems different clients have different behaviours wrt trailing whitespace.. so trim() to be safe //$NON-NLS-1$
			for (int i = 0; i < externals.length; i++) {
				boolean isCheckSpacesInLocalPath = CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() >= ISVNConnectorFactory.APICompatibility.SVNAPI_1_6_x;
				String[] parts = SVNExternalPropertyData.splitExternalOnParts(externals[i], isCheckSpacesInLocalPath);
				// 2 - name + URL
				// 3 - name + -rRevision + URL
				// 4 - name + -r + Revision + URL
				//or in SVN 1.5 format
				// 2 - URL@peg + name
				// 3 - -rRevision + URL@peg + name
				// 4 - -r + Revision + URL@peg + name
				if (parts.length < 2 || parts.length > 4) {
					throw new UnreportableException("Malformed external, " + parts.length + ", " + externals[i]);  //$NON-NLS-1$//$NON-NLS-2$
				}
					
				String name = null;
				String url = null;
				String revision = null;
				String pegRevision = null;
				boolean isNewFormat = false;
				
				if (SVNUtility.isValidSVNURL(parts[parts.length - 1])) {
					isNewFormat = false;
					name = parts[0];
					
					url = parts[1];
					if (parts.length == 4) {
						revision = parts[2];
						url = parts[3];
					}
					else if (parts.length == 3) {
						revision = parts[1].substring(2);
						url = parts[2];
					}								
				} else {
					isNewFormat = true;
					name = parts[parts.length - 1];
					
					url = parts[0];
					if (parts.length == 4) {
						revision = parts[1];
						url = parts[2];
					}
					else if (parts.length == 3) {
						revision = parts[0].substring(2);
						url = parts[1];
					}
																																	
					int idx = url.lastIndexOf('@');				
					if (idx != -1) {
						pegRevision = url.substring(idx + 1);
						url = url.substring(0, idx);
					}				
				}
							
				SVNExternalPropertyData data = new SVNExternalPropertyData(name, url, pegRevision, revision, isNewFormat);
				resList.add(data);			
			}
			return resList.toArray(new SVNExternalPropertyData[0]);
		}			
		
		public String toString() {
			StringBuffer res = new StringBuffer();			
			if (this.isNewFormat) {
				//Example: -r12 http://svn.example.com/skin-maker@21 third-party/skins/toolkit
				if (this.revision != null) {
					res.append("-r").append(this.revision).append("\t"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				res.append(this.url);
				if (this.pegRevision != null) {
					res.append("@").append(this.pegRevision); //$NON-NLS-1$
				}
				res.append("\t").append(this.localPath); //$NON-NLS-1$
			} else {
				//Example: third-party/skins -r148 http://svn.example.com/skinproj
				res.append(this.localPath).append("\t"); //$NON-NLS-1$
				if (this.revision != null) {
					res.append("-r").append(this.revision).append("\t"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				res.append(this.url);
			}			
			return res.toString();
		}
	}

	/**
	 * This method should be used instead of creating {@link Path} directly when you need to manipulate with URLs.
	 * 
	 * @param fullPath
	 * @return
	 */
	public static IPath createPathForSVNUrl(String fullPath) {
		return new PathForURL(fullPath, true);
	}
	
	private SVNUtility() {
		
	}
}
