/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.mylyn;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.svn.core.mapping.SVNChangeSetResourceMapping;

public class SVNChangeSetAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("unchecked")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ActiveChangeSet && adapterType == ResourceMapping.class) {
			ActiveChangeSet cs = (ActiveChangeSet) adaptableObject;
			return new SVNChangeSetResourceMapping(cs);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Class[] getAdapterList() {
		return new Class[] { ResourceMapping.class };
	}

}