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

import java.util.HashSet;

import org.eclipse.team.svn.core.connector.SVNProperty;

/**
 *  Collects the LogTamplates values to a HashSet until its
 *  size less or equals the value of CommitPanel.MAXIMUM_LOG_TEMPLATE_SIZE
 *  static field.
 * 
 * @author Alexei Goncharov
 */
public class LogTemplatesPropFindVisitor implements IPropFindVisitor {
	// Restricting the size of log templates set in order to prevent prolonged commit operation execution.
	public static final int MAXIMUM_LOG_TEMPLATE_SIZE = 20;
	
	protected HashSet<String> logTemplates;
	
	public LogTemplatesPropFindVisitor() {
		this.logTemplates = new HashSet<String>();
	}
	
	public boolean visit(SVNProperty propertyParam) {
		if (this.logTemplates.size() == LogTemplatesPropFindVisitor.MAXIMUM_LOG_TEMPLATE_SIZE) {
			return false;
		}
		if (propertyParam.name.equals("tsvn:logtemplate")) { //$NON-NLS-1$
			this.logTemplates.add(propertyParam.value);
		}
		return true;
	}
	
	public HashSet<String> getLogTemplates() {
		return this.logTemplates;
	}

}
