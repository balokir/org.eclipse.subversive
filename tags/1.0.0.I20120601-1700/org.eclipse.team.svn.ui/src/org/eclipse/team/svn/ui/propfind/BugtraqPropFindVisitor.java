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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.ui.properties.bugtraq.BugtraqModel;

/**
 * Visitor class, implementing the Bugtraq property search.
 * Constructs proper Bugtraq model.
 * 
 * @author Alexei Goncharov
 */
public class BugtraqPropFindVisitor implements IPropFindVisitor {
	protected BugtraqModel model;
	protected Set<String> bugtraqProperties;
	
	public BugtraqPropFindVisitor() {
		this.model = new BugtraqModel();
		this.bugtraqProperties = new HashSet<String>(
				Arrays.asList(new String[] {"bugtraq:url", //$NON-NLS-1$
											 "bugtraq:logregex", //$NON-NLS-1$
											 "bugtraq:label", //$NON-NLS-1$
											 "bugtraq:message", //$NON-NLS-1$
											 "bugtraq:number", //$NON-NLS-1$
											 "bugtraq:warnifnoissue", //$NON-NLS-1$
											 "bugtraq:append"})); //$NON-NLS-1$
	}
	
	public boolean visit(SVNProperty propertyParam) {
		if (this.bugtraqProperties.contains(propertyParam.name)) {
			this.processBugtraqProperty(propertyParam.name, propertyParam.value);
			this.bugtraqProperties.remove(propertyParam.name);
		}
		return true;
	}
	
	protected void processBugtraqProperty(String name, String value) {
		if (name.equals("bugtraq:url")) { //$NON-NLS-1$
			this.model.setUrl(value);
		}
		else if (name.equals("bugtraq:logregex")) { //$NON-NLS-1$
			this.model.setLogregex(value);
		}
		else if (name.equals("bugtraq:label")) { //$NON-NLS-1$
			this.model.setLabel(value);
		}
		else if (name.equals("bugtraq:message")) { //$NON-NLS-1$
			this.model.setMessage(value);
		}
		else if (name.equals("bugtraq:number")) { //$NON-NLS-1$
			boolean number = value == null || !(value.trim().equals("false") || value.trim().equals("no")); //$NON-NLS-1$ //$NON-NLS-2$
			this.model.setNumber(number);
		}
		else if (name.equals("bugtraq:warnifnoissue")) { //$NON-NLS-1$
			boolean warn = value != null && (value.trim().equals("yes") || value.trim().equals("true")); //$NON-NLS-1$ //$NON-NLS-2$
			this.model.setWarnIfNoIssue(warn);
		}
		else if (name.equals("bugtraq:append")) { //$NON-NLS-1$
			boolean append = value == null || !(value.trim().equals("false") || value.trim().equals("no")); //$NON-NLS-1$ //$NON-NLS-2$
			this.model.setAppend(append);
		}
	}
	
	public BugtraqModel getBugtraqModel() {
		return this.model;
	}

}
