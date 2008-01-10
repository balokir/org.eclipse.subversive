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
 * TODO insert type description here
 * 
 * @author Alexei Goncharov
 */
public class MinLockSizePropFindVisitor implements IPropFindVisitor {
	protected int minLockSize;
	
	public MinLockSizePropFindVisitor() {
		this.minLockSize = 0;
	}
	
	public boolean visit(SVNProperty propertyParam) {
		if (propertyParam.name.equals("tsvn:lockmsgminsize")) {
			try {
				int currMinSize = Integer.decode(propertyParam.value);
				if (this.minLockSize < currMinSize) {
					this.minLockSize = currMinSize;
				}
			}
			catch (NumberFormatException ex) {
				//we ignore the exception
			}
		}
		return true;
	}
	
	public int getMinLockSize() {
		return this.minLockSize;
	}

}
