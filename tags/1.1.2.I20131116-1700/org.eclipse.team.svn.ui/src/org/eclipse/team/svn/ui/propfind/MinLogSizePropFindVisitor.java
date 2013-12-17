/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
 * Selects a minimum size of log message form a minLogSize properties.
 * 
 * @author Alexei Goncharov
 */
public class MinLogSizePropFindVisitor implements IPropFindVisitor {
	protected int minLogSize;
	
	public MinLogSizePropFindVisitor() {
		this.minLogSize = 0;
	}
	
	public boolean visit(SVNProperty propertyParam) {
		if (propertyParam.name.equals("tsvn:logminsize")) { //$NON-NLS-1$
			try {
				int currMinSize = Integer.decode(propertyParam.value);
				if (this.minLogSize < currMinSize) {
					this.minLogSize = currMinSize;
				}
			}
			catch (NumberFormatException ex) {
				//we ignore the exception
			}
		}
		return true;
	}
	
	public int getMinLogSize() {
		return this.minLogSize;
	}

}
