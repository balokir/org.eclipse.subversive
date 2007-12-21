/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.extension.impl;

import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.extension.factory.IReportingDescriptor;

/**
 * Default IReportingDescriptor implementation
 *
 * @author Sergiy Logvin
 */

public class DefaultReportingDescriptor implements IReportingDescriptor {
	public String getEmailTo() {
		return "subversive-bugs@polarion.org";
	}
	
	public String getEmailFrom() {
		return "subversive-bugs@polarion.org";
	}

	public String getHost() {
		return "mail.polarion.cz";
	}

	public String getPort() {
		return "25";
	}	

	public String getProductName() {
		return "Subversive";
	}

	public String getProductVersion() {
		return SVNTeamUIPlugin.instance().getVersionString();
	}

	public String getTrackerUrl() {
		return "https://bugs.eclipse.org/bugs";
	}
	
	public boolean isTrackerSupportsHTML() {
		return false;
	}

}
