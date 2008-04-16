/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.verifier;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Property verifier. Checks the property value to match property regExp
 * 
 * @author Alexei Goncharov
 */
public class PropertyVerifier extends AbstractFormattedVerifier {
	
	private Pattern pattern;
	private String propName;
	private boolean toValidate;
	
	public PropertyVerifier(String fieldName, String regExp, String propName) {
        super(fieldName);
        if (regExp == null) {
        	this.toValidate = false;
        	return;
        }
        this.toValidate = true;
        String [] parts = propName.split(":");
		this.propName = "";
		for (int i = 0; i < parts.length; i++) {
			this.propName += parts[i];
		} 
        this.pattern = Pattern.compile(regExp);
        
    }
	
	protected String getErrorMessageImpl(Control input) {
		if (!this.toValidate) {
			return null;
		}
		String inputText = this.getText(input);
		if (this.propName.equals("bugtraqlogregex")) {
			try {
				String [] logs = inputText.split("\r\n");
				for (int i = 0; i < logs.length; i++) {
					Pattern.compile(logs[i]);
				}
			}
			catch (Exception ex) {
				return SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.Verifier." + this.propName);
			}
			return null;
		}
		if (this.propName.equals("bugtraqmessage")) {
			if (!inputText.contains("%BUGID%")) {
				return SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.Verifier." + this.propName);
			}
			return null;
		}
		if (this.propName.equals("svnexternals")) {
			try {
				SVNUtility.parseSVNExternalsProperty(inputText);
			}
			catch (Exception ex) {
				return SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.Verifier." + this.propName);
			}
			return null;
		}
		Matcher matcher = this.pattern.matcher(inputText);
		if (!matcher.matches()) {
			String retVal = SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.Verifier." + this.propName);
			if (retVal.equals("PropertyEditPanel.Verifier." + this.propName)) {
				return MessageFormat.format(SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.regExp.Verifier"), this.pattern.pattern());
			}
			return SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.Verifier." + this.propName);
		}
		return null;
	}	

	protected String getWarningMessageImpl(Control input) {
		return null;
	}

}
