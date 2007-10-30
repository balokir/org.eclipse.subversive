/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.decorator;

/**
 * User defined decoration variable 
 * 
 * @author Alexander Gurov
 */
public class UserVariable extends PredefinedVariable {
	public UserVariable(String domain, String data) {
		super(domain, data);
	}
	
	public String getDescription() {
		return this.getDescription("_user_defined_data");
	}
	
}
