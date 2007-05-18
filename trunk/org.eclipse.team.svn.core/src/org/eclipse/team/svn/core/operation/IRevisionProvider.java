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

package org.eclipse.team.svn.core.operation;

import org.eclipse.team.svn.core.resource.IRepositoryLocation;

/**
 * Interface which will allow to return produced revision number for the operations  
 * which modify repository 
 * 
 * @author Alexander Gurov
 */
public interface IRevisionProvider {
	 public static class RevisionPair {
        public final long revision;
        public final String []paths;
        public final IRepositoryLocation location;
        
        public RevisionPair(long revision, String []paths, IRepositoryLocation location) {
         	this.revision = revision;
         	this.paths = paths;
         	this.location = location;
        }
    }
    
	public RevisionPair []getRevisions();
	
}
