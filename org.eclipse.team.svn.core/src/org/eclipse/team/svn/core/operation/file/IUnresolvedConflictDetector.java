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

package org.eclipse.team.svn.core.operation.file;

import java.io.File;

/**
 * Interface that enables us to detect unresolved conflict situation
 * 
 * @author Alexander Gurov
 */
public interface IUnresolvedConflictDetector {
    public boolean hasUnresolvedConflicts();
    public File []getUnprocessed();
    public File []getProcessed();
    public String getMessage();
}
