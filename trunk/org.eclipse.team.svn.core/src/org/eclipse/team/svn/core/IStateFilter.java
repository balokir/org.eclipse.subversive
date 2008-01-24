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

package org.eclipse.team.svn.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNProperty.BuiltIn;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Resource state filter interface and most useful implementations
 * 
 * @author Alexander Gurov
 */
public interface IStateFilter {

	public static final String ST_NOTEXISTS = null;

	public static final String ST_IGNORED = "Ignored";

	public static final String ST_NEW = "New";

	public static final String ST_ADDED = "Added";

	public static final String ST_NORMAL = "Normal";

	public static final String ST_MODIFIED = "Modified";

	public static final String ST_CONFLICTING = "Conflicting";

	public static final String ST_DELETED = "Deleted";

	public static final String ST_MISSING = "Missing";

	public static final String ST_OBSTRUCTED = "Obstructed";

	public static final String ST_PREREPLACED = "Prereplaced";

	public static final String ST_REPLACED = "Replaced";

	public static final String ST_LINKED = "Linked";

	public boolean accept(ILocalResource resource);
	
	public boolean accept(IResource resource, String state, int mask);
	
	public boolean allowsRecursion(ILocalResource resource);
	
	public boolean allowsRecursion(IResource resource, String state, int mask);
	
	public abstract class AbstractStateFilter implements IStateFilter {
		public boolean accept(ILocalResource resource) {
			return this.acceptImpl(resource, resource.getResource(), resource.getStatus(), resource.getChangeMask());
		}
		public boolean accept(IResource resource, String state, int mask) {
			return this.acceptImpl(null, resource, state, mask);
		}
		public boolean allowsRecursion(ILocalResource resource) {
			return this.allowsRecursionImpl(null, resource.getResource(), resource.getStatus(), resource.getChangeMask());
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return this.allowsRecursionImpl(null, resource, state, mask);
		}
		
		protected abstract boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask);
		protected abstract boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask);
		
		protected ILocalResource takeLocal(ILocalResource local, IResource resource) {
			return local != null ? local : SVNRemoteStorage.instance().asLocalResource(resource);
		}
	}

	public static final IStateFilter SF_LOCKED = new AbstractStateFilter() {
	    protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
	        return (mask & ILocalResource.IS_LOCKED) != 0;
	    }
	    protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return SF_ONREPOSITORY.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_READY_TO_LOCK = new AbstractStateFilter() {
	    protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
	        return resource instanceof IFile && (mask & ILocalResource.IS_LOCKED) == 0 && IStateFilter.SF_EXCLUDE_DELETED.accept(resource, state, mask);
	    }
	    
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_EXCLUDE_DELETED.accept(resource, state, mask);
		}
	};

	public static final IStateFilter SF_EXTERNAL = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return (mask & ILocalResource.IS_EXTERNAL) != 0;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_LINKED = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state == IStateFilter.ST_LINKED;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_ALL = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_NOTEXISTS = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state == IStateFilter.ST_NOTEXISTS || state == IStateFilter.ST_LINKED;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_OBSTRUCTED = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state == IStateFilter.ST_OBSTRUCTED;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_VALID = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state != IStateFilter.ST_OBSTRUCTED && state != IStateFilter.ST_LINKED;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return this.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_REPLACED = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state == IStateFilter.ST_REPLACED;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_PREREPLACED = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state == IStateFilter.ST_PREREPLACED;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_PREREPLACEDREPLACED = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_REPLACED;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_IGNORED = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state == IStateFilter.ST_IGNORED;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_UNVERSIONED = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_NEW || 
				state == IStateFilter.ST_IGNORED || state == IStateFilter.ST_NOTEXISTS;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_VERSIONED = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_REPLACED || state == IStateFilter.ST_PREREPLACED ||
				state == IStateFilter.ST_ADDED || state == IStateFilter.ST_NORMAL || 
				state == IStateFilter.ST_MODIFIED || state == IStateFilter.ST_CONFLICTING || state == IStateFilter.ST_DELETED || state == IStateFilter.ST_MISSING;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return this.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_NOTONREPOSITORY = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_NEW || 
				state == IStateFilter.ST_IGNORED || state == IStateFilter.ST_NOTEXISTS ||
				state == IStateFilter.ST_ADDED;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_ONREPOSITORY = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_REPLACED || 
				state == IStateFilter.ST_NORMAL || state == IStateFilter.ST_MODIFIED || 
				state == IStateFilter.ST_CONFLICTING || state == IStateFilter.ST_DELETED || state == IStateFilter.ST_MISSING;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_NEW = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_NEW;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state != IStateFilter.ST_IGNORED && state != IStateFilter.ST_OBSTRUCTED && state != IStateFilter.ST_LINKED;
		}
	};
	
	public static final IStateFilter SF_ADDED = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_REPLACED || 
				state == IStateFilter.ST_NEW || state == IStateFilter.ST_ADDED;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_NOTMODIFIED = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state == IStateFilter.ST_NORMAL || state == IStateFilter.ST_NOTEXISTS || state == IStateFilter.ST_LINKED;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_MODIFIED = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state == IStateFilter.ST_MODIFIED || state == IStateFilter.ST_CONFLICTING;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_CONFLICTING = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state == IStateFilter.ST_CONFLICTING;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_DELETED = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_REPLACED || 
				state == IStateFilter.ST_DELETED || state == IStateFilter.ST_MISSING;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_MISSING = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state == IStateFilter.ST_MISSING;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_COMMITABLE = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_REPLACED || state == IStateFilter.ST_ADDED || 
				state == IStateFilter.ST_MODIFIED || state == IStateFilter.ST_DELETED || state == IStateFilter.ST_MISSING;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_REVERTABLE = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_CONFLICTING || 
				state == IStateFilter.ST_REPLACED || state == IStateFilter.ST_ADDED || 
				state == IStateFilter.ST_MODIFIED || state == IStateFilter.ST_DELETED || state == IStateFilter.ST_MISSING;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_ANY_CHANGE = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return 
				state != IStateFilter.ST_IGNORED && state != IStateFilter.ST_NORMAL && 
				state != IStateFilter.ST_OBSTRUCTED && state != IStateFilter.ST_LINKED;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state != IStateFilter.ST_IGNORED && state != IStateFilter.ST_OBSTRUCTED && state != IStateFilter.ST_LINKED;
		}
	};

	public static final IStateFilter SF_EXCLUDE_DELETED = new AbstractStateFilter() {
        protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
        	if (IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask)) {
                return state != IStateFilter.ST_DELETED && state != IStateFilter.ST_MISSING;
            }
            return false;
        }
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return this.accept(resource, state, mask);
		}
    };
    
    public static final IStateFilter SF_NEEDS_LOCK = new AbstractStateFilter() {		
		protected boolean acceptImpl(ILocalResource local, final IResource resource, String state, int mask) {
			if (!(resource instanceof IFile) || IStateFilter.SF_UNVERSIONED.accept(resource, state, mask) || !resource.isAccessible()) {
				return false;
			}
			final SVNProperty [][]propData = new SVNProperty[1][];
			IActionOperation op = new AbstractActionOperation("Operation.CheckProperty") {
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(resource);
					ISVNConnector proxy = location.acquireSVNProxy();
					try {
						propData[0] = SVNUtility.properties(proxy, new SVNEntryRevisionReference(FileUtility.getWorkingCopyPath(resource), null, SVNRevision.BASE), new SVNProgressMonitor(this, monitor, null));
					}
					finally {
						location.releaseSVNProxy(proxy);
					}
				}
			};
			ProgressMonitorUtility.doTaskExternalDefault(op, new NullProgressMonitor());
			boolean needsLock = false;
			if (propData[0] != null) {
				for (int i = 0; i < propData[0].length; i++) {
					if (propData[0][i].name.equals(BuiltIn.NEEDS_LOCK)) {
						needsLock = true;
						break;
					}
				}
				return needsLock;
			}
			return false;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_MODIFIED_NOT_IGNORED = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return !IStateFilter.SF_IGNORED.accept(resource, state, mask) &&
					!IStateFilter.SF_NOTMODIFIED.accept(resource, state, mask);
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state != IStateFilter.ST_IGNORED && state != IStateFilter.ST_OBSTRUCTED && state != IStateFilter.ST_LINKED;
		}
	};
	
	public static final IStateFilter SF_EXCLUDE_PREREPLACED_AND_DELETED = new AbstractStateFilter() {
        protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
        	if (IStateFilter.SF_VERSIONED.accept(resource, state, mask) &&
                !IStateFilter.SF_PREREPLACED.accept(resource, state, mask)) {
                return state != IStateFilter.ST_DELETED && state != IStateFilter.ST_MISSING;
            }
            return false;
        }
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_EXCLUDE_DELETED.accept(resource, state, mask);
		}
    };
	
	public static final IStateFilter SF_EXCLUDE_PREREPLACED_AND_DELETED_FILES = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return resource instanceof IFile && IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED.accept(resource, state, mask);
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_EXCLUDE_DELETED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_VERSIONED_FOLDERS = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return resource instanceof IContainer && IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};

	public static final IStateFilter SF_VERSIONED_FILES = new AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return resource instanceof IFile && IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};

}
