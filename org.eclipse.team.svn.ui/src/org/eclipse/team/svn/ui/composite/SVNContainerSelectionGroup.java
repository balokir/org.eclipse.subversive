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

package org.eclipse.team.svn.ui.composite;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.ui.internal.ide.misc.ContainerSelectionGroup;

/**
 * This control works with Eclispe IDE 3.0 - 3.2
 * 
 * @author Alexander Gurov
 */
public class SVNContainerSelectionGroup extends ContainerSelectionGroup {
	public SVNContainerSelectionGroup(Composite parent, Listener listener) {
		super(parent, listener, false, "", false);
	}
	
	public void createContents(String message, int heightHint, int widthHint) {
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        setLayout(layout);
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createTreeViewer(heightHint);
        Dialog.applyDialogFont(this);
    }
	
	public void createContents(String message, int heightHint) {
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        setLayout(layout);
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createTreeViewer(heightHint);
        Dialog.applyDialogFont(this);
    }
	
    public static class SVNContainerSelectionVerifier extends AbstractFormattedVerifier {
    	protected static String ERROR_MESSAGE;
    	protected static String DESTINATION_IS_DETACHED_FROM_SVN;
    	protected static String DESTINATION_DIRECTORY_IS_DELETED;
    	protected static String DESTINATION_DIRECTORY_IS_OBSTRUCTED;
    	
    	public SVNContainerSelectionVerifier() {
            super("");
            
            SVNContainerSelectionVerifier.ERROR_MESSAGE = SVNUIMessages.SVNContainerSelectionGroup_Verifier_NotSelected;
            SVNContainerSelectionVerifier.DESTINATION_IS_DETACHED_FROM_SVN = SVNUIMessages.SVNContainerSelectionGroup_Verifier_NonSVN;
            SVNContainerSelectionVerifier.DESTINATION_DIRECTORY_IS_DELETED = SVNUIMessages.SVNContainerSelectionGroup_Verifier_Deleted;
            SVNContainerSelectionVerifier.DESTINATION_DIRECTORY_IS_OBSTRUCTED = SVNUIMessages.SVNContainerSelectionGroup_Verifier_Obstructed;
        }
    	
        protected String getErrorMessageImpl(Control input) {
        	SVNContainerSelectionGroup control = (SVNContainerSelectionGroup)input;
        	if (control.getContainerFullPath() == null) {
            	return SVNContainerSelectionVerifier.ERROR_MESSAGE;	
            }
        	IResource destinationRoot = ResourcesPlugin.getWorkspace().getRoot().findMember(control.getContainerFullPath());
    		ILocalResource localDest = SVNRemoteStorage.instance().asLocalResource(destinationRoot);
    		if (IStateFilter.SF_INTERNAL_INVALID.accept(localDest)) {
    			return this.isNonSVNCheckDisabled() ? SVNContainerSelectionVerifier.DESTINATION_IS_DETACHED_FROM_SVN : null;
    		}
        	if (IStateFilter.SF_DELETED.accept(localDest)) {
        		return SVNContainerSelectionVerifier.DESTINATION_DIRECTORY_IS_DELETED;
        	}
        	if (IStateFilter.SF_OBSTRUCTED.accept(localDest)) {
        		return SVNContainerSelectionVerifier.DESTINATION_DIRECTORY_IS_OBSTRUCTED;
        	}
            return null;
        }
        
        protected String getWarningMessageImpl(Control input) {
        	return null;
        }
        
        protected boolean isNonSVNCheckDisabled() {
        	return true;
        }
    }
    
    public static class SVNContainerCheckOutSelectionVerifier extends SVNContainerSelectionVerifier {

    	protected static String WARNING_MESSAGE;
    	
    	public SVNContainerCheckOutSelectionVerifier() { 
            super();
            SVNContainerCheckOutSelectionVerifier.WARNING_MESSAGE = SVNUIMessages.SVNContainerSelectionGroup_Verifier_NonSVNWarning;
        }
    	
    	protected String getWarningMessageImpl(Control input) {
    		SVNContainerSelectionGroup control = (SVNContainerSelectionGroup)input;
    		IResource destinationRoot = ResourcesPlugin.getWorkspace().getRoot().findMember(control.getContainerFullPath());
    		if (!FileUtility.isConnected(destinationRoot)) {
    			return SVNContainerCheckOutSelectionVerifier.WARNING_MESSAGE;
    		}
    		return null;
    	}
    	
    	protected boolean isNonSVNCheckDisabled() {
        	return false;
        }
    }
	    
}
