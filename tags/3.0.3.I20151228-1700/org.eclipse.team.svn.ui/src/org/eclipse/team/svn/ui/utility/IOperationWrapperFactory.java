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
import org.eclipse.team.svn.core.utility.ILoggedOperationFactory;

/**
 * Executable operation wrapper factory provide ability to change default IActionOperation behaviour corresponding to some rules
 * 
 * @author Alexander Gurov
 */
public interface IOperationWrapperFactory extends ILoggedOperationFactory {
	public ICancellableOperationWrapper getCancellable(IActionOperation operation);
}
