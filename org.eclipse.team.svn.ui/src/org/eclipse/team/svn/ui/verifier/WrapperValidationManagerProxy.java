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

package org.eclipse.team.svn.ui.verifier;

import org.eclipse.swt.widgets.Control;

/**
 * Wraps all verifiers
 * 
 * @author Igor Burilo
 *
 */
public abstract class WrapperValidationManagerProxy implements IValidationManager {
	
	protected IValidationManager validationManager;
	
	public WrapperValidationManagerProxy(IValidationManager validationManager) {
		this.validationManager = validationManager;
	}

	public void attachTo(Control cmp, AbstractVerifier verifier) {
		this.validationManager.attachTo(cmp, this.wrapVerifier(verifier));			
	}

	public void detachAll() {
		this.validationManager.detachAll();						
	}

	public void detachFrom(Control cmp) {
		this.validationManager.detachFrom(cmp);			
	}

	public boolean isFilledRight() {
		return this.validationManager.isFilledRight();
	}

	public void validateContent() {
		this.validationManager.validateContent();			
	}

	public boolean validateControl(Control cmp) {
		return this.validationManager.validateControl(cmp);
	}
	
	protected abstract AbstractVerifier wrapVerifier(AbstractVerifier verifier);
}
