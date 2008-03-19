/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.preferences;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.verifier.AbstractVerificationKeyListener;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Abstract preferences page provides validation support.
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractSVNTeamPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage, IValidationManager {
    private VerificationKeyListener changeListener;
    
	public AbstractSVNTeamPreferencesPage() {
		super();
        this.changeListener = new VerificationKeyListener();
	}

	public void init(IWorkbench workbench) {

	}

	public boolean performOk() {
		this.saveValues(this.getPreferenceStore());
		
		SVNTeamUIPlugin.instance().savePluginPreferences();
		
		return true;
	}
	
	protected void performDefaults() {
		super.performDefaults();
		this.loadDefaultValues(this.getPreferenceStore());
		this.initializeControls();
		
		this.validateContent();
	}
	
	protected Control createContents(Composite parent) {
		Control retVal = this.createContentsImpl(parent);
		
		this.loadValues(this.getPreferenceStore());
		this.initializeControls();
		
		this.addListeners();
		
		return retVal;
	}
	
	protected IPreferenceStore doGetPreferenceStore() {
		return SVNTeamUIPlugin.instance().getPreferenceStore();
	}

	protected abstract void loadDefaultValues(IPreferenceStore store);
	protected abstract void loadValues(IPreferenceStore store);
	protected abstract void saveValues(IPreferenceStore store);
	protected abstract void initializeControls();
	protected abstract Control createContentsImpl(Composite parent);
	
	public boolean isFilledRight() {
		return this.changeListener.isFilledRight();
	}

	public void attachTo(Control cmp, AbstractVerifier verifier) {
		this.changeListener.attachTo(cmp, verifier);
	}
	
	public void addListeners() {
		this.changeListener.addListeners();		
		this.validateContent();
		this.setMessage(this.getDescription(), IMessageProvider.NONE);
	}
	
	public void detachFrom(Control cmp) {
		this.changeListener.detachFrom(cmp);
	}
		
	public void detachAll() {
		this.changeListener.detachAll();
	}
	
	public void validateContent() {
		this.changeListener.validateContent();
	}
	
	public boolean validateControl(Control cmp) {
		return this.changeListener.validateControl(cmp);
	}
	
    protected class VerificationKeyListener extends AbstractVerificationKeyListener {
        public VerificationKeyListener() {
            super();
        }
        
        public void hasError(String errorReason) {
        	AbstractSVNTeamPreferencesPage.this.setMessage(errorReason, IMessageProvider.ERROR);
			this.handleButtons();
        }

        public void hasWarning(String warningReason) {
        	AbstractSVNTeamPreferencesPage.this.setMessage(warningReason, IMessageProvider.WARNING);
			this.handleButtons();
        }

        public void hasNoError() {
        	AbstractSVNTeamPreferencesPage.this.setMessage(AbstractSVNTeamPreferencesPage.this.getDescription(), IMessageProvider.NONE);
			this.handleButtons();
        }

        protected void handleButtons() {
        	AbstractSVNTeamPreferencesPage.this.setValid(this.isFilledRight());
        }
        
    }
    
}
