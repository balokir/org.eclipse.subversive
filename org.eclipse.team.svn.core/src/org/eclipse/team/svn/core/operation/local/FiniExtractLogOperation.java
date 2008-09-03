/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *    Igor Burilo - Bug 245509: Improve extract log
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;

/**
 * Performs finalization of extract operations log file.
 * 
 * @author Alexander Gurov
 */
public class FiniExtractLogOperation extends AbstractActionOperation {
	protected InitExtractLogOperation logger;
	
	public FiniExtractLogOperation(InitExtractLogOperation logger) {
		super("Operation.FiniExtractLog");
		this.logger = logger;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.logger.flushLog();
	}
	
}
