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

package org.eclipse.team.svn.core.svnstorage;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.preferences.Base64;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNCredentialsPrompt;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNEntryInfo;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.options.IOptionProvider;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.resource.IRevisionLink;
import org.eclipse.team.svn.core.resource.SSHSettings;
import org.eclipse.team.svn.core.resource.SSLSettings;
import org.eclipse.team.svn.core.resource.events.IRepositoryLocationStateListener;
import org.eclipse.team.svn.core.resource.events.ISSHSettingsStateListener;
import org.eclipse.team.svn.core.resource.events.ISSLSettingsStateListener;
import org.eclipse.team.svn.core.utility.ILoggedOperationFactory;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNURLStreamHandler;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * SVN based representation of IRepositoryLocation
 * 
 * @author Alexander Gurov
 */
public class SVNRepositoryLocation extends SVNRepositoryBase implements IRepositoryLocation, ISSHSettingsStateListener, ISSLSettingsStateListener, Serializable {
    private static final int PROXY_CACHE_SIZE = 5;
	private static final long serialVersionUID = -5820937379741639580L;
	
	public static final int DEFAULT_CONNECTION = 0;
    public static final int SSH_CONNECTION = 1;
    public static final int SSL_CONNECTION = 2;
    public static final int PROXY_CONNECTION = 3;
    
	protected String id;
	protected String label;
	protected String trunk;
	protected String branches;
	protected String tags;
	protected boolean trunkEnabled;
	protected String username;
	protected String repositoryRootUrl;
	protected String repositoryUUID;
	// Base64 encoded to hide in the serialized content
	protected String password;
	protected boolean passwordSaved;
	private transient String passwordTemporary;
	private List<byte[]> serializedRevisionLinks;
	private SSLSettings sslSettings;
	private SSHSettings sshSettings;
	
	private transient List<ISVNConnector> proxyCache;
	private transient HashSet<ISVNConnector> usedProxies;
	private transient HashMap<Thread, ProxyHolder> thread2Proxy;
	private transient IRevisionLink []revisionLinks;
    protected transient boolean trustSiteDefined;
    protected transient int trustSite;
    protected transient int proxyConfigurationState;
    
    protected boolean authorNameEnabled;
    protected String authorName;
    
    private Map<String, IRepositoryLocation> additionalRealms;
    //used for differed in time realms initialization
    private transient List<String> rawRealms = new ArrayList<String>();
    private transient List<IRepositoryLocationStateListener> changeListeners = new ArrayList<IRepositoryLocationStateListener>();
    
    private transient Integer lazyInitLock = new Integer(0);
    private transient Integer proxyManagerLock = new Integer(0);
    private transient Integer repositoryRootLock = new Integer(0);
    private transient Integer authInitLock = new Integer(0);
    
    /*
     * Used for differed in time authentication retrieving.
     * We need it in order to avoid deadlocks in case of loading 
     * authentication info initiated from Plugin#start.
     */    
    protected transient boolean isRetrieveAuthInfo;    
      
	public SVNRepositoryLocation() {
		super(null);
	}

	public SVNRepositoryLocation(String id) {
		super(null);
		this.id = id;
	}
	
	public void addStateListener(IRepositoryLocationStateListener listener) {
		synchronized (this.changeListeners) {
			this.changeListeners.add(listener);
		}
	}

	public void removeStateListener(IRepositoryLocationStateListener listener) {
		synchronized (this.changeListeners) {
			this.changeListeners.remove(listener);
		}
	}

	public String asReference(LocationReferenceTypeEnum locationReferenceType) {
		String reference = this.id;
		reference += ";" + this.getUrlAsIs(); //$NON-NLS-1$
		if (locationReferenceType == LocationReferenceTypeEnum.ONLY_REQUIRED_DATA) {
			return reference;
		}		
		reference += ";" + this.getLabel(); //$NON-NLS-1$
		reference += ";" + this.getBranchesLocation(); //$NON-NLS-1$
		reference += ";" + this.getTagsLocation(); //$NON-NLS-1$
		reference += ";" + this.getTrunkLocation(); //$NON-NLS-1$
		reference += ";" + this.trunkEnabled; //$NON-NLS-1$
		reference += ";" + ((this.repositoryUUID == null) ? "" : this.repositoryUUID); //$NON-NLS-1$ //$NON-NLS-2$
		reference += ";" + ((this.repositoryRootUrl == null) ? "" : this.repositoryRootUrl); //$NON-NLS-1$ //$NON-NLS-2$
		reference += ";" + this.getAuthorName(); //$NON-NLS-1$
		reference += ";" + this.authorNameEnabled + ";"; //$NON-NLS-1$ //$NON-NLS-2$
		String [] realms = this.getRealms().toArray(new String [0]);
		for (int i = 0; i < realms.length; i++) {
			if (i < realms.length - 1) {
				reference += realms[i] + "^"; //$NON-NLS-1$
			}
			else {
				reference += realms[i];
			}			
		}
		reference += ";"; //$NON-NLS-1$
		
		IRevisionLink[] revisionLinks = this.getRevisionLinks();
		for (int i = 0; i < revisionLinks.length; i++) {
			String base64revLink = new String(Base64.encode(SVNRemoteStorage.instance().revisionLinkAsBytes(revisionLinks[i], locationReferenceType != LocationReferenceTypeEnum.WITHOUT_REVISION_COMMENTS)));
			if (i < revisionLinks.length - 1) {
				reference += base64revLink + "^"; //$NON-NLS-1$
			}
			else {
				reference += base64revLink;
			}
		}
		
		//write this line only for compatibility issues, previously it contained ssh port
		reference += ";" + 0; //$NON-NLS-1$
		
		return reference;
	}
	
	public void fillLocationFromReference(String[] referenceParts) {
		boolean containRevisionLinks = false;
		switch (referenceParts.length) {
		case 14:
			//check ssh port for compatibility issues 
			int sshPort = Integer.parseInt(referenceParts[13]);
			if (sshPort != 0) {
				this.getSSHSettings().setPort(sshPort);
			}
		case 13:
			if (!referenceParts[12].equals("")) { //$NON-NLS-1$
				containRevisionLinks = true;
			}
		case 12:
			if (!referenceParts[11].equals("")) { //$NON-NLS-1$
				this.rawRealms.addAll(Arrays.asList(referenceParts[11].split("\\^"))); //$NON-NLS-1$
			}
		case 11:
			this.setAuthorNameEnabled(referenceParts[10].equals("true")); //$NON-NLS-1$
		case 10:
			this.setAuthorName(referenceParts[9].trim());
		case 9:
			this.repositoryRootUrl = (referenceParts[8].trim().equals("") ? null : referenceParts[8].trim()); //$NON-NLS-1$
		case 8:
			this.repositoryUUID = (referenceParts[7].trim().equals("") ? null : referenceParts[7].trim()); //$NON-NLS-1$
		case 7:
			this.setStructureEnabled(referenceParts[6].equals("true")); //$NON-NLS-1$
		case 6:
			this.setTrunkLocation(referenceParts[5].trim());
		case 5:
			this.setTagsLocation(referenceParts[4].trim());
		case 4:
			this.setBranchesLocation(referenceParts[3].trim());
		case 3:
			String label = referenceParts[2].trim();
			if (label.length() > 0) {
				this.setLabel(label);
			}
		case 2:
			this.setUrl(referenceParts[1].trim());
		case 1:
		}
		if (this.label == null || this.label.length() == 0) {
			this.label = this.url;
		}
		if (containRevisionLinks) {
			String [] revLinks = referenceParts[12].split("\\^"); //$NON-NLS-1$
			for (int i = 0 ; i < revLinks.length; i++) {
				this.addRevisionLink(SVNRemoteStorage.instance().revisionLinkFromBytes(Base64.decode(revLinks[i].getBytes()), this));
			}
		}
		this.setRetrieveAuthInfo(true);
	}

	public Collection<String> getRealms() {
		return this.getAdditionalRealms().keySet();
	}
	
	public void addRealm(String realm, IRepositoryLocation location) {
		this.getAdditionalRealms(false).put(realm, location);
		this.fireRealmAdded(realm, location);
	}
	
	public void removeRealm(String realm) {
		this.fireRealmRemoved(realm);
		this.getAdditionalRealms().remove(realm);
	}
	
	public Collection<IRepositoryLocation> getRealmLocations() {
		return this.getAdditionalRealms().values();
	}
	
	public IRepositoryLocation getLocationForRealm(String realm) {
		return this.getAdditionalRealms().get(realm);
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return this.getUrl();
	}
	
	public String getUrlAsIs() {
	    return super.getUrl();
	}
	
	public String getUrl() {
	    return this.getUrlImpl(super.getUrl());
	}

	public String getLabel() {
		return this.label == null ? this.getUrl() : this.label;
	}
	
	public String getRepositoryRootUrl() {
		this.fetchRepoInfo();
		return this.repositoryRootUrl == null ? this.getUrl() : this.repositoryRootUrl;
	}
	
	public String getRepositoryUUID() {
		this.fetchRepoInfo();
		return this.repositoryUUID;
	}
	
	public IRepositoryRoot getRepositoryRoot() {
		return new SVNRepositoryRoot(this);
	}

	public IRepositoryRoot getRoot() {
		return new SVNRepositoryLocationRoot(this);
	}

	public boolean isStructureEnabled() {
		return this.trunkEnabled;
	}

	public void setStructureEnabled(boolean structureEnabled) {
		boolean oldValue = this.trunkEnabled;
		this.trunkEnabled = structureEnabled;
		this.fireChanged(IRepositoryLocationStateListener.STRUCTURE_ENABLED, Boolean.valueOf(oldValue), Boolean.valueOf(this.trunkEnabled));
	}

	public String getUserInputTrunk() {
		return this.trunk == null ? "" : this.trunk; //$NON-NLS-1$
	}

	public String getUserInputTags() {
		return this.tags == null ? "" : this.tags; //$NON-NLS-1$
	}

	public String getUserInputBranches() {
		return this.branches == null ? "" : this.branches; //$NON-NLS-1$
	}

	public String getTrunkLocation() {
		return (this.trunk == null || !this.isStructureEnabled()) ? "" : this.trunk; //$NON-NLS-1$
	}

	public String getBranchesLocation() {
		return (this.branches == null || !this.isStructureEnabled()) ? "" : this.branches; //$NON-NLS-1$
	}

	public String getTagsLocation() {
		return (this.tags == null || !this.isStructureEnabled()) ? "" : this.tags; //$NON-NLS-1$
	}
	
	public boolean isAuthorNameEnabled() {
		return this.authorNameEnabled;
	}
	
	public String getAuthorName() {
		return this.authorName == null ? "" : this.authorName; //$NON-NLS-1$
	}

    public IRepositoryContainer asRepositoryContainer(String url, boolean allowsNull) {
    	return SVNRepositoryLocation.asRepositoryContainer(this, url, allowsNull);
    }

    public IRepositoryFile asRepositoryFile(String url, boolean allowsNull) {
    	return SVNRepositoryLocation.asRepositoryFile(this, url, allowsNull);
    }
	
    public static IRepositoryContainer asRepositoryContainer(IRepositoryLocation location, String url, boolean allowsNull) {
    	if (!SVNRepositoryLocation.isArgumentsCorrect(location, url, allowsNull)) {
    		return null;
    	}
    	
    	IPath urlPath = SVNUtility.createPathForSVNUrl(url);
    	String name = urlPath.lastSegment();
    	
    	if (location.isStructureEnabled()) {
    		boolean regularFolder = false;
    		if (name.equals(location.getTrunkLocation()) || name.equals(location.getTagsLocation()) || name.equals(location.getBranchesLocation())) {
	    		IPath tPath = SVNUtility.createPathForSVNUrl(url);
	    		String tName = null;
	    		while ((tPath = tPath.removeLastSegments(1)) != null && !tPath.isEmpty() && (tName = tPath.lastSegment()) != null) {
	    			if (tName.equals(location.getTrunkLocation()) || tName.equals(location.getBranchesLocation()) || tName.equals(location.getTagsLocation())) {
	    				regularFolder = true;
	    				break;
	    			}
	    		}
    		}
    		if (!regularFolder) {
	            if (name.equals(location.getTrunkLocation())) {
	                return new SVNRepositoryTrunk(location, url, SVNRevision.HEAD);
	            }
	            if (name.equals(location.getTagsLocation())) {
	                return new SVNRepositoryTags(location, url, SVNRevision.HEAD);
	            }
	            if (name.equals(location.getBranchesLocation())) {
	                return new SVNRepositoryBranches(location, url, SVNRevision.HEAD);
	            }
    		}
    	}
    	IPath locationUrl = SVNUtility.createPathForSVNUrl(location.getUrl());
        if (urlPath.equals(locationUrl)) {
            return location.getRoot();
        }
        if (locationUrl.isPrefixOf(urlPath)) {
        	// do not access repository root if it is not required
            return new SVNRepositoryFolder(location, url, SVNRevision.HEAD);
        }
        if (urlPath.equals(SVNUtility.createPathForSVNUrl(location.getRepositoryRootUrl()))) {
            return location.getRepositoryRoot();
        }
        return new SVNRepositoryFolder(location, url, SVNRevision.HEAD);
    }

    public static IRepositoryFile asRepositoryFile(IRepositoryLocation location, String url, boolean allowsNull) {
    	if (!SVNRepositoryLocation.isArgumentsCorrect(location, url, allowsNull)) {
    		return null;
    	}
        return new SVNRepositoryFile(location, url, SVNRevision.HEAD);
    }
	
	public String getUsername() {
		this.checkAuthInfo();
		return this.username;
	}

	public String getPassword() {
		this.checkAuthInfo();
		return this.passwordSaved ? SVNUtility.base64Decode(this.password) : SVNUtility.base64Decode(this.passwordTemporary);
	}

	public boolean isPasswordSaved() {
		this.checkAuthInfo();
		return this.passwordSaved;
	}

	public IRevisionLink []getRevisionLinks() {
		synchronized (this.lazyInitLock) {
			if (this.revisionLinks == null) {
				List<byte[]> serialized = this.getSerializedRevisionLinks();
				this.revisionLinks = new IRevisionLink[serialized.size()];
				for (int i = 0; i < this.revisionLinks.length; i++) {
					byte []data = (byte [])serialized.get(i);
					this.revisionLinks[i] = SVNRemoteStorage.instance().revisionLinkFromBytes(data, this);
				}
			}
			return this.revisionLinks;
		}
	}
	
	public void addRevisionLink(IRevisionLink link) {
		synchronized (this.lazyInitLock) {
			IRevisionLink []links = this.getRevisionLinks();
			int idx = -1;
			for (int i = 0; i < links.length; i++) {
				if (links[i].equals(link) && links[i].getRepositoryResource().getSelectedRevision().equals(link.getRepositoryResource().getSelectedRevision())) {
					idx = i;
					break;
				}
			}
			if (idx == -1) {
				List<byte[]> serialized = this.getSerializedRevisionLinks();
				serialized.add(SVNRemoteStorage.instance().revisionLinkAsBytes(link, true));
				this.revisionLinks = null;
			}
		}
		this.fireRevisionLinkAdded(link);
	}
	
	public void removeRevisionLink(IRevisionLink link) {
		this.fireRevisionLinkRemoved(link);
		synchronized (this.lazyInitLock) {
			IRevisionLink []links = this.getRevisionLinks();
			int idx = -1;
			for (int i = 0; i < links.length; i++) {
				if (links[i].equals(link) && links[i].getRepositoryResource().getSelectedRevision().equals(link.getRepositoryResource().getSelectedRevision())) {
					idx = i;
					break;
				}
			}
			if (idx != -1) {
				List<byte[]> serialized = this.getSerializedRevisionLinks();
				serialized.remove(idx);
				this.revisionLinks = null;
			}
		}
	}
	
	public void setLabel(String label) {
		String oldValue = this.label;
		this.label = label;
		this.fireChanged(IRepositoryLocationStateListener.LABEL, oldValue, this.label);
	}

	public void setUrl(String url) {
		String oldValue = this.url;
		
		String oldRootUrl = this.getRepositoryRootUrl();
		IRevisionLink[] oldLinks = this.getRevisionLinks();
		List<byte[]> serialized = this.getSerializedRevisionLinks();
		
		this.url = url;
		
		if (oldRootUrl != null && !SVNUtility.createPathForSVNUrl(oldRootUrl).isPrefixOf(SVNUtility.createPathForSVNUrl(this.getUrl()))) {
			this.repositoryRootUrl = null;
			this.repositoryUUID = null;
			
			if (oldLinks.length > 0) {
				String newRootUrl = this.getRepositoryRootUrl();
				synchronized (this.lazyInitLock) {
					for (int i = 0; i < oldLinks.length; i++) {
						String linkUrl = oldLinks[i].getRepositoryResource().getUrl();
						int idx = linkUrl.indexOf(oldRootUrl);
						if (idx == -1) {
							serialized.set(i, null);
						}
						else {
							linkUrl = newRootUrl + linkUrl.substring(idx + oldRootUrl.length());
							IRepositoryResource tmpResource = oldLinks[i] instanceof IRepositoryFile ? (IRepositoryResource)this.asRepositoryFile(linkUrl, false) : this.asRepositoryContainer(linkUrl, false);
							IRevisionLink link = SVNUtility.createRevisionLink(tmpResource);							
							link.getRepositoryResource().setPegRevision(oldLinks[i].getRepositoryResource().getPegRevision());
							link.getRepositoryResource().setSelectedRevision(oldLinks[i].getRepositoryResource().getSelectedRevision());
							link.setComment(oldLinks[i].getComment());							
							serialized.set(i, SVNRemoteStorage.instance().revisionLinkAsBytes(link, true));
						}
					}
					for (Iterator<byte[]> it = serialized.iterator(); it.hasNext(); ) {
						if (it.next() == null) {
							it.remove();
						}
					}
					this.revisionLinks = null;
				}
			}
		}
		this.fireChanged(IRepositoryLocationStateListener.URL, oldValue, this.url);
	}
	
	public void setTrunkLocation(String location) {
		String oldValue = this.trunk;
		this.trunk = location;
		this.fireChanged(IRepositoryLocationStateListener.TRUNK_LOCATION, oldValue, this.trunk);
	}

	public void setBranchesLocation(String location) {
		String oldValue = this.branches;
		this.branches = location;
		this.fireChanged(IRepositoryLocationStateListener.BRANCHES_LOCATION, oldValue, this.branches);
	}

	public void setTagsLocation(String location) {
		String oldValue = this.tags;
		this.tags = location;
		this.fireChanged(IRepositoryLocationStateListener.TAGS_LOCATION, oldValue, this.tags);
	}

	public void setUsername(String username) {
		String oldValue = this.username;
		this.username = username;
		this.fireChanged(IRepositoryLocationStateListener.USERNAME, oldValue, this.username);
	}
	
	public void setAuthorNameEnabled(boolean isEnabled) {
		boolean oldValue = this.authorNameEnabled;
		this.authorNameEnabled = isEnabled;
		this.fireChanged(IRepositoryLocationStateListener.AUTHOR_NAME_ENABLED, Boolean.valueOf(oldValue), Boolean.valueOf(this.authorNameEnabled));
	}

	public void setAuthorName(String name) {
		String oldValue = this.authorName;
		this.authorName = name;
		this.fireChanged(IRepositoryLocationStateListener.AUTHOR_NAME, oldValue, this.authorName);
	}
	
	public void setPassword(String password) {
		String oldValue = !this.passwordSaved ? this.passwordTemporary : this.password;
		oldValue = oldValue != null ? SVNUtility.base64Decode(oldValue) : oldValue;
		if (!this.passwordSaved) {
			this.passwordTemporary = SVNUtility.base64Encode(password);
		}
		else {
			this.password = SVNUtility.base64Encode(password);
		}
		this.fireChanged(IRepositoryLocationStateListener.PASSWORD, oldValue, password);
	}

	public void setPasswordSaved(boolean saved) {
		if (this.passwordSaved == saved) {
			return;
		}
		boolean oldValue = this.passwordSaved;
		this.passwordSaved = saved;
		if (!saved) {
			this.passwordTemporary = this.password;
			this.password = null;
		}
		else {
			this.password = this.passwordTemporary;
		}
		this.fireChanged(IRepositoryLocationStateListener.PASSWORD_SAVED, Boolean.valueOf(oldValue), Boolean.valueOf(this.passwordSaved));
	}

	public ISVNConnector acquireSVNProxy() {
		ISVNConnector retVal = null;
		boolean isNew = false;
		synchronized (this.proxyManagerLock) {
			try {
				// initialize proxy cache, usedProxies list and thread2Proxy map
			    List<ISVNConnector> cache = this.getProxyCache();
			    
				// make the method reenterable: the same thread must use the same proxy. Access from call-backs must be controlled by programmer
				Thread current = Thread.currentThread();
				ProxyHolder holder = this.thread2Proxy.get(current);
				if (holder != null) {
					holder.referenceCounter++;
					return holder.proxy;
				}

				if (this.proxyConfigurationState == 1) {
					try {this.proxyManagerLock.wait();} catch (InterruptedException ex) {}
					if (this.proxyConfigurationState != 2) {
						throw new ActivityCancelledException(SVNMessages.getErrorString("Error_AuthenticationCancelled")); //$NON-NLS-1$
					}
				}
				else if (this.proxyConfigurationState == 0) {
					this.proxyConfigurationState = 1;
				}
			    
				if (cache.size() == 0) {
					retVal = this.newProxyInstance();
					isNew = true;
				}
				else {
					retVal = cache.remove(0);
				}
				
			    this.usedProxies.add(retVal);
			    this.thread2Proxy.put(current, new ProxyHolder(retVal));
			}
			catch (RuntimeException e) {
			    this.proxyConfigurationState = 0;
			    this.proxyManagerLock.notifyAll();
			    throw e;
			}
			catch (Throwable e) {
			    this.proxyConfigurationState = 0;
			    this.proxyManagerLock.notifyAll();
			    throw new RuntimeException(e);
			}
		}
		if (isNew) { // configure a new proxy later in order to avoid recursive deadlocks when there is a misconfiguration of some sort
			SVNUtility.configureProxy(retVal, this);
		}
		this.fireProxyAcquired(retVal);
	    return retVal;
	}
	
	public void releaseSVNProxy(ISVNConnector proxy) {
		this.fireProxyDisposed(proxy);
		synchronized (this.proxyManagerLock) {
		    List<ISVNConnector> proxies = this.getProxyCache();
		    
		    Thread current = Thread.currentThread();
		    ProxyHolder holder = this.thread2Proxy.get(current);
		    
		    if (--holder.referenceCounter > 0) {
		    	return;
		    }
		    
	    	this.thread2Proxy.remove(current);
	    	// Proxy should be always removed from the usedProxies list. So, do it first.
		    if (!this.usedProxies.remove(proxy) || proxies.size() >= SVNRepositoryLocation.PROXY_CACHE_SIZE) {
		    	// The function code is sensitive to exceptions. So, disallow error reporting in that case.
    	        try {proxy.dispose();} catch (Throwable ex) {}
		    }
		    else {
		        proxies.add(proxy);
		    }
		    
		    if (this.proxyConfigurationState == 1) {
			    this.proxyConfigurationState = 2;
		    }
		    this.proxyManagerLock.notifyAll();
		}
	}
	
	public void reconfigure() {
		synchronized (this.proxyManagerLock) {
			this.proxyConfigurationState = 0;
			this.reconfigureImpl();
		}
	}
	
	public void dispose() {
		synchronized (this.proxyManagerLock) {
			this.reconfigureProxies(new IProxyVisitor() {
	            public void visit(ISVNConnector proxy) {
	            	// When exiting Eclipse IDE connector plug-in's can be stopped before Core. So, disallow error reporting in that case. 
	    	        try {proxy.dispose();} catch (Throwable ex) {}
	            }
	        });
		    this.getProxyCache().clear();
		}
	}

	public SSLSettings getSSLSettings() {
		return this.getSSLSettings(true);
	}
	
	public SSLSettings getSSLSettings(boolean isCheckAuthInfo) {
		if (isCheckAuthInfo) {
			this.checkAuthInfo();
		}				
		synchronized (this.lazyInitLock) {
			if (this.sslSettings == null) {
				this.sslSettings = new SSLSettings();
			}
			return this.sslSettings;
		}
	}

	public SSHSettings getSSHSettings(boolean isCheckAuthInfo) {
		if (isCheckAuthInfo) {
			this.checkAuthInfo();
		}				
		synchronized (this.lazyInitLock) {
			if (this.sshSettings == null) {
				this.sshSettings = new SSHSettings(this);
			}
			return this.sshSettings;
		}
	}
	
	public SSHSettings getSSHSettings() {
		return this.getSSHSettings(true);
	}
	
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof IRepositoryLocation) {
			IRepositoryLocation other = (IRepositoryLocation)obj;
			return this.getId().equals(other.getId());
		}
		return false;
	}

    protected static boolean isArgumentsCorrect(IRepositoryLocation location, String url, boolean allowsNull) throws IllegalArgumentException {
    	if (url == null) {
    		throw new IllegalArgumentException(SVNMessages.getErrorString("Error_NullURL")); //$NON-NLS-1$
    	}
        IPath repoPath = SVNUtility.createPathForSVNUrl(location.getUrl());
    	IPath urlPath = SVNUtility.createPathForSVNUrl(url);
    	// do not access repository root URL if it is not required
        if (!repoPath.isPrefixOf(urlPath)) {
            IPath rootPath = SVNUtility.createPathForSVNUrl(location.getRepositoryRootUrl());
        	if (!rootPath.isPrefixOf(urlPath)) {
        		if (!allowsNull) {
            		if (!urlPath.isPrefixOf(rootPath)) {
            			String message = SVNMessages.formatErrorString("Error_NotRelatedURL", new String[] {url, rootPath.toString()}); //$NON-NLS-1$
            			throw new IllegalArgumentException(message);
            		}
            		String message = SVNMessages.formatErrorString("Error_ShorterURL", new String[] {url, rootPath.toString()}); //$NON-NLS-1$
            		throw new UnreportableException(message);
        		}
        		return false;
        	}
        }
    	return true;
    }
    
	protected void fetchRepoInfo() {
		synchronized (this.repositoryRootLock) {
			if (this.repositoryRootUrl == null && this.url != null && SVNUtility.isValidSVNURL(this.getUrl())) {
				String []values = SVNRepositoryLocation.fetchRepoInfo(this, false);
				this.repositoryRootUrl = values[0];
				this.repositoryUUID = values[1];
				if (this.repositoryUUID != null) {
					SVNTeamPlugin.instance().setLocationsDirty(true);
				}
			}
		}
	}
	
	public static String []fetchRepoInfo(final IRepositoryLocation location, final boolean usePrompt) {
		// completely disallow error reporting
		final String []retVal = new String[2];
		ProgressMonitorUtility.doTaskExternal(new AbstractActionOperation("Operation_FetchRepositoryRoot", SVNMessages.class) { //$NON-NLS-1$
			protected void runImpl(IProgressMonitor monitor) throws Exception {
			    ISVNConnector proxy = CoreExtensionsManager.instance().getSVNConnectorFactory().createConnector();
				proxy.setCredentialsCacheEnabled(false);
				SVNUtility.configureProxy(proxy, location);
				
			    if (usePrompt) {
					IOptionProvider optionProvider = SVNTeamPlugin.instance().getOptionProvider();
				    ISVNCredentialsPrompt externalPrompt = optionProvider.getCredentialsPrompt();
				    if (externalPrompt != null) {
						proxy.setPrompt(new BaseCredentialsPromptWrapper(externalPrompt, location));
				    }
			    }
			    else {
					proxy.setPrompt(new BaseCredentialsPromptWrapper(null, location));
			    }
			    
				SVNEntryInfo []infos = null;
				String url = location.getUrl();
				try {
				    infos = SVNUtility.info(proxy, new SVNEntryRevisionReference(SVNUtility.encodeURL(url), SVNRevision.HEAD, SVNRevision.HEAD), SVNDepth.EMPTY, new SVNProgressMonitor(this, monitor, null));
				}
				finally {
					proxy.dispose();
				}
				if (infos != null && infos.length > 0 && infos[0] != null) {
					retVal[0] = SVNUtility.decodeURL(infos[0].reposRootUrl);
					if (!SVNUtility.createPathForSVNUrl(retVal[0]).isPrefixOf(SVNUtility.createPathForSVNUrl(url))) {
						// different host name could be returned by server side
						SVNURLStreamHandler userUrl = SVNUtility.getSVNUrlStreamHandler(url);
						SVNURLStreamHandler returnedURL = SVNUtility.getSVNUrlStreamHandler(retVal[0]);
						returnedURL.setHost(userUrl.getURL().getHost());
						retVal[0] = returnedURL.getURL().toExternalForm();
					}
					retVal[1] = infos[0].reposUUID;
				}
			}
		}, 
		new NullProgressMonitor(), 
		new ILoggedOperationFactory() {
			public IActionOperation getLogged(IActionOperation operation) {
				return operation;
			}
		});
		return retVal;
	}
	
	protected void reconfigureImpl() {
		this.reconfigureProxies(new IProxyVisitor() {
			public void visit(ISVNConnector proxy) {
				SVNUtility.configureProxy(proxy, SVNRepositoryLocation.this);
			}
		});
	}
	
	protected void reconfigureProxies(IProxyVisitor visitor) {
	    this.visitProxies(visitor);
	    this.usedProxies.clear();
	}
	
	protected void visitProxies(IProxyVisitor visitor) {
	    for (ISVNConnector proxy : this.getProxyCache()) {
		    visitor.visit(proxy);
	    }
	}
	
	protected List<ISVNConnector> getProxyCache() {
	    if (this.proxyCache == null) {
	        this.proxyCache = new ArrayList<ISVNConnector>();
	        this.usedProxies = new HashSet<ISVNConnector>();
	        this.thread2Proxy = new HashMap<Thread, ProxyHolder>();
	    }
	    return this.proxyCache;
	}
	
	protected ISVNConnector newProxyInstance() {
		IOptionProvider optionProvider = SVNTeamPlugin.instance().getOptionProvider();
	    ISVNConnector proxy = CoreExtensionsManager.instance().getSVNConnectorFactory().createConnector();
	    
		proxy.setCredentialsCacheEnabled(false);
		proxy.setSSLCertificateCacheEnabled(true);
		proxy.setCommitMissingFiles(true);
		
	    ISVNCredentialsPrompt externalPrompt = optionProvider.getCredentialsPrompt();
	    if (externalPrompt != null) {
			proxy.setPrompt(new CredentialsPromptWrapper(externalPrompt));
	    }
	    
		return proxy;
	}
	
	protected List<byte[]> getSerializedRevisionLinks() {
		if (this.serializedRevisionLinks == null) {
			this.serializedRevisionLinks = new ArrayList<byte[]>();
		}
		return this.serializedRevisionLinks;
	}
	
	protected String getUrlImpl(String url) {
		if (url == null) {
			return null;
		}
		try {
			url = SVNUtility.decodeURL(url);
		}
		catch (IllegalArgumentException ex) {
			// the URL is not encoded
		}
	    return SVNUtility.normalizeURL(url);
	}

	protected Map<String, IRepositoryLocation> getAdditionalRealms() {
		return this.getAdditionalRealms(true);
	}
	
	protected Map<String, IRepositoryLocation> getAdditionalRealms(boolean isCheckAuthInfo) {
		if (isCheckAuthInfo) {
			this.checkAuthInfo();	
		}				
		synchronized (this) {
			if (this.additionalRealms == null) {
				this.additionalRealms = new LinkedHashMap<String, IRepositoryLocation>();
			}	
		}
		return this.additionalRealms;
	}
	
	protected void fireChanged(String field, Object oldValue, Object newValue) {
		for (IRepositoryLocationStateListener listener : this.getStateListeners()) {
			listener.changed(this, field, oldValue, newValue);
		}
	}
	
	public void sslChanged(IRepositoryLocation where, String field, Object oldValue, Object newValue) {
		for (IRepositoryLocationStateListener listener : this.getStateListeners()) {
			listener.sslChanged(this, field, oldValue, newValue);
		}
	}

	public void sshChanged(IRepositoryLocation where, String field, Object oldValue, Object newValue) {
		for (IRepositoryLocationStateListener listener : this.getStateListeners()) {
			listener.sshChanged(this, field, oldValue, newValue);
		}
	}

	protected void fireRealmAdded(String realm, IRepositoryLocation location) {
		for (IRepositoryLocationStateListener listener : this.getStateListeners()) {
			listener.realmAdded(this, realm, location);
		}
	}
	
	protected void fireRealmRemoved(String realm) {
		for (IRepositoryLocationStateListener listener : this.getStateListeners()) {
			listener.realmRemoved(this, realm);
		}
	}
	
	protected void fireRevisionLinkAdded(IRevisionLink link) {
		for (IRepositoryLocationStateListener listener : this.getStateListeners()) {
			listener.revisionLinkAdded(this, link);
		}
	}
	
	protected void fireRevisionLinkRemoved(IRevisionLink link) {
		for (IRepositoryLocationStateListener listener : this.getStateListeners()) {
			listener.revisionLinkRemoved(this, link);
		}
	}
	
	protected void fireProxyAcquired(ISVNConnector proxy) {
		for (IRepositoryLocationStateListener listener : this.getStateListeners()) {
			listener.proxyAcquired(this, proxy);
		}
	}
	
	protected void fireProxyDisposed(ISVNConnector proxy) {
		for (IRepositoryLocationStateListener listener : this.getStateListeners()) {
			listener.proxyDisposed(this, proxy);
		}
	}
	
	protected IRepositoryLocationStateListener []getStateListeners() {
		synchronized (this.changeListeners) {
			return this.changeListeners.toArray(new IRepositoryLocationStateListener[this.changeListeners.size()]);
		}
	}

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.lazyInitLock = new Integer(0);
        this.proxyManagerLock = new Integer(0);
        this.repositoryRootLock = new Integer(0);
    }
    
	protected interface IProxyVisitor {
	    public void visit(ISVNConnector proxy);
	}

	public static class BaseCredentialsPromptWrapper implements ISVNCredentialsPrompt {
		protected ISVNCredentialsPrompt prompt;
		protected String tryRealm;
		protected String threadName;
		
		protected IRepositoryLocation realmLocation;
		protected IRepositoryLocation location;
		
		public BaseCredentialsPromptWrapper(ISVNCredentialsPrompt prompt, IRepositoryLocation location) {
			this.prompt = prompt == null ? ISVNCredentialsPrompt.DEFAULT_PROMPT : prompt;
			this.location = location;
		}

		public int askTrustSSLServer(Object location, String info, boolean allowPermanently) {
        	return this.prompt.askTrustSSLServer(this.location, info, allowPermanently);
		}

		public String getRealmToSave() {
			return this.prompt.getRealmToSave();
		}
		
		public String getProxyHost() {
			return this.prompt.getProxyHost();
		}

		public String getProxyPassword() {
			return this.prompt.getProxyPassword();
		}

		public int getProxyPort() {
			return this.prompt.getProxyPort();
		}

		public String getProxyUserName() {
			return this.prompt.getProxyUserName();
		}

		public int getSSHPort() {
			return this.realmLocation != null ? this.realmLocation.getSSHSettings().getPort() : this.prompt.getSSHPort();
		}

		public String getSSHPrivateKeyPassphrase() {
			return this.realmLocation != null ? this.realmLocation.getSSHSettings().getPassPhrase() : this.prompt.getSSHPrivateKeyPassphrase();
		}

		public String getSSHPrivateKeyPath() {
			return this.realmLocation != null ? this.realmLocation.getSSHSettings().getPrivateKeyPath() : this.prompt.getSSHPrivateKeyPath();
		}

		public String getSSLClientCertPassword() {
			return this.realmLocation != null ? this.realmLocation.getSSLSettings().getPassPhrase() : this.prompt.getSSLClientCertPassword();
		}

		public String getSSLClientCertPath() {
			return this.realmLocation != null ? this.realmLocation.getSSLSettings().getCertificatePath() : this.prompt.getSSLClientCertPath();
		}

		public String getUsername() {
			return this.realmLocation != null ? this.realmLocation.getUsername() : this.prompt.getUsername();
		}

		public String getPassword() {
			return this.realmLocation != null ? this.realmLocation.getPassword() : this.prompt.getPassword();
		}

		public boolean isProxyAuthenticationEnabled() {
			return this.prompt.isProxyAuthenticationEnabled();
		}

		public boolean isProxyEnabled() {
			return this.prompt.isProxyEnabled();
		}

		public boolean isSSHPrivateKeyPassphraseSaved() {
			return this.prompt.isSSHPrivateKeyPassphraseSaved();
		}

		public boolean isSSHPublicKeySelected() {
			return this.prompt.isSSHPublicKeySelected();
		}

		public boolean isSSLAuthenticationEnabled() {
			return this.prompt.isSSLAuthenticationEnabled();
		}

		public boolean isSSLSavePassphrase() {
			return this.prompt.isSSLSavePassphrase();
		}

		public boolean isSaveCredentialsEnabled() {
			return this.prompt.isSaveCredentialsEnabled();
		}

		public boolean isSaveProxyPassword() {
			return this.prompt.isSaveProxyPassword();
		}

		public boolean promptProxy(Object context) {
			boolean retVal = this.prompt.promptProxy(this.location);
			this.checkForSave(retVal, SVNRepositoryLocation.PROXY_CONNECTION);
			return retVal;
		}

		public boolean prompt(Object context, String realm) {
			if (this.tryCachedRealm(realm)) {
				return true;
			}
			boolean retVal = this.prompt.prompt(this.location, realm);
			this.checkForSave(retVal, SVNRepositoryLocation.DEFAULT_CONNECTION);
			return retVal;
		}

		public boolean promptSSH(Object context, String realm) {
			if (this.tryCachedRealm(realm)) {
				return true;
			}
			boolean retVal = this.prompt.promptSSH(this.location, realm);
			this.checkForSave(retVal, SVNRepositoryLocation.SSH_CONNECTION);
			return retVal;
		}

		public boolean promptSSL(Object context, String realm) {
			if (this.tryCachedRealm(realm)) {
				return true;
			}
			boolean retVal = this.prompt.promptSSL(this.location, realm);
			this.checkForSave(retVal, SVNRepositoryLocation.SSL_CONNECTION);
			return retVal;
		}
		
		protected boolean tryCachedRealm(String realm) {
			/*
			 * There shouldn't be any checks on emptiness of the basic authentication password, since there might be other than basic authentication access method used (SSH key-file, for example).
			 */
			String threadName = Thread.currentThread().getName();
			if (this.tryRealm == null || !this.tryRealm.equals(realm) || !threadName.equals(this.threadName)) {
				this.realmLocation = this.location.getLocationForRealm(realm);
				if (this.realmLocation != null && this.realmLocation.getUsername() != null && !this.realmLocation.getUsername().equals("")) {
					this.tryRealm = realm;
					this.threadName = threadName;
					return true;
				}
				if (this.realmLocation == null && CoreExtensionsManager.instance().getSVNConnectorFactory().getId().indexOf("svnkit") != -1) {
					String protocol = "file"; //$NON-NLS-1$
					try {
						URL url = SVNUtility.getSVNUrl(location.getUrl());
						protocol = url.getProtocol();
						if (protocol.equals("file")) { //$NON-NLS-1$
							this.realmLocation = this.location;
							this.tryRealm = realm;
							this.threadName = threadName;
							return true;
						}
					}
					catch (MalformedURLException ex) {
						//skip
					}
				}
			}
			else {
				this.tryRealm = null;
				this.realmLocation = null;
				this.threadName = null;
			}
			return false;
		}
		
        protected void checkForSave(boolean retVal, int connectionType) {
            if (retVal) {
        		IRepositoryLocation location = this.location;
        		String realmToSave = this.getRealmToSave();
        		if (!ISVNCredentialsPrompt.ROOT_LOCATION.equals(realmToSave)) {
        			location = this.location.getLocationForRealm(realmToSave);
        			if (location == null) {
            			location = SVNRemoteStorage.instance().newRepositoryLocation();
            			SVNRemoteStorage.instance().copyRepositoryLocation(location, this.location);
            			this.location.addRealm(realmToSave, location);
        			}
        		}
            	this.checkForSaveImpl(location, retVal, connectionType);
            }
        }
        
        protected void checkForSaveImpl(IRepositoryLocation location, boolean retVal, int connectionType) {
    		location.setUsername(this.prompt.getUsername());
    		location.setPassword(this.prompt.getPassword());
    		location.setPasswordSaved(this.prompt.isSaveCredentialsEnabled());
    		SVNTeamPlugin.instance().setLocationsDirty(true);
        
    		if (connectionType == SVNRepositoryLocation.SSH_CONNECTION) {
    			SSHSettings settings = location.getSSHSettings();
    			settings.setPort(this.prompt.getSSHPort());
    			settings.setUseKeyFile(this.prompt.isSSHPublicKeySelected());
    			if (settings.isUseKeyFile()) {
    				settings.setPrivateKeyPath(this.prompt.getSSHPrivateKeyPath());
    				settings.setPassPhraseSaved(this.prompt.isSSHPrivateKeyPassphraseSaved());
    				settings.setPassPhrase(this.getSSHPrivateKeyPassphrase());
    			}
    		}
    		if (connectionType == SVNRepositoryLocation.SSL_CONNECTION) {
    			SSLSettings settings = location.getSSLSettings();
    			settings.setAuthenticationEnabled(this.prompt.isSSLAuthenticationEnabled());
    			if (settings.isAuthenticationEnabled()) {
    				settings.setCertificatePath(this.prompt.getSSLClientCertPath());
    				settings.setPassPhrase(this.prompt.getSSLClientCertPassword());
    				settings.setPassPhraseSaved(this.prompt.isSSLSavePassphrase());
    			}
    		}
        }
        
	}
	
	protected class CredentialsPromptWrapper extends BaseCredentialsPromptWrapper {
		public CredentialsPromptWrapper(ISVNCredentialsPrompt prompt) {
			super(prompt, SVNRepositoryLocation.this);
		}

		public int askTrustSSLServer(IRepositoryLocation location, String info, boolean allowPermanently) {
        	if (!SVNRepositoryLocation.this.trustSiteDefined) {
        		SVNRepositoryLocation.this.trustSite = super.askTrustSSLServer(SVNRepositoryLocation.this, info, allowPermanently);
        		if (SVNRepositoryLocation.this.trustSite != ISVNCredentialsPrompt.REJECT) {
            		SVNRepositoryLocation.this.trustSiteDefined = true;
        		}
        		else {
        			SVNRepositoryLocation.this.proxyConfigurationState = 0;
        		}
            	return SVNRepositoryLocation.this.trustSite;
        	}
        	return SVNRepositoryLocation.this.trustSite;
		}
		
        protected void checkForSave(boolean retVal, int connectionType) {
        	synchronized (SVNRepositoryLocation.this.proxyManagerLock) {
                if (retVal) {
                	super.checkForSave(retVal, connectionType);
                }
        		else {
        			SVNRepositoryLocation.this.proxyConfigurationState = 0;
        		}
                SVNRepositoryLocation.this.reconfigureImpl();
        	}
        }
        
	}
	
	protected static class ProxyHolder {
		public final ISVNConnector proxy;
		public int referenceCounter;
		
		public ProxyHolder(ISVNConnector proxy) {
			this.proxy = proxy;
			this.referenceCounter = 1;
		}
	}

	public boolean isPasswordSavedForRealm(String realm) {
		IRepositoryLocation locationForRealm = this.getAdditionalRealms().get(realm);
		if (locationForRealm != null && locationForRealm.isPasswordSaved()) {
			return true;
		}
		return false;
	}
	
	protected void setRetrieveAuthInfo(boolean isRetrieveAuthInfo) {
		synchronized (this.authInitLock) {
			this.isRetrieveAuthInfo = isRetrieveAuthInfo;	
		}		
	}
	
	private void checkAuthInfo() {		
		synchronized (this.authInitLock) {
			if (this.isRetrieveAuthInfo) {																
				try {
					SVNRemoteStorage.instance().loadAuthInfo(this, ""); //$NON-NLS-1$
					for (String realm : this.rawRealms) {
						SVNRemoteStorage.instance().loadAuthInfo(this, realm);
					}
				} catch (Exception ex) {
					LoggedOperation.reportError("fillLocationFromReference", ex); //$NON-NLS-1$
				} finally {
					this.isRetrieveAuthInfo = false;
				}											
			}	
		}			
	}

}
