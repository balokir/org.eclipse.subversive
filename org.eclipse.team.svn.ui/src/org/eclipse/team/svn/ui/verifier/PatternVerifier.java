/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.verifier;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.text.StringMatcher;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * Pattern verifier
 *
 * @author Sergiy Logvin
 */
public class PatternVerifier extends AbstractFormattedVerifier {
	protected IResource []resources;
	protected static String message;

	public PatternVerifier(String fieldName, IResource []resources) {
		super(fieldName);
		PatternVerifier.message = SVNUIMessages.Verifier_Pattern;
		this.resources = resources;
	}

	protected String getErrorMessageImpl(Control input) {
		String pattern = this.getText(input);
		StringMatcher matcher = new StringMatcher(pattern, true, false);
		for (int i = 0; i < this.resources.length; i++) {
			if (!matcher.match(this.resources[i].getName())) {
				return BaseMessages.format(PatternVerifier.message, new Object[] {this.resources[i].getName()});
			}				
		}
		return null;
	}

	protected String getWarningMessageImpl(Control input) {
		return null;
	}

}
