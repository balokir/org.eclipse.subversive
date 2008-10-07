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

/**
 * The reporter implements concrete issue reporting mechanism: mail, web services or something else.
 * 
 * @author Alexander Gurov
 */
public interface IReporterFactory {
	/**
	 * Enumeration of available report types
	 */
	public enum ReportType {
		BUG, TIP
	}

	/**
	 * Returns <code>true</code> if report could be edited in the custom feature-reach editor
	 * @return <code>true</code> if the custom editor is supported by reporter
	 */
	public boolean isCustomEditorSupported();
	
	/**
	 * Create new issue reporter instance based on {@link IReportingDescriptor} descriptor
	 * 
	 * @param settings
	 *            tracker settings descriptor
	 * @return new issue reporter instance or <code>null</code>
	 */
	public IReporter newReporter(IReportingDescriptor settings, ReportType type);
}
