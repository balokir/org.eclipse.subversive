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

package org.eclipse.team.svn.core.utility;

import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;

/**
 * Implementation of the interface should wrap any input operation with log writer operation
 * 
 * @author Alexander Gurov
 */
public interface ILoggedOperationFactory {
	public static final ILoggedOperationFactory EMPTY = new ILoggedOperationFactory() {
		public IActionOperation getLogged(IActionOperation operation) {
			return operation;
		}
	};
	
	public static final ILoggedOperationFactory DEFAULT = new ILoggedOperationFactory() {
		public IActionOperation getLogged(IActionOperation operation) {
			return new LoggedOperation(operation);
		}
	};
	
	public IActionOperation getLogged(IActionOperation operation);
}
