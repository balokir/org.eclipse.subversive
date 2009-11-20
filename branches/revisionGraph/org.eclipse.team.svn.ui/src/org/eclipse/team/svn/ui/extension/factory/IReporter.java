/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.extension.factory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.svn.core.operation.IActionOperation;

/**
 * Generic issue reporter interface
 * 
 * @author Alexander Gurov
 */
public interface IReporter extends IActionOperation {
	public IReportingDescriptor getReportingDescriptor();
	
	public boolean isCustomEditorSupported();
	
	public void setSummary(String summary);

	public void setUserComment(String userComment);

	public void setUserName(String userName);

	public void setUserEMail(String userEMail);

	public void setProblemStatus(IStatus problemStatus);
	
	public String buildReport();
	
	public String buildSubject();
}
