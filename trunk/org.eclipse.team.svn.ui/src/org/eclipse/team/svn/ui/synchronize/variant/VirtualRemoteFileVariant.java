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

import java.io.ByteArrayInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.GetLocalFileContentOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Remote file emulator variant
 * 
 * @author Alexander Gurov
 */
public class VirtualRemoteFileVariant extends VirtualRemoteResourceVariant {

	public VirtualRemoteFileVariant(ILocalResource local) {
		super(local);
	}

	protected void fetchContents(IProgressMonitor monitor) throws TeamException {
		if (!this.local.isCopied() && this.local.getRevision() == SVNRevision.INVALID_REVISION_NUMBER &&
			!IStateFilter.SF_PREREPLACED.accept(this.local)) {
			this.setContents(new ByteArrayInputStream(new byte[0]), monitor);
			return;
		}
		GetLocalFileContentOperation op = new GetLocalFileContentOperation(this.local.getResource(), Kind.BASE);
		ProgressMonitorUtility.doTaskExternal(op, monitor);
		if (op.getExecutionState() == IActionOperation.OK) {
			this.setContents(op.getContent(), monitor);
		}
	}

	public boolean isContainer() {
		return false;
	}

}
