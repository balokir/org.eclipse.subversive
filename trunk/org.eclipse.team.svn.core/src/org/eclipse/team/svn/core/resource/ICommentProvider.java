/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.resource;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.client.SVNRevision;

/**
 * Deffered in time comment acquisition: SVN does not provide comment while synchronizing with repository
 * 
 * @author Alexander Gurov
 */
public interface ICommentProvider {
	public String getComment(IResource resource, SVNRevision rev, SVNRevision peg);
}
