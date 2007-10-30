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

package org.eclipse.team.svn.ui.synchronize.variant;

import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;

/**
 * Remote resource emulator variant. Allow us to fetch BASE resource info for the remote view without request to repository.
 * 
 * @author Alexander Gurov
 */
public abstract class VirtualRemoteResourceVariant extends RemoteResourceVariant {

	public VirtualRemoteResourceVariant(ILocalResource local) {
		super(local);
	}
	
	public String getStatus() {
		return this.isNotOnRepository() ? IStateFilter.ST_NOTEXISTS : IStateFilter.ST_NORMAL;
	}
	
    protected boolean isNotOnRepository() {
        return !IStateFilter.SF_ONREPOSITORY.accept(this.local.getResource(), this.local.getStatus(), this.local.getChangeMask());
    }
    
}
