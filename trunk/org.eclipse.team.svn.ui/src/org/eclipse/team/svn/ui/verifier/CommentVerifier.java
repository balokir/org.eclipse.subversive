/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.verifier;

import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Comment verifier
 * 
 * @author Elena Matokhina
 */
public class CommentVerifier extends AbstractFormattedVerifier {
    protected static String WARNING_MESSAGE = "Commit comment is empty.";
        
    public CommentVerifier(String fieldName) {
        super(fieldName);
        CommentVerifier.WARNING_MESSAGE = SVNTeamUIPlugin.instance().getResource("Verifier.Comment");
    }
    
    protected String getErrorMessageImpl(Control input) {
        return null;
    }

    protected String getWarningMessageImpl(Control input) {
    	if (this.getText(input).trim().length() == 0) {
    		return CommentVerifier.WARNING_MESSAGE;
    	}
        return null;
    }

}
