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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.resource.ILocalResource;

/**
 * Remote folder emulator variant
 * 
 * @author Alexander Gurov
 */
public class VirtualRemoteFolderVariant extends VirtualRemoteResourceVariant {

	public VirtualRemoteFolderVariant(ILocalResource local) {
		super(local);
	}

	protected void fetchContents(IProgressMonitor monitor) throws TeamException {

	}

	public boolean isContainer() {
		return true;
	}

}
