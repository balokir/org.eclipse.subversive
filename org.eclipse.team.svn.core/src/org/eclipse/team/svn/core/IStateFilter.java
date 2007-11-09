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
import org.eclipse.team.svn.core.client.EntryRevisionReference;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.PropertyData.BuiltIn;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
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

	public static final IStateFilter SF_EXTERNAL = new IStateFilter() {
	
		public boolean accept(IResource resource, String state, int mask) {
			if ((mask & ILocalResource.IS_EXTERNAL) != 0) {
				return true;
			}
			
			return false;
		}
	
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return true;
		}
		
	};
	
	public boolean accept(IResource resource, String state, int mask);
	
	public boolean allowsRecursion(IResource resource, String state, int mask);
	
	public static final IStateFilter SF_LINKED = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return state == IStateFilter.ST_LINKED;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_ALL = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return true;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_NOTEXISTS = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return state == IStateFilter.ST_NOTEXISTS || state == IStateFilter.ST_LINKED;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_OBSTRUCTED = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return state == IStateFilter.ST_OBSTRUCTED;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_VALID = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return state != IStateFilter.ST_OBSTRUCTED && state != IStateFilter.ST_LINKED;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return this.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_REPLACED = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return state == IStateFilter.ST_REPLACED;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_PREREPLACED = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return state == IStateFilter.ST_PREREPLACED;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_PREREPLACEDREPLACED = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_REPLACED;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_IGNORED = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return state == IStateFilter.ST_IGNORED;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_UNVERSIONED = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_NEW || 
				state == IStateFilter.ST_IGNORED || state == IStateFilter.ST_NOTEXISTS;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_VERSIONED = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_REPLACED || state == IStateFilter.ST_PREREPLACED ||
				state == IStateFilter.ST_ADDED || state == IStateFilter.ST_NORMAL || 
				state == IStateFilter.ST_MODIFIED || state == IStateFilter.ST_CONFLICTING || state == IStateFilter.ST_DELETED || state == IStateFilter.ST_MISSING;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return this.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_NOTONREPOSITORY = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_NEW || 
				state == IStateFilter.ST_IGNORED || state == IStateFilter.ST_NOTEXISTS ||
				state == IStateFilter.ST_ADDED;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_ONREPOSITORY = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_REPLACED || 
				state == IStateFilter.ST_NORMAL || state == IStateFilter.ST_MODIFIED || 
				state == IStateFilter.ST_CONFLICTING || state == IStateFilter.ST_DELETED || state == IStateFilter.ST_MISSING;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_NEW = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_NEW;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return state != IStateFilter.ST_IGNORED && state != IStateFilter.ST_OBSTRUCTED && state != IStateFilter.ST_LINKED;
		}
	};
	
	public static final IStateFilter SF_ADDED = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_REPLACED || 
				state == IStateFilter.ST_NEW || state == IStateFilter.ST_ADDED;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_NOTMODIFIED = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return state == IStateFilter.ST_NORMAL || state == IStateFilter.ST_NOTEXISTS || state == IStateFilter.ST_LINKED;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return true;
		}
	};
	
	public static final IStateFilter SF_MODIFIED = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return state == IStateFilter.ST_MODIFIED || state == IStateFilter.ST_CONFLICTING;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_CONFLICTING = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return state == IStateFilter.ST_CONFLICTING;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_DELETED = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_REPLACED || state == IStateFilter.ST_DELETED || state == IStateFilter.ST_MISSING;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_MISSING = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return state == IStateFilter.ST_MISSING;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_COMMITABLE = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_REPLACED || state == IStateFilter.ST_ADDED || 
				state == IStateFilter.ST_MODIFIED || state == IStateFilter.ST_DELETED || state == IStateFilter.ST_MISSING;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_REVERTABLE = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_CONFLICTING || 
				state == IStateFilter.ST_REPLACED || state == IStateFilter.ST_ADDED || 
				state == IStateFilter.ST_MODIFIED || state == IStateFilter.ST_DELETED || state == IStateFilter.ST_MISSING;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_ANY_CHANGE = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return 
				state != IStateFilter.ST_IGNORED && state != IStateFilter.ST_NORMAL && 
				state != IStateFilter.ST_OBSTRUCTED && state != IStateFilter.ST_LINKED;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return state != IStateFilter.ST_IGNORED && state != IStateFilter.ST_OBSTRUCTED && state != IStateFilter.ST_LINKED;
		}
	};

	public static final IStateFilter SF_EXCLUDE_DELETED = new IStateFilter() {
        public boolean accept(IResource resource, String state, int mask) {
        	if (IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask)) {
                return state != IStateFilter.ST_DELETED && state != IStateFilter.ST_MISSING;
            }
            return false;
        }
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return this.accept(resource, state, mask);
		}
    };
    
    public static final IStateFilter SF_NEEDS_LOCK = new IStateFilter() {		
		public boolean accept(final IResource resource, String state, int mask) {
			if (!(resource instanceof IFile) || IStateFilter.SF_UNVERSIONED.accept(resource, state, mask) || !resource.isAccessible()) {
				return false;
			}
			final PropertyData [][]propData = new PropertyData[1][];
			IActionOperation op = new AbstractNonLockingOperation("Operation.CheckProperty") {
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(resource);
					ISVNClientWrapper proxy = location.acquireSVNProxy();
					try {
						propData[0] = SVNUtility.properties(proxy, new EntryRevisionReference(FileUtility.getWorkingCopyPath(resource), null, Revision.BASE), new SVNProgressMonitor(this, monitor, null));
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
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_MODIFIED_NOT_IGNORED = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return !IStateFilter.SF_IGNORED.accept(resource, state, mask) &&
					!IStateFilter.SF_NOTMODIFIED.accept(resource, state, mask);
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return state != IStateFilter.ST_IGNORED && state != IStateFilter.ST_OBSTRUCTED && state != IStateFilter.ST_LINKED;
		}
	};
	
	public static final IStateFilter SF_EXCLUDE_PREREPLACED_AND_DELETED = new IStateFilter() {
        public boolean accept(IResource resource, String state, int mask) {
        	if (IStateFilter.SF_VERSIONED.accept(resource, state, mask) &&
                !IStateFilter.SF_PREREPLACED.accept(resource, state, mask)) {
                return state != IStateFilter.ST_DELETED && state != IStateFilter.ST_MISSING;
            }
            return false;
        }
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_EXCLUDE_DELETED.accept(resource, state, mask);
		}
    };
	
	public static final IStateFilter SF_EXCLUDE_PREREPLACED_AND_DELETED_FILES = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			if (resource instanceof IFile) {
				return IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED.accept(resource, state, mask);
			}
			return false;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_EXCLUDE_DELETED.accept(resource, state, mask);
		}
	};
	
	public static final IStateFilter SF_VERSIONED_FOLDERS = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			if (resource instanceof IContainer) {
				return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
			}
			return false;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_VERSIONED.accept(resource, state, mask);
		}
	};

}
