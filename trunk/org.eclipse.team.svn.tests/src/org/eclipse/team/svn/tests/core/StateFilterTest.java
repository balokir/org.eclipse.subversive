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

package org.eclipse.team.svn.tests.core;

import org.eclipse.team.svn.core.IStateFilter;

import junit.framework.TestCase;

/**
 * IStateFilter behaviour test
 * 
 * @author Alexander Gurov
 */
public class StateFilterTest extends TestCase {
	
	public void testSF_ADDED() {
		if (!IStateFilter.SF_ADDED.accept(null, IStateFilter.ST_ADDED, 0) ||
			IStateFilter.SF_ADDED.accept(null, IStateFilter.ST_DELETED, 0) ||
			IStateFilter.SF_ADDED.accept(null, IStateFilter.ST_MISSING, 0) ||
			IStateFilter.SF_ADDED.accept(null, IStateFilter.ST_MODIFIED, 0) ||
			!IStateFilter.SF_ADDED.accept(null, IStateFilter.ST_NEW, 0) ||
			IStateFilter.SF_ADDED.accept(null, IStateFilter.ST_IGNORED, 0) ||
			IStateFilter.SF_ADDED.accept(null, IStateFilter.ST_NORMAL, 0) ||
			IStateFilter.SF_ADDED.accept(null, IStateFilter.ST_NOTEXISTS, 0)
			) {
			throw new RuntimeException(IStateFilter.SF_ADDED.getClass().getName() + " failed");
		}
	}
	
	public void testSF_ALL() {
		if (!IStateFilter.SF_ALL.accept(null, IStateFilter.ST_ADDED, 0) ||
			!IStateFilter.SF_ALL.accept(null, IStateFilter.ST_DELETED, 0) ||
			!IStateFilter.SF_ALL.accept(null, IStateFilter.ST_MISSING, 0) ||
			!IStateFilter.SF_ALL.accept(null, IStateFilter.ST_MODIFIED, 0) ||
			!IStateFilter.SF_ALL.accept(null, IStateFilter.ST_NEW, 0) ||
			!IStateFilter.SF_ALL.accept(null, IStateFilter.ST_IGNORED, 0) ||
			!IStateFilter.SF_ALL.accept(null, IStateFilter.ST_NORMAL, 0) ||
			!IStateFilter.SF_ALL.accept(null, IStateFilter.ST_NOTEXISTS, 0)
			) {
			throw new RuntimeException(IStateFilter.SF_ALL.getClass().getName() + " failed");
		}
	}
	
	public void testSF_ANY_CHANGE() {
		if (!IStateFilter.SF_ANY_CHANGE.accept(null, IStateFilter.ST_ADDED, 0) ||
			!IStateFilter.SF_ANY_CHANGE.accept(null, IStateFilter.ST_DELETED, 0) ||
			!IStateFilter.SF_ANY_CHANGE.accept(null, IStateFilter.ST_MISSING, 0) ||
			!IStateFilter.SF_ANY_CHANGE.accept(null, IStateFilter.ST_MODIFIED, 0) ||
			!IStateFilter.SF_ANY_CHANGE.accept(null, IStateFilter.ST_NEW, 0) ||
			IStateFilter.SF_ANY_CHANGE.accept(null, IStateFilter.ST_IGNORED, 0) ||
			IStateFilter.SF_ANY_CHANGE.accept(null, IStateFilter.ST_NORMAL, 0) ||
			!IStateFilter.SF_ANY_CHANGE.accept(null, IStateFilter.ST_NOTEXISTS, 0)
			) {
			throw new RuntimeException(IStateFilter.SF_ANY_CHANGE.getClass().getName() + " failed");
		}
	}
	
	public void testSF_COMMITABLE() {
		if (!IStateFilter.SF_COMMITABLE.accept(null, IStateFilter.ST_ADDED, 0) ||
			!IStateFilter.SF_COMMITABLE.accept(null, IStateFilter.ST_DELETED, 0) ||
			!IStateFilter.SF_COMMITABLE.accept(null, IStateFilter.ST_MISSING, 0) ||
			!IStateFilter.SF_COMMITABLE.accept(null, IStateFilter.ST_MODIFIED, 0) ||
			IStateFilter.SF_COMMITABLE.accept(null, IStateFilter.ST_NEW, 0) ||
			IStateFilter.SF_COMMITABLE.accept(null, IStateFilter.ST_IGNORED, 0) ||
			IStateFilter.SF_COMMITABLE.accept(null, IStateFilter.ST_NORMAL, 0) ||
			IStateFilter.SF_COMMITABLE.accept(null, IStateFilter.ST_NOTEXISTS, 0)
			) {
			throw new RuntimeException(IStateFilter.SF_COMMITABLE.getClass().getName() + " failed");
		}
	}
	
	public void testSF_DELETED() {
		if (IStateFilter.SF_DELETED.accept(null, IStateFilter.ST_ADDED, 0) ||
			!IStateFilter.SF_DELETED.accept(null, IStateFilter.ST_DELETED, 0) ||
			!IStateFilter.SF_DELETED.accept(null, IStateFilter.ST_MISSING, 0) ||
			IStateFilter.SF_DELETED.accept(null, IStateFilter.ST_MODIFIED, 0) ||
			IStateFilter.SF_DELETED.accept(null, IStateFilter.ST_NEW, 0) ||
			IStateFilter.SF_DELETED.accept(null, IStateFilter.ST_IGNORED, 0) ||
			IStateFilter.SF_DELETED.accept(null, IStateFilter.ST_NORMAL, 0) ||
			IStateFilter.SF_DELETED.accept(null, IStateFilter.ST_NOTEXISTS, 0)
			) {
			throw new RuntimeException(IStateFilter.SF_DELETED.getClass().getName() + " failed");
		}
	}
	
	public void testSF_IGNORED() {
		if (IStateFilter.SF_IGNORED.accept(null, IStateFilter.ST_ADDED, 0) ||
			IStateFilter.SF_IGNORED.accept(null, IStateFilter.ST_DELETED, 0) ||
			IStateFilter.SF_IGNORED.accept(null, IStateFilter.ST_MISSING, 0) ||
			IStateFilter.SF_IGNORED.accept(null, IStateFilter.ST_MODIFIED, 0) ||
			IStateFilter.SF_IGNORED.accept(null, IStateFilter.ST_NEW, 0) ||
			!IStateFilter.SF_IGNORED.accept(null, IStateFilter.ST_IGNORED, 0) ||
			IStateFilter.SF_IGNORED.accept(null, IStateFilter.ST_NORMAL, 0) ||
			IStateFilter.SF_IGNORED.accept(null, IStateFilter.ST_NOTEXISTS, 0)
			) {
			throw new RuntimeException(IStateFilter.SF_IGNORED.getClass().getName() + " failed");
		}
	}
	
	public void testSF_MODIFIED() {
		if (IStateFilter.SF_MODIFIED.accept(null, IStateFilter.ST_ADDED, 0) ||
			IStateFilter.SF_MODIFIED.accept(null, IStateFilter.ST_DELETED, 0) ||
			IStateFilter.SF_MODIFIED.accept(null, IStateFilter.ST_MISSING, 0) ||
			!IStateFilter.SF_MODIFIED.accept(null, IStateFilter.ST_MODIFIED, 0) ||
			IStateFilter.SF_MODIFIED.accept(null, IStateFilter.ST_NEW, 0) ||
			IStateFilter.SF_MODIFIED.accept(null, IStateFilter.ST_IGNORED, 0) ||
			IStateFilter.SF_MODIFIED.accept(null, IStateFilter.ST_NORMAL, 0) ||
			IStateFilter.SF_MODIFIED.accept(null, IStateFilter.ST_NOTEXISTS, 0)
			) {
			throw new RuntimeException(IStateFilter.SF_MODIFIED.getClass().getName() + " failed");
		}
	}
	
	public void testSF_NEW() {
		if (IStateFilter.SF_NEW.accept(null, IStateFilter.ST_ADDED, 0) ||
			IStateFilter.SF_NEW.accept(null, IStateFilter.ST_DELETED, 0) ||
			IStateFilter.SF_NEW.accept(null, IStateFilter.ST_MISSING, 0) ||
			IStateFilter.SF_NEW.accept(null, IStateFilter.ST_MODIFIED, 0) ||
			!IStateFilter.SF_NEW.accept(null, IStateFilter.ST_NEW, 0) ||
			IStateFilter.SF_NEW.accept(null, IStateFilter.ST_IGNORED, 0) ||
			IStateFilter.SF_NEW.accept(null, IStateFilter.ST_NORMAL, 0) ||
			IStateFilter.SF_NEW.accept(null, IStateFilter.ST_NOTEXISTS, 0)
			) {
			throw new RuntimeException(IStateFilter.SF_NEW.getClass().getName() + " failed");
		}
	}
	
	public void testSF_NONVERSIONED() {
		if (IStateFilter.SF_UNVERSIONED.accept(null, IStateFilter.ST_ADDED, 0) ||
			IStateFilter.SF_UNVERSIONED.accept(null, IStateFilter.ST_DELETED, 0) ||
			IStateFilter.SF_UNVERSIONED.accept(null, IStateFilter.ST_MISSING, 0) ||
			IStateFilter.SF_UNVERSIONED.accept(null, IStateFilter.ST_MODIFIED, 0) ||
			!IStateFilter.SF_UNVERSIONED.accept(null, IStateFilter.ST_NEW, 0) ||
			!IStateFilter.SF_UNVERSIONED.accept(null, IStateFilter.ST_IGNORED, 0) ||
			IStateFilter.SF_UNVERSIONED.accept(null, IStateFilter.ST_NORMAL, 0) ||
			!IStateFilter.SF_UNVERSIONED.accept(null, IStateFilter.ST_NOTEXISTS, 0)
			) {
			throw new RuntimeException(IStateFilter.SF_UNVERSIONED.getClass().getName() + " failed");
		}
	}
	
	public void testSF_NOTEXISTS() {
		if (IStateFilter.SF_NOTEXISTS.accept(null, IStateFilter.ST_ADDED, 0) ||
			IStateFilter.SF_NOTEXISTS.accept(null, IStateFilter.ST_DELETED, 0) ||
			IStateFilter.SF_NOTEXISTS.accept(null, IStateFilter.ST_MISSING, 0) ||
			IStateFilter.SF_NOTEXISTS.accept(null, IStateFilter.ST_MODIFIED, 0) ||
			IStateFilter.SF_NOTEXISTS.accept(null, IStateFilter.ST_NEW, 0) ||
			IStateFilter.SF_NOTEXISTS.accept(null, IStateFilter.ST_IGNORED, 0) ||
			IStateFilter.SF_NOTEXISTS.accept(null, IStateFilter.ST_NORMAL, 0) ||
			!IStateFilter.SF_NOTEXISTS.accept(null, IStateFilter.ST_NOTEXISTS, 0)
			) {
			throw new RuntimeException(IStateFilter.SF_NOTEXISTS.getClass().getName() + " failed");
		}
	}
	
	public void testSF_NOTMODIFIED() {
		if (IStateFilter.SF_NOTMODIFIED.accept(null, IStateFilter.ST_ADDED, 0) ||
			IStateFilter.SF_NOTMODIFIED.accept(null, IStateFilter.ST_DELETED, 0) ||
			IStateFilter.SF_NOTMODIFIED.accept(null, IStateFilter.ST_MISSING, 0) ||
			IStateFilter.SF_NOTMODIFIED.accept(null, IStateFilter.ST_MODIFIED, 0) ||
			IStateFilter.SF_NOTMODIFIED.accept(null, IStateFilter.ST_NEW, 0) ||
			IStateFilter.SF_NOTMODIFIED.accept(null, IStateFilter.ST_IGNORED, 0) ||
			!IStateFilter.SF_NOTMODIFIED.accept(null, IStateFilter.ST_NORMAL, 0) ||
			!IStateFilter.SF_NOTMODIFIED.accept(null, IStateFilter.ST_NOTEXISTS, 0)
			) {
			throw new RuntimeException(IStateFilter.SF_NOTMODIFIED.getClass().getName() + " failed");
		}
	}
	
	public void testSF_ONREPOSITORY() {
		if (IStateFilter.SF_ONREPOSITORY.accept(null, IStateFilter.ST_ADDED, 0) ||
			!IStateFilter.SF_ONREPOSITORY.accept(null, IStateFilter.ST_DELETED, 0) ||
			!IStateFilter.SF_ONREPOSITORY.accept(null, IStateFilter.ST_MISSING, 0) ||
			!IStateFilter.SF_ONREPOSITORY.accept(null, IStateFilter.ST_MODIFIED, 0) ||
			IStateFilter.SF_ONREPOSITORY.accept(null, IStateFilter.ST_NEW, 0) ||
			IStateFilter.SF_ONREPOSITORY.accept(null, IStateFilter.ST_IGNORED, 0) ||
			!IStateFilter.SF_ONREPOSITORY.accept(null, IStateFilter.ST_NORMAL, 0) ||
			IStateFilter.SF_ONREPOSITORY.accept(null, IStateFilter.ST_NOTEXISTS, 0)
			) {
			throw new RuntimeException(IStateFilter.SF_ONREPOSITORY.getClass().getName() + " failed");
		}
	}
	
	public void testSF_VERSIONED() {
		if (!IStateFilter.SF_VERSIONED.accept(null, IStateFilter.ST_ADDED, 0) ||
			!IStateFilter.SF_VERSIONED.accept(null, IStateFilter.ST_DELETED, 0) ||
			!IStateFilter.SF_VERSIONED.accept(null, IStateFilter.ST_MISSING, 0) ||
			!IStateFilter.SF_VERSIONED.accept(null, IStateFilter.ST_MODIFIED, 0) ||
			IStateFilter.SF_VERSIONED.accept(null, IStateFilter.ST_NEW, 0) ||
			IStateFilter.SF_VERSIONED.accept(null, IStateFilter.ST_IGNORED, 0) ||
			!IStateFilter.SF_VERSIONED.accept(null, IStateFilter.ST_NORMAL, 0) ||
			IStateFilter.SF_VERSIONED.accept(null, IStateFilter.ST_NOTEXISTS, 0)
			) {
			throw new RuntimeException(IStateFilter.SF_VERSIONED.getClass().getName() + " failed");
		}
	}
	
}
