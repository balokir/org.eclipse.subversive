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

package org.eclipse.team.svn.ui.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Compare input for comparison of local and remote SVN properties.
 * 
 * @author Alexei Goncharov
 */
public class ThreeWayPropertyCompareInput extends PropertyCompareInput {
	
	protected long baseRevisionNumber;

	public ThreeWayPropertyCompareInput(CompareConfiguration configuration,
										SVNEntryRevisionReference left,
										SVNEntryRevisionReference right,
										SVNEntryRevisionReference ancestor,
										IRepositoryLocation location,
										long baseRevisionNumber) {
		super(configuration, left, right, ancestor, location);
		this.baseRevisionNumber = baseRevisionNumber;
	}

	protected void fillMenu(IMenuManager manager, TreeSelection selection) {
		/*manager.add(new Action("Test!") {
			public void run() {
				
			}
		})*/;
	}
	
	public String getTitle() {
		//TODO process different resources names (needed???)
		return SVNTeamUIPlugin.instance().getResource("PropertyCompareInput.Title3",
													  new String []	{
													  this.left.path.substring(this.left.path.lastIndexOf("/")+1)
													  + " [" + this.getRevisionPart(this.left),
													  this.getRevisionPart(this.ancestor),
													  this.getRevisionPart(this.right)+ "] "
													  });
	}
	
	protected String getRevisionPart(SVNEntryRevisionReference reference) {
		if (reference.revision == SVNRevision.WORKING) {
			return SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.LocalSign");
		}
		else if (reference.revision == SVNRevision.BASE) {
			if (this.ancestor == null) {
				return SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.ResourceIsNotAvailable");
			}
			return SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.BaseSign", new String [] {String.valueOf(this.baseRevisionNumber)});
		}
		return SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.RevisionSign", new String [] {String.valueOf(reference.revision)});
	}

}
