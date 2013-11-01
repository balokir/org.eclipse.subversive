/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.file;

import java.io.File;

import org.eclipse.team.svn.core.SVNMessages;

/**
 * Working copy resources status
 * 
 * @author Alexander Gurov
 */
public class LocalStatusOperation extends AbstractStatusOperation {
	public LocalStatusOperation(File []files, boolean recursive) {
		super("Operation_LocalStatusFile", SVNMessages.class, files, recursive); //$NON-NLS-1$
	}

	public LocalStatusOperation(IFileProvider provider, boolean recursive) {
		super("Operation_LocalStatusFile", SVNMessages.class, provider, recursive); //$NON-NLS-1$
	}

	protected boolean isRemote() {
		return false;
	}

}
