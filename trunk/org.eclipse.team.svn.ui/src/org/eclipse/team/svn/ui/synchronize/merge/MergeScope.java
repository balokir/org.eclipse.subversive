/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.merge;

import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.ui.synchronize.AbstractSynchronizeScope;
import org.eclipse.ui.IMemento;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Merge resources scope. Non-persistent.
 * 
 * @author Alexander Gurov
 */
public class MergeScope extends AbstractSynchronizeScope {
    protected IResource []local;
    protected IRepositoryResource []remoteResources;
    protected Revision startRevision;

    public MergeScope(IResource []local, IRepositoryResource []remoteResources, Revision startRevision) {
        super();
        this.setInfoImpl(local, remoteResources, startRevision);
    }

    public MergeScope(IMemento memento) {
        super(memento);
    }

    public String getName() {
    	if (this.local == null || this.remoteResources == null) {
    		return "";
    	}
    	String url = (this.remoteResources.length > 1 ? this.remoteResources[0].getRoot() : this.remoteResources[0]).getUrl();
    	String names = null;
    	for (int i = 0; i < this.local.length; i++) {
    		String path = this.local[i].getFullPath().toString();
    		names = names == null ? path : (names + ", " + path);
    	}
    	String message = SVNTeamUIPlugin.instance().getResource("MergeScope.Name");
        return MessageFormat.format(message, new String[] {url, names});
    }

    public IResource []getRoots() {
        return this.local;
    }
    
    public void setInfo(IResource []local, IRepositoryResource []remoteResources, Revision startRevision) {
        this.setInfoImpl(local, remoteResources, startRevision);
        this.fireRootsChanges();
    }
    
	public IRepositoryResource []getRepositoryResources() {
		return this.remoteResources;
	}
    
    public Revision getStartRevision() {
        return this.startRevision;
    }

    private void setInfoImpl(IResource []local, IRepositoryResource []remoteResources, Revision startRevision) {
        this.local = local;
        this.remoteResources = remoteResources;
        this.startRevision = startRevision;
    }
    
}
