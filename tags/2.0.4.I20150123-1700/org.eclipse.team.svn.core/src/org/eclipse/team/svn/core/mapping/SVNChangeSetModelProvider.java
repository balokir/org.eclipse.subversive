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

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.LoggedOperation;

public class SVNChangeSetModelProvider extends ModelProvider {

	public static final String ID = "org.eclipse.team.svn.core.svnChangeSetModel"; //$NON-NLS-1$
	private static SVNChangeSetModelProvider provider;
	
	public SVNChangeSetModelProvider() {
		super();
	}
	
	public static SVNChangeSetModelProvider getProvider() {
		if (SVNChangeSetModelProvider.provider == null) {
			try {
				SVNChangeSetModelProvider.provider = (SVNChangeSetModelProvider)ModelProvider.getModelProviderDescriptor(SVNChangeSetModelProvider.ID).getModelProvider();
			} catch (CoreException e) {
				LoggedOperation.reportError(SVNTeamPlugin.NATURE_ID, e);
			}
		}
		return SVNChangeSetModelProvider.provider;
	}
}
