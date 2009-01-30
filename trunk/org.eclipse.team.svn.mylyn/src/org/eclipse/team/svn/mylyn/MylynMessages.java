/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.mylyn;

import org.eclipse.osgi.util.NLS;
import org.eclipse.team.svn.core.BaseMessages;

public class MylynMessages extends BaseMessages {

	protected static final String BUNDLE_NAME = "org.eclipse.team.svn.mylyn.messages";	 //$NON-NLS-1$
	
	public static String Operation_OpenReportEditor;
	public static String Operation_OpenReportEditor_Id;
	
	static {
		//load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, MylynMessages.class);
	}
	
	public static String getString(String key) {
		return BaseMessages.getString(key, MylynMessages.class);
	}
	
	public static String getErrorString(String key) {
		return BaseMessages.getErrorString(key, MylynMessages.class);
	}
	
	public static String formatErrorString(String key, Object[] args) {
		return BaseMessages.formatErrorString(key, args, MylynMessages.class);
	}
}
