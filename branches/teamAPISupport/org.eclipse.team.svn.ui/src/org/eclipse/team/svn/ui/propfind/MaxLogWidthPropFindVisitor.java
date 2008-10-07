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
 * Selects a maximum width of log message form a logWidthMarker properties.
 * 
 * @author Alexei Goncharov
 */
public class MaxLogWidthPropFindVisitor implements IPropFindVisitor {
	protected int width;
	
	public MaxLogWidthPropFindVisitor() {
		this.width = 0;
	}
	
	public boolean visit(SVNProperty propertyParam) {
		if (propertyParam.name.equals("tsvn:logwidthmarker")) {
			try {
				int currWidth = Integer.decode(propertyParam.value);
				if (this.width > currWidth || this.width == 0) {
					this.width = currWidth;
				}
			}
			catch (NumberFormatException ex) {
				//we ignore the exception
			}
		}
		return true;
	}
	
	public int getMaxLogWidth() {
		return this.width;
	}

}
