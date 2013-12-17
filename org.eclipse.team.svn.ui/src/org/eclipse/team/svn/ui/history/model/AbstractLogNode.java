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

package org.eclipse.team.svn.ui.history.model;

import org.eclipse.team.svn.core.utility.PatternProvider;

/**
 * Abstract implementation of ILogNode
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractLogNode implements ILogNode {
	protected ILogNode parent;

	public AbstractLogNode(ILogNode parent) {
		this.parent = parent;
	}

	public ILogNode getParent() {
		return this.parent;
	}

	protected static String flattenMultiLineText(String input, String lineSeparatorReplacement) {
		String retVal = PatternProvider.replaceAll(input, "\r\n", lineSeparatorReplacement); //$NON-NLS-1$
		retVal = PatternProvider.replaceAll(retVal, "\n", lineSeparatorReplacement); //$NON-NLS-1$
		retVal = PatternProvider.replaceAll(retVal, "\r", lineSeparatorReplacement); //$NON-NLS-1$
		return retVal;
	}
	
}
