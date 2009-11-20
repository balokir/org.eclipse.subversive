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
 * Interface of the visitor finding a specified property for a resource 
 * 
 * @author Alexei Goncharov
 */
public interface IPropFindVisitor {

	public boolean visit(SVNProperty propertyParam);
	
}
