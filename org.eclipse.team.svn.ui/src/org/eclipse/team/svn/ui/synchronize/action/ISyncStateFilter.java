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

package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;

/**
 * Synchronize view state filter interface with predefined filter set
 * 
 * @author Alexander Gurov
 */
public interface ISyncStateFilter extends IStateFilter {
    public boolean acceptRemote(IResource resource, String state, int mask);
    public boolean acceptGroupNodes();
    
    public abstract class AbstractSyncStateFilter extends IStateFilter.AbstractStateFilter implements ISyncStateFilter {
    	
    }

    public static ISyncStateFilter SF_ONREPOSITORY = new AbstractSyncStateFilter() {
        public boolean acceptRemote(IResource resource, String state, int mask) {
            return !IStateFilter.SF_NOTEXISTS.accept(resource, state, mask);
        }
        
        public boolean acceptGroupNodes() {
        	return true;
        }
        
        protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
            return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask);
        }
        
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
    };

    public static ISyncStateFilter SF_OVERRIDE = new AbstractSyncStateFilter() {
        public boolean acceptRemote(IResource resource, String state, int mask) {
            return !IStateFilter.SF_NOTEXISTS.accept(resource, state, mask);
        }
        
        public boolean acceptGroupNodes() {
        	return true;
        }
        
        protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
            return 
            	IStateFilter.SF_REVERTABLE.accept(resource, state, mask) || 
            	IStateFilter.SF_UNVERSIONED.accept(resource, state, mask);
        }

		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
    };

    public static class StateFilterWrapper implements ISyncStateFilter {
        protected IStateFilter local;
        protected IStateFilter remote;
        protected boolean acceptGroupNodes;
        
        public StateFilterWrapper(IStateFilter filter, boolean acceptGroupNodes) {
        	this(filter, null, acceptGroupNodes);
        }
        
        public StateFilterWrapper(IStateFilter local, IStateFilter remote, boolean acceptGroupNodes) {
            this.local = local;
            this.remote = remote;
            this.acceptGroupNodes = acceptGroupNodes;
        }
        
        public boolean acceptGroupNodes() {
        	return this.acceptGroupNodes;
        }
        
        public boolean acceptRemote(IResource resource, String state, int mask) {
            return this.remote != null && this.remote.accept(resource, state, mask);
        }
        
        public boolean accept(IResource resource, String state, int mask) {
            return this.local != null && this.local.accept(resource, state, mask);
        }
        
		public boolean accept(ILocalResource resource) {
            return this.local != null && this.local.accept(resource);
		}

		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return true;
		}

		public boolean allowsRecursion(ILocalResource resource) {
			return true;
		}
    }
    
}
