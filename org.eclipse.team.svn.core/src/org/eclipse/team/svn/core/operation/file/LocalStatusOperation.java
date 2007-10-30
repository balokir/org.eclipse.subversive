/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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

/**
 * Working copy resources status
 * 
 * @author Alexander Gurov
 */
public class LocalStatusOperation extends AbstractStatusOperation {
	public LocalStatusOperation(File []files, boolean recursive) {
		super("Operation.LocalStatusFile", files, recursive);
	}

	public LocalStatusOperation(IFileProvider provider, boolean recursive) {
		super("Operation.LocalStatusFile", provider, recursive);
	}

	protected boolean isRemote() {
		return false;
	}

}
