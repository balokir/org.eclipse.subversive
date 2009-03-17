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

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.operation.local.AbstractMergeSet;
import org.eclipse.team.svn.core.synchronize.MergeScopeHelper;
import org.eclipse.team.ui.synchronize.AbstractSynchronizeScope;

/**
 * Merge resources scope. Non-persistent.
 * 
 * @author Alexander Gurov
 */
public class MergeScope extends AbstractSynchronizeScope {
	     
	protected MergeScopeHelper scopeHelper;
	
    public MergeScope(AbstractMergeSet info) {
        this.scopeHelper = new MergeScopeHelper(info);
    }
	
	public MergeScope() {
		this.scopeHelper = new MergeScopeHelper();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeScope#getName()
	 */
	public String getName() {
		return this.scopeHelper.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeScope#getRoots()
	 */
	public IResource[] getRoots() {		
		return this.scopeHelper.getRoots();
	}
	
    public void setMergeSet(AbstractMergeSet info) {
    	this.scopeHelper.setMergeSet(info);       
        this.fireRootsChanges();
    }
    
    public MergeScopeHelper getMergeScopeHelper() {
    	return this.scopeHelper;
    }
}
