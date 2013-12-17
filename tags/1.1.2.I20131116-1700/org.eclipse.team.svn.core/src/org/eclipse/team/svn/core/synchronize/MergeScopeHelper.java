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

package org.eclipse.team.svn.core.synchronize;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.operation.local.AbstractMergeSet;
import org.eclipse.team.svn.core.operation.local.MergeSet1URL;
import org.eclipse.team.svn.core.operation.local.MergeSet2URL;
import org.eclipse.team.svn.core.operation.local.MergeSetReintegrate;

/**
 * Merge resources scope. Non-persistent.
 * 
 * @author Alexander Gurov
 */
public class MergeScopeHelper {
    protected AbstractMergeSet info;

    public MergeScopeHelper() {    	
    }
    
    public MergeScopeHelper(AbstractMergeSet info) {
        super();
        this.info = info;
    }        

    public String getName() {
    	if (this.info.to == null) {
    		return ""; //$NON-NLS-1$
    	}
    	String url = null;
    	if (this.info instanceof MergeSet1URL) {
    		MergeSet1URL info = (MergeSet1URL)this.info;
        	url = (info.from.length > 1 ? info.from[0].getRoot() : info.from[0]).getUrl();
    	}
    	else if (this.info instanceof MergeSet2URL) {
    		MergeSet2URL info = (MergeSet2URL)this.info;
        	url = (info.fromEnd.length > 1 ? info.fromEnd[0].getRoot() : info.fromEnd[0]).getUrl();
    	}
    	else {
    		MergeSetReintegrate info = (MergeSetReintegrate)this.info;
        	url = (info.from.length > 1 ? info.from[0].getRoot() : info.from[0]).getUrl();
    	}
    	String names = null;
    	for (int i = 0; i < this.info.to.length; i++) {
    		String path = this.info.to[i].getFullPath().toString().substring(1);
    		names = names == null ? path : (names + ", " + path); //$NON-NLS-1$
    	}
    	return SVNMessages.format(SVNMessages.MergeScope_Name, new String[] {url, names});
    }

    public IResource []getRoots() {
        return this.info.to;
    }
    
    public void setMergeSet(AbstractMergeSet info) {
        this.info = info;     
    }
    
    public AbstractMergeSet getMergeSet() {
    	return this.info;
    }
    
}
