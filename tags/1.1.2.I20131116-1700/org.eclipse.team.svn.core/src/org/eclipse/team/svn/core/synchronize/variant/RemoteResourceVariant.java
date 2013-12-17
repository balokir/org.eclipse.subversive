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

package org.eclipse.team.svn.core.synchronize.variant;

import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.resource.ILocalResource;

/**
 * Abstract remote resource variant
 * 
 * @author Alexander Gurov
 */
public abstract class RemoteResourceVariant extends ResourceVariant {
	protected static String svnAuthor;

	public RemoteResourceVariant(ILocalResource local) {
		super(local);
		RemoteResourceVariant.svnAuthor = SVNMessages.SVNInfo_Author;
	}

	protected String getCacheId() {
		return "Remote: " + super.getCacheId();
	}
	
    public String getContentIdentifier() {
        if (IStateFilter.SF_PREREPLACED.accept(this.local)) {
        	return "";
        }
    	String retVal = super.getContentIdentifier();
	    if ((!this.isNotOnRepository() || this.local.isCopied()) && this.local.getAuthor() != null) {
	        retVal += " " + BaseMessages.format(RemoteResourceVariant.svnAuthor, new Object[] {this.local.getAuthor()});
	    }
        return retVal;
    }
    
}
