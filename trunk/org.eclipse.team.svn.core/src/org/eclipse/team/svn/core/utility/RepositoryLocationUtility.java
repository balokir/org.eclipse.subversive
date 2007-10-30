/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.utility;

import org.eclipse.team.svn.core.resource.IRepositoryLocation;

/**
 * Utility for comparing repository locations by UUID
 *
 * @author Sergiy Logvin
 */
public class RepositoryLocationUtility {
    
    protected IRepositoryLocation location;
    
    public RepositoryLocationUtility(IRepositoryLocation location) {
        this.location = location;
    }
    
    public IRepositoryLocation getRepositoryLocation() {
        return this.location;
    }
    
    public String getRepositoryUUID() {
        String uuid = this.location.getRepositoryUUID();
        return uuid == null ? this.location.getId() : uuid;
    }
    
    public int hashCode() {
        int h = 17;
        String username = this.location.getUsername();
        String password = this.location.getPassword();
        h += (31 * this.getRepositoryUUID().hashCode());
        h += (31 * (username != null ? username.hashCode() : 0));
        h += (31 * (password != null ? password.hashCode() : 0));
        
        return h;
    }
    
    public boolean equals(Object arg0) {
        RepositoryLocationUtility location2 = (RepositoryLocationUtility) arg0;

        return 
	        this.getRepositoryUUID().equals(location2.getRepositoryUUID()) &&
	        (this.location.getUsername() != null && this.location.getUsername().equals(location2.getRepositoryLocation().getUsername()) || 
	        this.location.getUsername() == location2.getRepositoryLocation().getUsername()) &&
	        (this.location.getPassword() != null && this.location.getPassword().equals(location2.getRepositoryLocation().getPassword()) ||
	        this.location.getPassword() == location2.getRepositoryLocation().getPassword());
    }
    
}
