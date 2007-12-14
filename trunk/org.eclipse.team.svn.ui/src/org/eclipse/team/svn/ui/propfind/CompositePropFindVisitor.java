/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.propfind;

import org.eclipse.team.svn.core.connector.SVNProperty;

/**
 * Visitor combines another visitors implementing  IPropFindVisitors 
 * 
 * @author Alexei Goncharov
 */
public class CompositePropFindVisitor implements IPropFindVisitor {
	protected IPropFindVisitor [] visitorsList;
	
	public CompositePropFindVisitor(IPropFindVisitor []visitorsListParam) {
		this.visitorsList = visitorsListParam;
	}
	
	public boolean visit(SVNProperty propertyParam) {
		boolean retVal = false;
		for (int i = 0; i < this.visitorsList.length; i++) {
			retVal |= visitorsList[i].visit(propertyParam);
		}
		return retVal;
	}

}
