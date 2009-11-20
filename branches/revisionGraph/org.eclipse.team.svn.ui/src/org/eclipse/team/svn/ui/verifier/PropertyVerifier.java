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

import com.ibm.icu.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * Property verifier. Checks the property value to match property regExp
 * 
 * @author Alexei Goncharov
 */
public class PropertyVerifier extends AbstractFormattedVerifier {
	private IRepositoryResource base;
	private Pattern pattern;
	private String propName;
	private boolean toValidate;
	
	public PropertyVerifier(String fieldName, String regExp, String propName, IRepositoryResource base) {
        super(fieldName);
        if (regExp == null) {
        	this.toValidate = false;
        	return;
        }
        this.toValidate = true;
        String [] parts = propName.split(":"); //$NON-NLS-1$
		this.propName = ""; //$NON-NLS-1$
		for (int i = 0; i < parts.length; i++) {
			this.propName += parts[i];
		} 
        this.pattern = Pattern.compile(regExp);
        this.base = base;
    }
	
	protected String getErrorMessageImpl(Control input) {
		if (!this.toValidate || this.propName.equals("svnlog") || this.propName.equals("svnauthor")) { //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		String inputText = this.getText(input);
		if (this.propName.equals("bugtraqlogregex")) { //$NON-NLS-1$
			try {
				String [] logs = inputText.split("\r\n"); //$NON-NLS-1$
				for (int i = 0; i < logs.length; i++) {
					Pattern.compile(logs[i]);
				}
			}
			catch (Exception ex) {
				return SVNUIMessages.getString("PropertyEditPanel_Verifier_" + this.propName); //$NON-NLS-1$
			}
			return null;
		}
		if (this.propName.equals("bugtraqmessage")) { //$NON-NLS-1$
			if (!inputText.contains("%BUGID%")) { //$NON-NLS-1$
				return SVNUIMessages.getString("PropertyEditPanel_Verifier_" + this.propName); //$NON-NLS-1$
			}
			return null;
		}
		if (this.propName.equals("svnexternals")) { //$NON-NLS-1$
			try {
				SVNUtility.parseSVNExternalsProperty(inputText, this.base);
			}
			catch (Exception ex) {
				return SVNUIMessages.getString("PropertyEditPanel_Verifier_" + this.propName); //$NON-NLS-1$
			}
			return null;
		}
		if (this.propName.equals("svndate")) { //$NON-NLS-1$
			try {
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(inputText); //$NON-NLS-1$
			}
			catch (Exception ex) {
				return SVNUIMessages.format("PropertyEditPanel_Verifier_" + this.propName, new String [] {"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"}); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return null;
		}
		Matcher matcher = this.pattern.matcher(inputText);
		if (!matcher.matches()) {
			String retVal = SVNUIMessages.getString("PropertyEditPanel_Verifier_" + this.propName); //$NON-NLS-1$		
			if (retVal.equals("PropertyEditPanel_Verifier_" + this.propName)) { //$NON-NLS-1$
				return SVNUIMessages.format(SVNUIMessages.PropertyEditPanel_regExp_Verifier, new String[]{this.pattern.pattern()});
			}
			return SVNUIMessages.getString("PropertyEditPanel_Verifier_" + this.propName); //$NON-NLS-1$
		}
		return null;
	}	

	protected String getWarningMessageImpl(Control input) {
		if (this.propName == null) {
			return null;
		}
		if (this.propName.equals("svnauthor") //$NON-NLS-1$
				|| this.propName.equals("svnlog") //$NON-NLS-1$
				|| this.propName.equals("svndate")) { //$NON-NLS-1$
			return SVNUIMessages.getString("PropertyEditPanel_Verifier_Warning_" + this.propName); //$NON-NLS-1$
		}
		return null;
	}

}
