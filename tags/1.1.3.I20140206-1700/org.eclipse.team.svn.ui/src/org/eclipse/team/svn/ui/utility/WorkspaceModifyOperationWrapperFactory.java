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

package org.eclipse.team.svn.ui.utility;

import org.eclipse.team.svn.core.operation.IActionOperation;

/**
 * WorkspaceModify + LoggedOperation factory
 * 
 * @author Alexander Gurov
 */
public class WorkspaceModifyOperationWrapperFactory extends UILoggedOperationFactory implements IOperationWrapperFactory {
	public ICancellableOperationWrapper getCancellable(IActionOperation operation) {
		return new WorkspaceModifyCancellableOperationWrapper(operation);
	}

}
