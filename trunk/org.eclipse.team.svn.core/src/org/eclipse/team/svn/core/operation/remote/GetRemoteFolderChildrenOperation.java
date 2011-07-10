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

package org.eclipse.team.svn.core.operation.remote;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNProperty.BuiltIn;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource.Information;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * 
 * Load folder children. Used in asynchronous repository view refresh.
 * 
 * @author Alexander Gurov
 */
public class GetRemoteFolderChildrenOperation extends AbstractActionOperation {
	protected IRepositoryContainer parent;
	protected boolean handleExternals;
	protected IRepositoryResource []children;
	protected boolean caseInsensitive;
	protected Map<IRepositoryResource, String> externalsNames;

	public GetRemoteFolderChildrenOperation(IRepositoryContainer parent, boolean handleExternals) {
		this(parent, handleExternals, false);
	}

	public GetRemoteFolderChildrenOperation(IRepositoryContainer parent, boolean handleExternals, boolean caseInsensitive) {
		super("Operation_GetRemoteChildren", SVNMessages.class); //$NON-NLS-1$
		this.parent = parent;
		this.handleExternals = handleExternals;
		this.caseInsensitive = caseInsensitive;
		this.externalsNames = new HashMap<IRepositoryResource, String>();
	}

	public IRepositoryResource[] getChildren() {
		return this.children;
	}
	
	public String getExternalsName(IRepositoryResource resource) {
		return this.externalsNames.get(resource);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource []tmp = this.parent.getChildren();
		
		// handle svn:externals, if present:
		Information info = this.parent.getInfo();
		if (info != null && info.hasProperties && this.handleExternals) {
			IRepositoryLocation location = this.parent.getRepositoryLocation();
			ISVNConnector proxy = location.acquireSVNProxy();
			try {
				SVNProperty data = proxy.getProperty(SVNUtility.getEntryRevisionReference(this.parent), BuiltIn.EXTERNALS, new SVNProgressMonitor(this, monitor, null));
				if (data != null) {
					//Map externals;
					try {
						Map<String, SVNEntryRevisionReference> externals = SVNUtility.parseSVNExternalsProperty(data.value, this.parent);
						List<IRepositoryResource> newTmp = Arrays.asList(tmp);

						for (Iterator<Map.Entry<String, SVNEntryRevisionReference>> it = externals.entrySet().iterator(); it.hasNext();) {
							try {
								Map.Entry<String, SVNEntryRevisionReference> entry = it.next();
								String name = entry.getKey();
								SVNEntryRevisionReference ref = entry.getValue();
								IRepositoryResource repositoryResourtce = SVNRemoteStorage.instance().asRepositoryResource(location, ref, new SVNProgressMonitor(this, monitor, null));															
								if (repositoryResourtce != null) {
									repositoryResourtce.setSelectedRevision(ref.revision);
									repositoryResourtce.setPegRevision(ref.pegRevision);
									newTmp.add(repositoryResourtce);							
									this.externalsNames.put(repositoryResourtce, name);	
								}
							} catch (Exception e) {
								this.reportStatus(new Status(IStatus.WARNING, SVNTeamPlugin.NATURE_ID, IStatus.OK, this.getShortErrorMessage(e), e));
							}
						}
						tmp = newTmp.toArray(new IRepositoryResource[newTmp.size()]);
					}
					catch (UnreportableException ex) {
						this.reportStatus(new Status(IStatus.WARNING, SVNTeamPlugin.NATURE_ID, IStatus.OK, this.getShortErrorMessage(ex), ex));
					}
				}
			} finally {
				location.releaseSVNProxy(proxy);
			}
		}
		
		Arrays.sort(tmp, new Comparator<IRepositoryResource>() {
			public int compare(IRepositoryResource first, IRepositoryResource second) {
				boolean firstContainer = first instanceof IRepositoryContainer;
				boolean secondContainer = second instanceof IRepositoryContainer;
				if (firstContainer && secondContainer) {
					boolean firstRoot = first instanceof IRepositoryRoot;
					boolean secondRoot = second instanceof IRepositoryRoot;
					return firstRoot == secondRoot ? (firstRoot ? this.compareRoots(((IRepositoryRoot)first).getKind(), ((IRepositoryRoot)second).getKind()) : this.compareNames(first, second)) : (firstRoot ? -1 : 1);
				}
				return firstContainer == secondContainer ? this.compareNames(first, second) : (firstContainer ? -1 : 1);
			}
			
			private int compareNames(IRepositoryResource first, IRepositoryResource second) {
				String firstName = GetRemoteFolderChildrenOperation.this.externalsNames.containsKey(first) ? GetRemoteFolderChildrenOperation.this.externalsNames.get(first) : first.getName();
				String secondName = GetRemoteFolderChildrenOperation.this.externalsNames.containsKey(second) ? GetRemoteFolderChildrenOperation.this.externalsNames.get(second) : second.getName();
				return GetRemoteFolderChildrenOperation.this.caseInsensitive ? firstName.compareToIgnoreCase(secondName) : firstName.compareTo(secondName);
			}
			
			private int compareRoots(int firstKind, int secondKind) {
				return firstKind < secondKind ? -1 : 1;
			}
		});
		this.children = tmp;
	}

}
