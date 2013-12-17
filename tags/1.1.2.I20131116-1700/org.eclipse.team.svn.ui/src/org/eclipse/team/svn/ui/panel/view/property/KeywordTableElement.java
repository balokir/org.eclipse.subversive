/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.view.property;

/**
 * Table element representing 'svn:keywords' property value
 * 
 * @author Sergiy Logvin
 */
public class KeywordTableElement {
	public static final int INITIAL = -1;
    public static final int SELECTED = 0;
    public static final int DESELECTED = 1; 
    public static final int MIXED = 2;
    
    private String name; 
    private String description;
    private String sample;
    private int initialState;
    private int currentState;
    
    public KeywordTableElement(String name, String description, String sample, int initialState) {
        this.name = name;
        this.description = description;
        this.sample = sample;
        this.initialState = initialState;
        this.currentState =  initialState;
    }

	public int getCurrentState() {
		return this.currentState;
	}

	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}

	public String getDescription() {
		return this.description;
	}

	public int getInitialState() {
		return this.initialState;
	}

	public void setInitialState(int initialState) {
		this.initialState = initialState;
	}

	public String getName() {
		return this.name;
	}

	public String getSample() {
		return this.sample;
	}
	
}
