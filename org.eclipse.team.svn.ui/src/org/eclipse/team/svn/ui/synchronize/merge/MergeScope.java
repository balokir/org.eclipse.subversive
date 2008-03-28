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

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.operation.local.MergeSet;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.ui.synchronize.AbstractSynchronizeScope;
import org.eclipse.ui.IMemento;

/**
 * Merge resources scope. Non-persistent.
 * 
 * @author Alexander Gurov
 */
public class MergeScope extends AbstractSynchronizeScope {
    protected MergeSet info;

    public MergeScope(MergeSet info) {
        super();
        this.info = info;
    }

    public MergeScope(IMemento memento) {
        super(memento);
    }

    public String getName() {
    	if (this.info.to == null || this.info.fromEnd == null) {
    		return "";
    	}
    	String url = (this.info.fromEnd.length > 1 ? this.info.fromEnd[0].getRoot() : this.info.fromEnd[0]).getUrl();
    	String names = null;
    	for (int i = 0; i < this.info.to.length; i++) {
    		String path = this.info.to[i].getFullPath().toString().substring(1);
    		names = names == null ? path : (names + ", " + path);
    	}
        return SVNTeamUIPlugin.instance().getResource("MergeScope.Name", new String[] {url, names});
    }

    public IResource []getRoots() {
        return this.info.to;
    }
    
    public void setMergeSet(MergeSet info) {
        this.info = info;
        this.fireRootsChanges();
    }
    
    public MergeSet getMergeSet() {
    	return this.info;
    }
    
}
