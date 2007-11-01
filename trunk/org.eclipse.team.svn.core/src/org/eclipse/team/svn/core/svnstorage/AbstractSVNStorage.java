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
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.internal.preferences.Base64;
import org.eclipse.core.internal.utils.UniversalUniqueIdentifier;
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Revision.Kind;
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
	protected File stateInfoFile;
	
	protected IRepositoryLocation []repositories;

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
			for (Iterator it = from.getRealms().iterator(); it.hasNext(); ) {
				String realm = (String)it.next();
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
		location.setUrl("");
		location.setTrunkLocation("");
		location.setTagsLocation("");
		location.setBranchesLocation("");
		switch (parts.length) {
		case 6:
			location.setTrunkLocation(parts[5].trim());
		case 5:
			location.setTagsLocation(parts[4].trim());
		case 4:
			location.setBranchesLocation(parts[3].trim());
		case 3:
			String label = parts[2].trim();
			if (label.length() > 0) {
				location.setLabel(label);
			}
		case 2:
			location.setUrl(parts[1].trim());
		case 1:
		}
		if (location.getLabel() == null || location.getLabel().length() == 0) {
			location.setLabel(location.getUrlAsIs());
		}
		return location;
	}
	
	public String repositoryLocationAsReference(IRepositoryLocation location) {
		String reference = location.getId();
		reference += ";" + location.getUrlAsIs();
		reference += ";" + location.getLabel();
		reference += ";" + location.getBranchesLocation();
		reference += ";" + location.getTagsLocation();
		reference += ";" + location.getTrunkLocation();
		return reference;
	}
	
	public synchronized void addRepositoryLocation(IRepositoryLocation location) {
		List tmp = new ArrayList(Arrays.asList(this.repositories));
		if (!tmp.contains(location)) {
			tmp.add(location);
			this.repositories = (IRepositoryLocation [])tmp.toArray(new IRepositoryLocation[tmp.size()]);
		}
	}
	
	public synchronized void removeRepositoryLocation(IRepositoryLocation location) {
		List tmp = new ArrayList(Arrays.asList(this.repositories));
		if (tmp.remove(location)) {
			this.repositories = (IRepositoryLocation [])tmp.toArray(new IRepositoryLocation[tmp.size()]);
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
			(selectedKind == Kind.NUMBER ? String.valueOf(((Revision.Number)resource.getSelectedRevision()).getNumber()) : "0") + ";" +
			String.valueOf(IRepositoryRoot.KIND_ROOT) + ";" + 
			String.valueOf(pegKind) + ";" + 
			(pegKind == Kind.NUMBER ? String.valueOf(((Revision.Number)resource.getPegRevision()).getNumber()) : "0");
		return retVal.getBytes();
	}
	
	public IRepositoryResource repositoryResourceFromBytes(byte []bytes) {
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
		IRepositoryLocation location = this.getRepositoryLocation(data[1]);
		if (location == null) {
		    return null;
		}
		int revisionKind = Integer.parseInt(data[3]);
		Revision selectedRevision = revisionKind == Kind.NUMBER ? (Revision)Revision.fromNumber(Long.parseLong(data[4])) : new ISVNStorage.KindBasedRevision(revisionKind);
		Revision pegRevision = null;
		if (data.length > 6) {
			int pegKind = Integer.parseInt(data[6]);
			pegRevision = pegKind == Kind.NUMBER ? (Revision)Revision.fromNumber(Long.parseLong(data[7])) : new ISVNStorage.KindBasedRevision(pegKind);
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
    
	protected void initializeImpl(IPath stateInfoLocation, String fileName) throws Exception {
		this.stateInfoFile = stateInfoLocation.append(fileName).toFile();
		// if and only if the file does not exist
		if (this.stateInfoFile.createNewFile()) {
			this.saveLocations(); // initialize newly created state info file
		}
		
		try {
			this.loadLocations();
		}
		catch (Exception ex) {
			LoggedOperation.reportError(SVNTeamPlugin.instance().getResource("Error.LoadLocations"), ex);
			this.saveLocations(); // fix up a problem
		}
	}

	protected void saveLocations() throws Exception {
		ObjectOutputStream stream = null;
		try {
			stream = new ObjectOutputStream(new FileOutputStream(this.stateInfoFile));
			for (int i = 0; i < this.repositories.length; i++) {
				stream.writeObject(this.repositories[i]);
			}
		}
		finally {
			if (stream != null) {
				try {stream.close();} catch (Exception ex) {}
			}
		}
	}
	
	protected void loadLocations() throws Exception {
		List tmp = new ArrayList(Arrays.asList(this.repositories));
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
		this.repositories = (IRepositoryLocation [])tmp.toArray(new IRepositoryLocation[tmp.size()]);
	}
	
}
