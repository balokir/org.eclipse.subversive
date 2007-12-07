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

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.RemoteResourceTransfer;
import org.eclipse.team.svn.ui.RemoteResourceTransferrable;

/**
 * Copy/Cut remote resources into clipboard operation implementation
 * 
 * @author Alexander Gurov
 */
public class PrepareRemoteResourcesTransferrableOperation extends AbstractActionOperation {
	protected IRepositoryResource []resources;
	protected Display display;
	protected int operation;

	public PrepareRemoteResourcesTransferrableOperation(IRepositoryResource []resources, int operation, Display display) {
		super("Operation.FillCopyPaste");
		this.resources = resources;
		this.display = display;
		this.operation = operation;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		Clipboard clipboard = new Clipboard(this.display);
		try {
			clipboard.setContents(
				new Object[] {new RemoteResourceTransferrable(this.resources, this.operation)}, 
				new Transfer[] {new RemoteResourceTransfer()});
		}
		finally {
			clipboard.dispose();
		}
	}
	
}
