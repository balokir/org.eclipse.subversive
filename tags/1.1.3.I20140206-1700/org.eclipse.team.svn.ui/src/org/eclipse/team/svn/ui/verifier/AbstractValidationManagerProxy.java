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
 * Allow to wrap all verifiers in the same way
 * 
 * It can be useful e.g. in case if you want that all verifiers
 * don't start verifying on some particular event.
 * 
 * So instead of wraping all verifiers separately, you can provide here only one
 * verifier wrapper.
 * 
 * @author Igor Burilo
 */
public abstract class AbstractValidationManagerProxy implements IValidationManager {

	protected IValidationManager validationManager;
	
	public AbstractValidationManagerProxy(IValidationManager validationManager) {
		this.validationManager = validationManager;
	}
	
	public void attachTo(Control cmp, AbstractVerifier verifier) {
		this.validationManager.attachTo(cmp, this.wrapVerifier(verifier));
	}
	
	public void detachFrom(Control cmp) {
		this.validationManager.detachFrom(cmp);
	}

	public void detachAll() {
		this.validationManager.detachAll();
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
	
	/**
	 * Can be overridden if you want to wrap verifier in some other way
	 * 
	 * @param verifier
	 * @return
	 */
	protected AbstractVerifier wrapVerifier(AbstractVerifier verifier) {
		return new AbstractVerifierProxy(verifier) {
			protected boolean isVerificationEnabled(Control input) {
				return AbstractValidationManagerProxy.this.isVerificationEnabled(input);
			}			
		};
	}
	
	protected abstract boolean isVerificationEnabled(Control input);
}
