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

package org.eclipse.team.svn.ui.verifier;

import java.util.HashMap;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

/**
 * Composite properties verifier for properties' pages.
 * 
 * @author Alexei Goncharov
 */
public class CompositePropertiesVerifier extends AbstractVerifier {
	
	private HashMap<String, AbstractFormattedVerifier> verifiers;
	private Combo propNameCombo;
		
	public CompositePropertiesVerifier(Combo propNameCombo, HashMap<String, AbstractFormattedVerifier> verifierParam) {
		this.verifiers = verifierParam;
		this.propNameCombo = propNameCombo;
	}
	
	public boolean verify(Control input) {
		AbstractFormattedVerifier current = this.verifiers.get(this.propNameCombo.getText());
		if (current == null) {
			return true;
		}
		String msg = current.getErrorMessage(input);
		if (msg != null) {
			this.fireError(msg);
			return false;
		}
		msg = current.getWarningMessage(input);
		if (msg != null) {
			this.fireWarning(msg);
		}
		else {
			this.fireOk();
		}
		return true;
	}
	
	protected String getErrorMessage(Control input) {
		return null;
	}

	protected String getWarningMessage(Control input) {
		return null;
	}

}
