/*******************************************************************************
 * Copyright (c) 2008 Thomas Champagne.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thomas Champagne - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.utility;

import java.util.Date;

import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Utility class for format date with the good preference. 
 * @author Thomas Champagne
 */
public final class DateFormatter {	
	
	private DateFormatter() {
	}
	
	public static String formatDate(long date) {
		return DateFormatter.formatDate(new Date(date));
	}

	public static String formatDate(Date date) {
		return SVNTeamPreferences.getDateFormat(SVNTeamUIPlugin.instance().getPreferenceStore()).format(date);
	}
}
