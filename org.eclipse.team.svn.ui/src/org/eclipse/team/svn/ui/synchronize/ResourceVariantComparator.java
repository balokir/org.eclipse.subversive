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

package org.eclipse.team.svn.ui.synchronize;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.synchronize.variant.ResourceVariant;

/**
 * ResourceVariant comparator
 * 
 * @author Alexander Gurov
 */
public class ResourceVariantComparator implements IResourceVariantComparator {

	public boolean compare(IResource local, IResourceVariant remote) {
		if (local == null && remote == null) {
			return true;
		}
		if (local == null || remote == null) {
			return false;
		}
		ILocalResource resource = SVNRemoteStorage.instance().asLocalResource(local);
		return resource == null ? false : resource.getRevision() == ((ResourceVariant)remote).getResource().getRevision();
	}

	public boolean compare(IResourceVariant base, IResourceVariant remote) {
		if (base == remote) {
			return true;
		}
		if (base == null || remote == null) {
		    return false;
		}
		return base.getContentIdentifier().equals(remote.getContentIdentifier());
	}

	public boolean isThreeWay() {
		return true;
	}

}
