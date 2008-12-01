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

import java.text.MessageFormat;

import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * Comment verifier
 * 
 * @author Sergiy Logvin
 */
public class CommentVerifier extends AbstractFormattedVerifier {
    protected int logMinSize;
        
    public CommentVerifier(String fieldName, int logMinSize) {
        super(fieldName);
        this.logMinSize = logMinSize;
    }
    
    protected String getErrorMessageImpl(Control input) {
    	if (this.getText(input).trim().length() < this.logMinSize) {
    		return MessageFormat.format(SVNUIMessages.Verifier_Comment_Error, this.logMinSize);
    	}
        return null;
    }

    protected String getWarningMessageImpl(Control input) {
    	if (this.getText(input).trim().length() == 0) {
    		return SVNUIMessages.Verifier_Comment_Warning;
    	}
        return null;
    }

}
