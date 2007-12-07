/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;

/**
 * Copy repository resource URL action
 *
 * @author Sergiy Logvin
 */
public class CopyUrlAction extends AbstractRepositoryTeamAction {
	
	protected String url;

	public CopyUrlAction() {
		super();
	}

	public void runImpl(IAction action) {
		IActionOperation op = new AbstractActionOperation("Operation.CopyURL") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				Clipboard clipboard = new Clipboard(CopyUrlAction.this.getShell().getDisplay());
				try {
					clipboard.setContents(
						new Object[] {CopyUrlAction.this.url}, 
						new Transfer[] {TextTransfer.getInstance()});
				}
				finally {
					clipboard.dispose();
				}
			}
		};
		this.runBusy(op);
	}
	
	public boolean isEnabled() {
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		IRepositoryLocation []locations = this.getSelectedRepositoryLocations();
		if (resources.length == 0 && locations.length == 1) {
			this.url = locations[0].getUrl();
			return true;
		}
		else if (resources.length == 1) {
			this.url = resources[0].getUrl();
			return true;
		}
		
		return false;
	}

}
