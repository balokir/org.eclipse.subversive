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

/**
 * Synchronize view state filter interface with predefined filter set
 * 
 * @author Alexander Gurov
 */
public interface ISyncStateFilter extends IStateFilter {
    public boolean acceptRemote(IResource resource, String state, int mask);

    public static ISyncStateFilter SF_ONREPOSITORY = new ISyncStateFilter() {
        public boolean accept(IResource resource, String state, int mask) {
            return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask);
        }

        public boolean acceptRemote(IResource resource, String state, int mask) {
            return !IStateFilter.SF_NOTEXISTS.accept(resource, state, mask);
        }
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return true;
		}
    };

    public static ISyncStateFilter SF_OVERRIDE = new ISyncStateFilter() {
        public boolean accept(IResource resource, String state, int mask) {
            return 
            	IStateFilter.SF_REVERTABLE.accept(resource, state, mask) || 
            	IStateFilter.SF_NONVERSIONED.accept(resource, state, mask);
        }

        public boolean acceptRemote(IResource resource, String state, int mask) {
            return !IStateFilter.SF_NOTEXISTS.accept(resource, state, mask);
        }
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return true;
		}
    };

    public static class StateFilterWrapper implements ISyncStateFilter {
        protected IStateFilter filter;
        
        public StateFilterWrapper(IStateFilter filter) {
            this.filter = filter;
        }
        
        public boolean accept(IResource resource, String state, int mask) {
            return this.filter.accept(resource, state, mask);
        }
        
        public boolean acceptRemote(IResource resource, String state, int mask) {
            return false;
        }
        
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return true;
		}
    }
    
}
