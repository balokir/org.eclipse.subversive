/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.verifier;

import java.util.Iterator;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * Abstract verification listener, that allows us to listen and validate all specified components (not only Text fields) in generic manner
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractVerificationKeyListener extends KeyAdapter implements IValidationManager, IVerifierListener {
    protected GroupVerifier verifier;

    public AbstractVerificationKeyListener() {
        super();
        this.verifier = new GroupVerifier();
        this.verifier.addVerifierListener(this);
    }
    
	public void attachTo(Control cmp, AbstractVerifier verifier) {
		this.verifier.add(cmp, verifier);
	}
	
	public void addListeners() {
		for (Iterator it = this.verifier.getComponents(); it.hasNext(); ) {
			Control cmp = (Control)it.next();
			if (cmp instanceof Text) {
				((Text)cmp).addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						AbstractVerificationKeyListener.this.validateContent();
					}
				});
			}
			if (cmp instanceof Combo) {
				((Combo)cmp).addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						AbstractVerificationKeyListener.this.validateContent();
					}
				});
				((Combo)cmp).addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e) {
						AbstractVerificationKeyListener.this.validateContent();
					}
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			}
		}
	}
	
	public void detachFrom(Control cmp) {
		this.verifier.remove(cmp);
		cmp.removeKeyListener(this);
	}
	
	public void detachAll() {
		this.verifier.removeAll();
		for (Iterator it = this.verifier.getComponents(); it.hasNext(); ) {
			((Control)it.next()).removeKeyListener(this);
		}
	}
	
	public void validateContent() {
		this.verifier.verify();
	}

	public boolean isFilledRight() {
		return this.verifier.isFilledRight();
	}

    public void keyReleased(KeyEvent e) {
        this.validateContent();
    }
    
}
