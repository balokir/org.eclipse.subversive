/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Control;

/**
 * Group verifier allows us to verify dedicated group of controls placed on the panel
 * 
 * @author Alexander Gurov
 */
public class GroupVerifier extends CompositeVerifier {
    protected Map<AbstractVerifier, Control> componentsMap;
    
    public GroupVerifier() {
        super();
        this.componentsMap = new LinkedHashMap<AbstractVerifier, Control>();
    }

	public boolean verify() {
		this.hasWarning = false;
		for (Iterator<?> it = this.verifiers.iterator(); it.hasNext(); ) {
			AbstractVerifier iVer = (AbstractVerifier)it.next();
			if (!iVer.verify(this.componentsMap.get(iVer))) {
				return false;
			}
		}
		if (!this.hasWarning) {
			this.fireOk();
		}
		return true;
	}
	
	public boolean verify(Control input) {
		// could be used as workaround for situations when control validation is required in any case
		for (Map.Entry<AbstractVerifier, Control> entry : this.componentsMap.entrySet()) {
			if (entry.getValue().equals(input)) {
				return entry.getKey().verify(input);
			}
		}
		return true;
	}
	
	public void add(Control cmp, AbstractVerifier verifier) {
		super.add(verifier);
		this.componentsMap.put(verifier, cmp);
	}
    
	public void remove(Control cmp) {
	    for (Iterator<Map.Entry<AbstractVerifier, Control>> it = this.componentsMap.entrySet().iterator(); it.hasNext(); ) {
	    	Map.Entry<AbstractVerifier, Control> entry = it.next();
	        if (cmp == entry.getValue()) {
	            AbstractVerifier verifier = entry.getKey();
				super.remove(verifier);
				it.remove();
				break;
	        }
	    }
	}
    
	public void removeAll() {
		super.removeAll();
		this.componentsMap.clear();
	}
        
	public Iterator<Control> getComponents() {
		return this.componentsMap.values().iterator();
	}
	
}
