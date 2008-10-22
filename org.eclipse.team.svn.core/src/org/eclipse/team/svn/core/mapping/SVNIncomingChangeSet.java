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

package org.eclipse.team.svn.core.mapping;

import java.util.Date;

import org.eclipse.team.internal.core.subscribers.DiffChangeSet;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;

public class SVNIncomingChangeSet extends DiffChangeSet {
	
	protected ILocalResource entry;
	
	public SVNIncomingChangeSet(ILocalResource incomingChange) {
		this.entry = incomingChange;
	}
	
	public String getAuthor() {
        return this.entry.getAuthor();
    }

    public Date getDate() {
        return new Date(this.entry.getLastCommitDate());
    }

    public String getComment() {
        if (this.entry instanceof IResourceChange) {
        	return ((IResourceChange)this.entry).getComment();
        }
        return "";
    }
}
