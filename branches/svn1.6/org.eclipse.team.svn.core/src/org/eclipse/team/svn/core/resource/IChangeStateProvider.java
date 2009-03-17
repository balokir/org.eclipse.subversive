/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.resource;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.connector.SVNRevision;

/**
 * Allow us to generate ILocalResource-based changes independently from real information provider structure.
 * 
 * @author Alexander Gurov
 */
public interface IChangeStateProvider {
	public String getLocalPath();
	public int getNodeKind();
	public int getPropertiesChangeType();
	public int getTextChangeType();
	public SVNRevision.Number getChangeRevision();
	public String getChangeAuthor();
	public String getComment();
	public long getChangeDate();
	public boolean isCopied();
	public boolean isSwitched();
	public IResource getExact(IResource []set);
}
