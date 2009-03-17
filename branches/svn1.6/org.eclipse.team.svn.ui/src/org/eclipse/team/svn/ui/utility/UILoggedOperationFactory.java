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

package org.eclipse.team.svn.ui.utility;

import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.ILoggedOperationFactory;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;

/**
 * UI logged operation factory
 * 
 * @author Alexander Gurov
 */
public class UILoggedOperationFactory implements ILoggedOperationFactory {
	public IActionOperation getLogged(IActionOperation operation) {
		IActionOperation retVal = this.wrappedOperation(operation);
		
		retVal.setConsoleStream(SVNTeamUIPlugin.instance().getConsoleStream());
		
		return retVal;
	}

	protected IActionOperation wrappedOperation(IActionOperation operation) {
		return new UILoggedOperation(operation);
	}
}
