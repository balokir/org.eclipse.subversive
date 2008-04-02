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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.preferences.Base64;
import org.eclipse.core.internal.utils.UniversalUniqueIdentifier;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.resource.ISVNStorage;
import org.eclipse.team.svn.core.resource.ProxySettings;
import org.eclipse.team.svn.core.resource.SSHSettings;
import org.eclipse.team.svn.core.resource.SSLSettings;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Basic IRemoteStorage implementation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractSVNStorage implements ISVNStorage {
	
	private static final String URL_TO_STORE = "http://eclipse.org/subversive/"; 
	
	protected File stateInfoFile;
	protected String preferencesNode;
	protected IPreferenceChangeListener repoPrefChangeListener;
	
	protected IRepositoryLocation []repositories;
	
	protected class RepositoryPreferenceChangeListener implements IPreferenceChangeListener {
		public void preferenceChange(PreferenceChangeEvent event) {
			try {
				AbstractSVNStorage.this.loadLocations();
				AbstractSVNStorage.this.saveLocations();
			}
			catch (Exception ex) {
				LoggedOperation.reportError(SVNTeamPlugin.instance().getResource("Error.LoadLocations"), ex);
			}
		}
	}
	
	public AbstractSVNStorage() {
		this.repositories = new IRepositoryLocation[0];
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
		
		ProxySettings proxyOriginal = from.getProxySettings();
		ProxySettings proxyNew = to.getProxySettings();
		proxyNew.setAuthenticationEnabled(proxyOriginal.isAuthenticationEnabled());
		proxyNew.setEnabled(proxyOriginal.isEnabled());
		proxyNew.setHost(proxyOriginal.getHost());
		proxyNew.setPassword(proxyOriginal.getPassword());
		proxyNew.setPasswordSaved(proxyOriginal.isPasswordSaved());
		proxyNew.setPort(proxyOriginal.getPort());
		proxyNew.setUsername(proxyOriginal.getUsername());
		
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
		String []parts = reference.split(";");
		if (parts.length == 0 || parts[0].length() == 0) {
			return this.newRepositoryLocation();
		}
		IRepositoryLocation location = this.getRepositoryLocation(parts[0]);
		if (location != null) {
			return location;
		}
		String id = parts[0].trim();
		location = new SVNRepositoryLocation(id.length() > 0 ? id : new UniversalUniqueIdentifier().toString());
		location.setTrunkLocation("");
		location.setTagsLocation("");
		location.setBranchesLocation("");
		location.setAuthorName("");
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
	}
	
	public synchronized void removeRepositoryLocation(IRepositoryLocation location) {
		List<IRepositoryLocation> tmp = new ArrayList<IRepositoryLocation>(Arrays.asList(this.repositories));
		this.removeAuthInfoForLocation(location, "");
		String [] realms = location.getRealms().toArray(new String[0]);
		for (String realm : realms) {
			this.removeAuthInfoForLocation(location, realm);
		}
		if (tmp.remove(location)) {
			this.repositories = tmp.toArray(new IRepositoryLocation[tmp.size()]);
		}
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
			new String(Base64.encode(String.valueOf(resource instanceof IRepositoryContainer).getBytes())) + ";" + 
			resource.getRepositoryLocation().getId() + ";" +
			new String(Base64.encode(resource.getUrl().getBytes())) + ";" +
			String.valueOf(selectedKind) + ";" + 
			(selectedKind == Kind.NUMBER ? String.valueOf(((SVNRevision.Number)resource.getSelectedRevision()).getNumber()) : "0") + ";" +
			String.valueOf(IRepositoryRoot.KIND_ROOT) + ";" + 
			String.valueOf(pegKind) + ";" + 
			(pegKind == Kind.NUMBER ? String.valueOf(((SVNRevision.Number)resource.getPegRevision()).getNumber()) : "0");
		return retVal.getBytes();
	}
	
	public IRepositoryResource repositoryResourceFromBytes(byte []bytes) {
		return this.repositoryResourceFromBytes(bytes, null);
	}
	
	public IRepositoryResource repositoryResourceFromBytes(byte []bytes, IRepositoryLocation location) {
		if (bytes == null) {
			return null;
		}
		String []data = new String(bytes).split(";");
		boolean isFolder = false;
		boolean base64Label = false;
		if ("true".equals(data[0])) {
			isFolder = true;
		}
		else if (!"false".equals(data[0])) {
			isFolder = "true".equals(new String(Base64.decode(data[0].getBytes())));
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
				return location.isStructureEnabled() ? (location.getUrl() + "/" + location.getTrunkLocation()) : location.getUrl();
			}
			case IRepositoryRoot.KIND_BRANCHES: {
				return location.isStructureEnabled() ? (location.getUrl() + "/" + location.getBranchesLocation()) : location.getUrl();
			}
			case IRepositoryRoot.KIND_TAGS: {
				return location.isStructureEnabled() ? (location.getUrl() + "/" + location.getTagsLocation()) : location.getUrl();
			}
		}
		return null;
	}
    
	protected void initializeImpl(String preferencesNode) throws Exception {
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
			LoggedOperation.reportError(SVNTeamPlugin.instance().getResource("Error.LoadLocations"), ex);
		}
	}

	protected void saveLocations() throws Exception {
		IEclipsePreferences repositoryPreferences = AbstractSVNStorage.getRepositoriesPreferences(this.preferencesNode);
		repositoryPreferences.removePreferenceChangeListener(this.repoPrefChangeListener);
		repositoryPreferences.clear();
		for (IRepositoryLocation current : this.repositories) {
			repositoryPreferences.put(current.getId(), this.repositoryLocationAsReference(current));
			this.saveAuthInfo(current, "");
			String [] realms = current.getRealms().toArray(new String[0]);
			for (String realm : realms) {
				this.saveAuthInfo(current, realm);
			}
		}
		repositoryPreferences.flush();
		SVNTeamPlugin.instance().savePluginPreferences();
		repositoryPreferences.addPreferenceChangeListener(this.repoPrefChangeListener);
	}
	
	protected void saveAuthInfo(IRepositoryLocation location, String realm) throws Exception {
		Platform.flushAuthorizationInfo(new URL(AbstractSVNStorage.URL_TO_STORE), location.getId(), realm);
		IRepositoryLocation tmp = realm.equals("") ? location : location.getLocationForRealm(realm);
		boolean toStorePass = tmp.isPasswordSaved();
		HashMap<String, String> authInfo = new HashMap<String, String>();

		//store normal password settings
		authInfo.put("username", tmp.getUsername());
		authInfo.put("password", toStorePass ? tmp.getPassword() : "");
		authInfo.put("password_saved", String.valueOf(toStorePass));
		
		//store SSH settings
		SSHSettings sshSettings = tmp.getSSHSettings();
		authInfo.put("ssh_port", "" + String.valueOf(sshSettings.getPort()));
		boolean useKeyFile = sshSettings.isUseKeyFile();
		authInfo.put("ssh_use_key", String.valueOf(useKeyFile));
		boolean savePassphrase = sshSettings.isPassPhraseSaved();
		authInfo.put("ssh_passphrase_saved", useKeyFile ? String.valueOf(savePassphrase) : "false");
		authInfo.put("ssh_key", useKeyFile ? sshSettings.getPrivateKeyPath() : "");
		authInfo.put("ssh_passprase", (useKeyFile && savePassphrase) ? sshSettings.getPassPhrase() : "");
		
		//store SSL settings
		SSLSettings sslSettings = tmp.getSSLSettings();
		boolean clientAuthEnabled = sslSettings.isAuthenticationEnabled();
		savePassphrase = sslSettings.isPassPhraseSaved();
		authInfo.put("ssl_enabled", String.valueOf(clientAuthEnabled));
		authInfo.put("ssl_certificate", clientAuthEnabled ? sslSettings.getCertificatePath() : "");
		authInfo.put("ssl_passphrase_saved", clientAuthEnabled ? String.valueOf(savePassphrase) : "false");
		authInfo.put("ssl_passphrase", (clientAuthEnabled && savePassphrase) ? sslSettings.getPassPhrase() : "");
		
		//store the map in platform
		Platform.addAuthorizationInfo(new URL(AbstractSVNStorage.URL_TO_STORE), location.getId(), realm, authInfo);
	}
	
	protected void loadAuthInfo(IRepositoryLocation location, String realm) throws Exception {
		Map<String, String> authInfo = Platform.getAuthorizationInfo(new URL(AbstractSVNStorage.URL_TO_STORE), location.getId(), realm);
		if (authInfo != null) {
			IRepositoryLocation tmp;
			boolean toAddRealm = !realm.equals(""); 
			if (toAddRealm) {
				tmp = this.newRepositoryLocation();
			}
			else {
				tmp = location;
			}
			
			//recover normal password settings
			tmp.setUsername(authInfo.get("username"));
			tmp.setPasswordSaved(authInfo.get("password_saved").equals("true"));
			tmp.setPassword(authInfo.get("password"));
			
			//recover SSH settings
			SSHSettings sshSettings = tmp.getSSHSettings();
			sshSettings.setPort(Integer.parseInt(authInfo.get("ssh_port")));;
			sshSettings.setUseKeyFile(authInfo.get("ssh_use_key").equals("true"));
			sshSettings.setPrivateKeyPath(authInfo.get("ssh_key"));
			sshSettings.setPassPhraseSaved(authInfo.get("ssh_passphrase_saved").equals("true"));
			sshSettings.setPassPhrase(authInfo.get("ssh_passprase"));
			
			//recover SSL settings
			SSLSettings sslSettings = tmp.getSSLSettings();
			sslSettings.setAuthenticationEnabled(authInfo.get("ssl_enabled").equals("true"));
			sslSettings.setCertificatePath(authInfo.get("ssl_certificate"));
			sslSettings.setPassPhraseSaved(authInfo.get("ssl_passphrase_saved").equals("true"));
			sslSettings.setPassPhrase(authInfo.get("ssl_passphrase"));
			
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
			this.loadAuthInfo(location, "");
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
			
			// why stream.available() does not work ???
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
