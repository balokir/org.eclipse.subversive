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

package org.eclipse.team.svn.core.svnstorage;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.preferences.Base64;
import org.eclipse.core.internal.utils.UniversalUniqueIdentifier;
import org.eclipse.core.net.proxy.IProxyChangeEvent;
import org.eclipse.core.net.proxy.IProxyChangeListener;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.resource.ISVNStorage;
import org.eclipse.team.svn.core.resource.SSHSettings;
import org.eclipse.team.svn.core.resource.SSLSettings;
import org.eclipse.team.svn.core.svnstorage.events.IRepositoriesStateChangedListener;
import org.eclipse.team.svn.core.svnstorage.events.IRevisionPropertyChangeListener;
import org.eclipse.team.svn.core.svnstorage.events.RepositoriesStateChangedEvent;
import org.eclipse.team.svn.core.svnstorage.events.RevisonPropertyChangeEvent;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Basic IRemoteStorage implementation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractSVNStorage implements ISVNStorage {
	
	private static final String URL_TO_STORE = "http://eclipse.org/subversive/";  //$NON-NLS-1$
	
	protected File stateInfoFile;
	protected String preferencesNode;
	protected IPreferenceChangeListener repoPrefChangeListener;
	protected SVNCachedProxyCredentialsManager proxyCredentialsManager;
	
	protected IRepositoryLocation []repositories;
	protected List<IRepositoriesStateChangedListener> repositoriesStateChangedListeners;
	protected ArrayList<IRevisionPropertyChangeListener> revPropChangeListeners;
	
	protected class RepositoryPreferenceChangeListener implements IPreferenceChangeListener {
		public void preferenceChange(PreferenceChangeEvent event) {
			HashSet<IRepositoryLocation> readLocations = new HashSet<IRepositoryLocation>(Arrays.asList(AbstractSVNStorage.this.repositories));
			IRepositoryLocation location = AbstractSVNStorage.this.newRepositoryLocation((String)event.getNewValue());
			readLocations.add(location);
			AbstractSVNStorage.this.repositories = readLocations.toArray(new IRepositoryLocation[readLocations.size()]);
			try {
				((IEclipsePreferences)event.getSource()).flush();
			}
			catch (BackingStoreException e) {
				LoggedOperation.reportError("preferenceChange", e);
			}
			AbstractSVNStorage.this.fireRepositoriesStateChanged(new RepositoriesStateChangedEvent(location, RepositoriesStateChangedEvent.ADDED));
		}
	}
	
	public AbstractSVNStorage() {
		this.repositories = new IRepositoryLocation[0];
		this.repositoriesStateChangedListeners = new ArrayList<IRepositoriesStateChangedListener>();
		this.revPropChangeListeners = new ArrayList<IRevisionPropertyChangeListener>();
	}
	
	public void dispose() {
		// synchronization is innecessary
		IRepositoryLocation []locations = this.repositories;
		if (locations != null) {
		    for (int i = 0; i < locations.length; i++) {
		    	locations[i].dispose();
		    }
		}
	}
	
	public void reconfigureLocations() {
		// synchronization is innecessary
		IRepositoryLocation []locations = this.repositories;
		if (locations != null) {
		    for (int i = 0; i < locations.length; i++) {
		    	locations[i].reconfigure();
		    }
		}
	}
	
	public void addRepositoriesStateChangedListener(IRepositoriesStateChangedListener listener) {
		synchronized(this.repositoriesStateChangedListeners) {
			this.repositoriesStateChangedListeners.add(listener);
		}
	}
	
	public void removeRepositoriesStateChangedListener(IRepositoriesStateChangedListener listener) {
		synchronized(this.repositoriesStateChangedListeners) {
			this.repositoriesStateChangedListeners.remove(listener);
		}
	}

	public void fireRepositoriesStateChanged(RepositoriesStateChangedEvent event) {
		synchronized (this.repositoriesStateChangedListeners) {
			for (IRepositoriesStateChangedListener listener : this.repositoriesStateChangedListeners) {
				listener.repositoriesStateChanged(event);
			}
		}
	}
	
	public void addRevisionPropertyChangeListener(IRevisionPropertyChangeListener listener) {
		synchronized(this.revPropChangeListeners) {
			this.revPropChangeListeners.add(listener);
		}
	}

	public void fireRevisionPropertyChangeEvent(RevisonPropertyChangeEvent event) {
		synchronized(this.revPropChangeListeners) {
			for (IRevisionPropertyChangeListener current : this.revPropChangeListeners) {
				current.revisionPropertyChanged(event);
			}
		}
	}

	public void removeRevisionPropertyChangeListener(IRevisionPropertyChangeListener listener) {
		synchronized(this.revPropChangeListeners) {
			this.revPropChangeListeners.remove(listener);
		}
	}
	
	/**
	 * Return the preferences node whose child nodes are the repositories,
	 * stored in preferences.
	 * 
	 * @return preferences node
	 */
	public static IEclipsePreferences getRepositoriesPreferences(String prefNode) {
		return (IEclipsePreferences)SVNTeamPlugin.instance().getSVNCorePreferences().node(prefNode);
	}
	
	public IRepositoryLocation []getRepositoryLocations() {
		return this.repositories;
	}
	
	public IRepositoryLocation getRepositoryLocation(String id) {
		for (int i = 0; i < this.repositories.length; i++) {
			if (this.repositories[i].getId().equals(id)) {
				return this.repositories[i];
			}
		}
		return null;
	}

	public IRepositoryLocation newRepositoryLocation() {
		return new SVNRepositoryLocation(new UniversalUniqueIdentifier().toString());
	}
	
	public void copyRepositoryLocation(IRepositoryLocation to, IRepositoryLocation from) {
		to.setStructureEnabled(from.isStructureEnabled());
		to.setBranchesLocation(from.getUserInputBranches());
		to.setTagsLocation(from.getUserInputTags());
		to.setTrunkLocation(from.getUserInputTrunk());
		
		to.setUrl(from.getUrlAsIs());
		to.setLabel(from.getLabel());
		
		to.setUsername(from.getUsername());
		to.setPassword(from.getPassword());
		to.setPasswordSaved(from.isPasswordSaved());
		
		SSHSettings sshOriginal = from.getSSHSettings();
		SSHSettings sshNew = to.getSSHSettings();
		sshNew.setPassPhrase(sshOriginal.getPassPhrase());
		sshNew.setPassPhraseSaved(sshOriginal.isPassPhraseSaved());
		sshNew.setPort(sshOriginal.getPort());
		sshNew.setPrivateKeyPath(sshOriginal.getPrivateKeyPath());
		sshNew.setUseKeyFile(sshOriginal.isUseKeyFile());
		
		SSLSettings sslOriginal = from.getSSLSettings();
		SSLSettings sslNew = to.getSSLSettings();
		sslNew.setAuthenticationEnabled(sslOriginal.isAuthenticationEnabled());
		sslNew.setCertificatePath(sslOriginal.getCertificatePath());
		sslNew.setPassPhrase(sslOriginal.getPassPhrase());
		sslNew.setPassPhraseSaved(sslOriginal.isPassPhraseSaved());			
			
		if (from instanceof SVNRepositoryLocation && to instanceof SVNRepositoryLocation) {
			SVNRepositoryLocation tmpFrom = (SVNRepositoryLocation)from;
			SVNRepositoryLocation tmpTo = (SVNRepositoryLocation)to;
			tmpTo.repositoryRootUrl = tmpFrom.repositoryRootUrl;
			tmpTo.repositoryUUID = tmpFrom.repositoryUUID;
			
			tmpTo.getAdditionalRealms().clear();
			for (Iterator<String> it = from.getRealms().iterator(); it.hasNext(); ) {
				String realm = it.next();
				IRepositoryLocation target = this.newRepositoryLocation();
				IRepositoryLocation source = from.getLocationForRealm(realm);
				this.copyRepositoryLocation(target, source);
				to.addRealm(realm, target);
			}
		}
	}

	public IRepositoryLocation newRepositoryLocation(String reference) {
		if (reference == null) {
			return this.newRepositoryLocation();
		}
		String []parts = reference.split(";"); //$NON-NLS-1$
		if (parts.length == 0 || parts[0].length() == 0) {
			return this.newRepositoryLocation();
		}
		IRepositoryLocation location = this.getRepositoryLocation(parts[0]);
		if (location != null) {
			return location;
		}
		String id = parts[0].trim();
		location = new SVNRepositoryLocation(id.length() > 0 ? id : new UniversalUniqueIdentifier().toString());
		location.setTrunkLocation(""); //$NON-NLS-1$
		location.setTagsLocation(""); //$NON-NLS-1$
		location.setBranchesLocation(""); //$NON-NLS-1$
		location.setAuthorName(""); //$NON-NLS-1$
		location.fillLocationFromReference(parts);
		return location;
	}
	
	public String repositoryLocationAsReference(IRepositoryLocation location) {
		return location.asReference();
	}
	
	public synchronized void addRepositoryLocation(IRepositoryLocation location) {
		List<IRepositoryLocation> tmp = new ArrayList<IRepositoryLocation>(Arrays.asList(this.repositories));
		if (!tmp.contains(location)) {
			tmp.add(location);
			this.repositories = tmp.toArray(new IRepositoryLocation[tmp.size()]);
		}
		this.fireRepositoriesStateChanged(new RepositoriesStateChangedEvent(location, RepositoriesStateChangedEvent.ADDED));
	}
	
	public synchronized void removeRepositoryLocation(IRepositoryLocation location) {
		List<IRepositoryLocation> tmp = new ArrayList<IRepositoryLocation>(Arrays.asList(this.repositories));
		this.removeAuthInfoForLocation(location, ""); //$NON-NLS-1$
		String [] realms = location.getRealms().toArray(new String[0]);
		for (String realm : realms) {
			this.removeAuthInfoForLocation(location, realm);
		}
		if (tmp.remove(location)) {
			this.repositories = tmp.toArray(new IRepositoryLocation[tmp.size()]);
		}
		this.fireRepositoriesStateChanged(new RepositoriesStateChangedEvent(location, RepositoriesStateChangedEvent.REMOVED));
	}
	
	public SVNCachedProxyCredentialsManager getProxyCredentialsManager() {
		return this.proxyCredentialsManager;
	}

	public synchronized void saveConfiguration() throws Exception {
		this.saveLocations();
	}
	
	public byte []repositoryResourceAsBytes(IRepositoryResource resource) {
		if (resource == null) {
			return null;
		}
		int selectedKind = resource.getSelectedRevision().getKind();
		int pegKind = resource.getPegRevision().getKind();
		String retVal = 
			new String(Base64.encode(String.valueOf(resource instanceof IRepositoryContainer).getBytes())) + ";" +  //$NON-NLS-1$
			resource.getRepositoryLocation().getId() + ";" + //$NON-NLS-1$
			new String(Base64.encode(resource.getUrl().getBytes())) + ";" + //$NON-NLS-1$
			String.valueOf(selectedKind) + ";" +  //$NON-NLS-1$
			(selectedKind == Kind.NUMBER ? String.valueOf(((SVNRevision.Number)resource.getSelectedRevision()).getNumber()) : "0") + ";" + //$NON-NLS-1$ //$NON-NLS-2$
			String.valueOf(IRepositoryRoot.KIND_ROOT) + ";" +  //$NON-NLS-1$
			String.valueOf(pegKind) + ";" +  //$NON-NLS-1$
			(pegKind == Kind.NUMBER ? String.valueOf(((SVNRevision.Number)resource.getPegRevision()).getNumber()) : "0"); //$NON-NLS-1$
		return retVal.getBytes();
	}
	
	public IRepositoryResource repositoryResourceFromBytes(byte []bytes) {
		return this.repositoryResourceFromBytes(bytes, null);
	}
	
	public IRepositoryResource repositoryResourceFromBytes(byte []bytes, IRepositoryLocation location) {
		if (bytes == null) {
			return null;
		}
		String []data = new String(bytes).split(";"); //$NON-NLS-1$
		boolean isFolder = false;
		boolean base64Label = false;
		if ("true".equals(data[0])) { //$NON-NLS-1$
			isFolder = true;
		}
		else if (!"false".equals(data[0])) { //$NON-NLS-1$
			isFolder = "true".equals(new String(Base64.decode(data[0].getBytes()))); //$NON-NLS-1$
			base64Label = true;
		}
		if (location == null) {
			location = this.getRepositoryLocation(data[1]);
		}
		if (location == null) {
		    return null;
		}
		int revisionKind = Integer.parseInt(data[3]);
		long revNum = Long.parseLong(data[4]);
		SVNRevision selectedRevision = revisionKind == Kind.NUMBER ? (revNum == SVNRevision.INVALID_REVISION_NUMBER ? SVNRevision.INVALID_REVISION : (SVNRevision)SVNRevision.fromNumber(revNum)) : SVNRevision.fromKind(revisionKind);
		SVNRevision pegRevision = null;
		if (data.length > 6) {
			int pegKind = Integer.parseInt(data[6]);
			long pegNum = Long.parseLong(data[7]);
			pegRevision = pegKind == Kind.NUMBER ? (pegNum == SVNRevision.INVALID_REVISION_NUMBER ? null : (SVNRevision)SVNRevision.fromNumber(pegNum)) : SVNRevision.fromKind(pegKind);
		}
		
		String urlPart = base64Label ? new String(Base64.decode(data[2].getBytes())) : data[2];
		try {
			SVNUtility.getSVNUrl(urlPart);
		} 
		catch (MalformedURLException e) {
			// old-style partial url
			String prefix = AbstractSVNStorage.getRootPrefix(location, Integer.parseInt(data[5]));
			urlPart = prefix + urlPart;
		}
		
		location = this.wrapLocationIfRequired(location, urlPart, !isFolder);
		
		IRepositoryResource retVal = isFolder ? (IRepositoryResource)location.asRepositoryContainer(urlPart, false) : location.asRepositoryFile(urlPart, false);
		retVal.setSelectedRevision(selectedRevision);
		retVal.setPegRevision(pegRevision);
		return retVal;
	}
	
	protected abstract IRepositoryLocation wrapLocationIfRequired(IRepositoryLocation location, String url, boolean isFile);
	
	protected static String getRootPrefix(IRepositoryLocation location, int rootKind) {
		switch (rootKind) {
			case IRepositoryRoot.KIND_ROOT: {
				return location.getRepositoryRootUrl();
			}
			case IRepositoryRoot.KIND_LOCATION_ROOT: {
				return location.getUrl();
			}
			case IRepositoryRoot.KIND_TRUNK: {
				return location.isStructureEnabled() ? (location.getUrl() + "/" + location.getTrunkLocation()) : location.getUrl(); //$NON-NLS-1$
			}
			case IRepositoryRoot.KIND_BRANCHES: {
				return location.isStructureEnabled() ? (location.getUrl() + "/" + location.getBranchesLocation()) : location.getUrl(); //$NON-NLS-1$
			}
			case IRepositoryRoot.KIND_TAGS: {
				return location.isStructureEnabled() ? (location.getUrl() + "/" + location.getTagsLocation()) : location.getUrl(); //$NON-NLS-1$
			}
		}
		return null;
	}
    
	protected void initializeImpl(String preferencesNode) throws Exception {
		IProxyService proxyService = SVNTeamPlugin.instance().getProxyService();
		this.proxyCredentialsManager = new SVNCachedProxyCredentialsManager(proxyService);
		
		proxyService.addProxyChangeListener(new IProxyChangeListener() {
			public void proxyInfoChanged(IProxyChangeEvent event) {
				IProxyData [] newDatas = event.getChangedProxyData();
				for (IProxyData current : newDatas) {
					if (current.isRequiresAuthentication()){
						AbstractSVNStorage.this.proxyCredentialsManager.setPassword(current.getPassword());
						AbstractSVNStorage.this.proxyCredentialsManager.setUsername(current.getUserId());
						break;
					}
				}
				AbstractSVNStorage.this.dispose();
			}
		});
		
		this.preferencesNode = preferencesNode;
		this.repoPrefChangeListener = new RepositoryPreferenceChangeListener();
		IEclipsePreferences repositoryPreferences = AbstractSVNStorage.getRepositoriesPreferences(this.preferencesNode);
		repositoryPreferences.addPreferenceChangeListener(this.repoPrefChangeListener);
		// if the file exists, we should convert the data and delete the file.
		if (this.stateInfoFile.exists()) {
			this.loadLocationsFromFile();
			this.stateInfoFile.delete();
			this.saveLocations();
			return;
		}
		try {
			this.loadLocations();
		}
		catch (Exception ex) {
			LoggedOperation.reportError(SVNMessages.getErrorString("Error_LoadLocations"), ex); //$NON-NLS-1$
		}
	}

	protected void saveLocations() throws Exception {
		IEclipsePreferences repositoryPreferences = AbstractSVNStorage.getRepositoriesPreferences(this.preferencesNode);
		repositoryPreferences.removePreferenceChangeListener(this.repoPrefChangeListener);
		repositoryPreferences.clear();
		for (IRepositoryLocation current : this.repositories) {
			repositoryPreferences.put(current.getId(), this.repositoryLocationAsReference(current));
			this.saveAuthInfo(current, ""); //$NON-NLS-1$
			String [] realms = current.getRealms().toArray(new String[0]);
			for (String realm : realms) {
				if (current.isPasswordSavedForRealm(realm)) {
					this.saveAuthInfo(current, realm);
				}
			}
		}
		repositoryPreferences.flush();
		SVNTeamPlugin.instance().savePluginPreferences();
		repositoryPreferences.addPreferenceChangeListener(this.repoPrefChangeListener);
	}
	
	protected void saveAuthInfo(IRepositoryLocation location, String realm) throws Exception {
		Platform.flushAuthorizationInfo(new URL(AbstractSVNStorage.URL_TO_STORE), location.getId(), realm);
		IRepositoryLocation tmp = realm.equals("") ? location : location.getLocationForRealm(realm); //$NON-NLS-1$
		boolean toStorePass = tmp.isPasswordSaved();
		HashMap<String, String> authInfo = new HashMap<String, String>();

		//store normal password settings
		authInfo.put("username", tmp.getUsername()); //$NON-NLS-1$
		authInfo.put("password", toStorePass ? tmp.getPassword() : ""); //$NON-NLS-1$ //$NON-NLS-2$
		authInfo.put("password_saved", String.valueOf(toStorePass)); //$NON-NLS-1$
		
		//store SSH settings
		SSHSettings sshSettings = tmp.getSSHSettings();
		boolean useKeyFile = sshSettings.isUseKeyFile();
		authInfo.put("ssh_use_key", String.valueOf(useKeyFile)); //$NON-NLS-1$
		boolean savePassphrase = sshSettings.isPassPhraseSaved();
		authInfo.put("ssh_passphrase_saved", useKeyFile ? String.valueOf(savePassphrase) : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		authInfo.put("ssh_key", useKeyFile ? sshSettings.getPrivateKeyPath() : ""); //$NON-NLS-1$ //$NON-NLS-2$
		authInfo.put("ssh_passprase", (useKeyFile && savePassphrase) ? sshSettings.getPassPhrase() : ""); //$NON-NLS-1$ //$NON-NLS-2$
		
		//store SSL settings
		SSLSettings sslSettings = tmp.getSSLSettings();
		boolean clientAuthEnabled = sslSettings.isAuthenticationEnabled();
		savePassphrase = sslSettings.isPassPhraseSaved();
		authInfo.put("ssl_enabled", String.valueOf(clientAuthEnabled)); //$NON-NLS-1$
		authInfo.put("ssl_certificate", clientAuthEnabled ? sslSettings.getCertificatePath() : ""); //$NON-NLS-1$ //$NON-NLS-2$
		authInfo.put("ssl_passphrase_saved", clientAuthEnabled ? String.valueOf(savePassphrase) : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		authInfo.put("ssl_passphrase", (clientAuthEnabled && savePassphrase) ? sslSettings.getPassPhrase() : ""); //$NON-NLS-1$ //$NON-NLS-2$
		
		//store the map in platform
		Platform.addAuthorizationInfo(new URL(AbstractSVNStorage.URL_TO_STORE), location.getId(), realm, authInfo);
	}
	
	protected void loadAuthInfo(IRepositoryLocation location, String realm) throws Exception {
		Map<String, String> authInfo = Platform.getAuthorizationInfo(new URL(AbstractSVNStorage.URL_TO_STORE), location.getId(), realm);
		if (authInfo != null) {
			IRepositoryLocation tmp;
			boolean toAddRealm = !realm.equals("");  //$NON-NLS-1$
			if (toAddRealm) {
				tmp = this.newRepositoryLocation();
			}
			else {
				tmp = location;
			}
			
			//recover normal password settings
			tmp.setUsername(authInfo.get("username")); //$NON-NLS-1$
			tmp.setPasswordSaved(authInfo.get("password_saved").equals("true")); //$NON-NLS-1$ //$NON-NLS-2$
			tmp.setPassword(authInfo.get("password")); //$NON-NLS-1$
			
			//recover SSH settings
			SSHSettings sshSettings = tmp.getSSHSettings();
			sshSettings.setUseKeyFile(authInfo.get("ssh_use_key").equals("true")); //$NON-NLS-1$ //$NON-NLS-2$
			sshSettings.setPrivateKeyPath(authInfo.get("ssh_key")); //$NON-NLS-1$
			sshSettings.setPassPhraseSaved(authInfo.get("ssh_passphrase_saved").equals("true")); //$NON-NLS-1$ //$NON-NLS-2$
			sshSettings.setPassPhrase(authInfo.get("ssh_passprase")); //$NON-NLS-1$
			
			//recover SSL settings
			SSLSettings sslSettings = tmp.getSSLSettings();
			sslSettings.setAuthenticationEnabled(authInfo.get("ssl_enabled").equals("true")); //$NON-NLS-1$ //$NON-NLS-2$
			sslSettings.setCertificatePath(authInfo.get("ssl_certificate")); //$NON-NLS-1$
			sslSettings.setPassPhraseSaved(authInfo.get("ssl_passphrase_saved").equals("true")); //$NON-NLS-1$ //$NON-NLS-2$
			sslSettings.setPassPhrase(authInfo.get("ssl_passphrase")); //$NON-NLS-1$
			
			//if realm, add it to realms
			if (toAddRealm) {
				location.addRealm(realm, tmp);
			}
		}
	}
	
	public void removeAuthInfoForLocation(IRepositoryLocation location, String realm) {
		try {
			Platform.flushAuthorizationInfo(new URL(AbstractSVNStorage.URL_TO_STORE), location.getId(), realm);
		}
		catch (Exception ex) {
			LoggedOperation.reportError("Remove Authorization Info Operation", ex);
		}
	}
	
	protected void loadLocations() throws Exception {
		IEclipsePreferences repositoryPreferences = AbstractSVNStorage.getRepositoriesPreferences(this.preferencesNode);
		String [] keys = repositoryPreferences.keys();
		ArrayList<IRepositoryLocation> readLocations = new ArrayList<IRepositoryLocation>();
		for (String current : keys) {
			IRepositoryLocation location = this.newRepositoryLocation(repositoryPreferences.get(current, null));
			readLocations.add(location);
		}
		this.repositories = readLocations.toArray(new IRepositoryLocation[readLocations.size()]);
	}
	
	/**
	 * Sets the file, containing locations info preferences.
	 * Used for compatibility with the versions of Subversive,
	 * where the locations info was stored in files. 
	 */
	protected void setStateInfoFile(IPath stateInfoLocation, String fileName){
		this.stateInfoFile = stateInfoLocation.append(fileName).toFile();
	}
	
	/**
	 * Loads locations info from a file. Left for compatibility with earlier versions - 
	 * to have a probability to read locations and convert to preferences.
	 * Used for compatibility with the versions of Subversive,
	 * where the locations info was stored in files. 
	 * 
	 * @throws Exception
	 */
	protected void loadLocationsFromFile() throws Exception {
		List<IRepositoryLocation> tmp = new ArrayList<IRepositoryLocation>(Arrays.asList(this.repositories));
		ObjectInputStream stream = null;
		try {
			stream = new ObjectInputStream(new FileInputStream(this.stateInfoFile));
			
			// stream.available() does not provide any EOF information
			while (true) {
				SVNRepositoryLocation obj = (SVNRepositoryLocation)stream.readObject();
				if (!tmp.contains(obj)) {
					tmp.add(obj);
				}
			}
		}
		catch (EOFException ex) {
			// EOF, do nothing
		}
		finally {
			if (stream != null) {
				try {stream.close();} catch (Exception ex) {}
			}
		}
		this.repositories = tmp.toArray(new IRepositoryLocation[tmp.size()]);
	}
	
}
