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

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.RepositoryResourceSelectionComposite;
import org.eclipse.team.svn.ui.panel.common.AbstractRepositoryResourceSelectionPanel;

/**
 * Replace With URL panel
 * 
 * @author Alexei Goncharov
 */
public class ReplaceWithUrlPanel extends AbstractRepositoryResourceSelectionPanel {

	public ReplaceWithUrlPanel(IRepositoryResource baseResource, long currentRevision) {
		super(baseResource, currentRevision, SVNUIMessages.ReplaceUrlPanel_Title, SVNUIMessages.ReplaceUrlPanel_Selection_Description, "replaceUrl", SVNUIMessages.ReplaceUrlPanel_Selection_Title, SVNUIMessages.ReplaceUrlPanel_Description, RepositoryResourceSelectionComposite.TEXT_BASE); //$NON-NLS-1$
    	this.defaultMessage = SVNUIMessages.ReplaceUrlPanel_Message;
	}
	
}
