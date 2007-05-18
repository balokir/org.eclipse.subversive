/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexey Mikoyan - Initial implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.decorator;

/**
 * Implementors of this interface will provide variables that can be used
 * for various decoration purposes
 *
 * @author Alexey Mikoyan
 *
 */
public interface IVariableSetProvider {
	public IVariable getVariable(String name);
	public IVariable getCenterVariable();
	public String getDomainName();
}
