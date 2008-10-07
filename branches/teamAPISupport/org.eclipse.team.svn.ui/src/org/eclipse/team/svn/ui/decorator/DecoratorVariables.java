/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Dann Martens - [patch] Text decorations 'ascendant' variable
 *******************************************************************************/

package org.eclipse.team.svn.ui.decorator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.IDecoration;

/**
 * Contains methods used to create decoration
 * 
 * @author Alexander Gurov
 */
public final class DecoratorVariables {
	
	protected IVariableSetProvider variableSetProvider;
	
	private String getValue(IVariable var, IVariableContentProvider provider) {
		return amend(var, provider);
	}
	
	/**
	 * Helper method which recurses through variables in variables, first order only.
	 * @param var A variable wrapper.
	 * @param provider A <code>IVariableContentProvider</code>
	 * @return The amended value of this variable.
	 */
	private String amend(IVariable var, IVariableContentProvider provider) {
		IVariable[] variables = parseFormatLine(provider.getValue(var));
		String value = "";
		for(int i = 0; i < variables.length; i++) {
			String variableValue = provider.getValue(variables[i]);
			if (!variables[i].equals(var)) {
				value += variableValue;
			}
			else {
				if (variableValue.equals(variables[i].getName())) {
					value += variableValue;
				}
				else {
					value += "?{" + variables[i].getName() + "}?";
				}
			}
		}
		return value;
	}
	
	public void decorateText(IDecoration decoration, IVariable []format, IVariableContentProvider provider) {
		int centerPoint = Arrays.asList(format).indexOf(this.variableSetProvider.getCenterVariable());		
		String prefix = "";
		String suffix = "";
		for (int i = 0; i < format.length; i++) {
			if (!format[i].equals(this.variableSetProvider.getCenterVariable())) {
				if (centerPoint != -1 && i < centerPoint) {
					prefix += getValue(format[i], provider);
				}
				else {
					suffix += getValue(format[i], provider);
				}
			}
		}
		// trim left/trim right
		int i = 0;
		for (; i < prefix.length() && Character.isWhitespace(prefix.charAt(i)); i++);
		prefix = prefix.substring(i);
		i = suffix.length() - 1;
		for (; i >= 0 && Character.isWhitespace(suffix.charAt(i)); i--);
		suffix = suffix.substring(0, i + 1);

		decoration.addPrefix(prefix);
		decoration.addSuffix(suffix);
	}
	
	public static String prepareFormatLine(IVariable []format) {
		String retVal = "";
		for (int i = 0; i < format.length; i++) {
			if (format[i] instanceof UserVariable) {
				retVal += format[i].getName();
			}
			else {
				retVal += "{" + format[i].getName() + "}";
			}
		}
		return retVal;
	}
	
	public IVariable[] parseFormatLine(String line) {
		List<IVariable> retVal = new ArrayList<IVariable>();
		
		int startPos = 0;
		int stopPos = -1;
		int state = 0;
		do {
			switch (state) {
			case 0: {
				stopPos = line.indexOf('{', startPos);
				String userData = stopPos != -1 ? line.substring(startPos, stopPos++) : line.substring(startPos);
				if (userData.length() > 0) {
					retVal.add(new UserVariable(this.variableSetProvider.getDomainName(), userData));
				}
				startPos = stopPos;
				state = 1;
				break;
			}
			case 1: {
				stopPos = line.indexOf('}', startPos);
				if (stopPos != -1) {
					String varName = line.substring(startPos, stopPos++);
					if (varName.length() > 0) {
						IVariable var = this.variableSetProvider.getVariable(varName);
						retVal.add(var == null ? new UserVariable(this.variableSetProvider.getDomainName(), varName) : var);
					}
				}
				else {
					String userData = line.substring(startPos);
					if (userData.length() > 0) {
						retVal.add(new UserVariable(this.variableSetProvider.getDomainName(), userData));
					}
				}
				startPos = stopPos;
				state = 0;
				break;
			}
			}
		} while (stopPos > 0);
		
		return retVal.toArray(new IVariable[retVal.size()]);
	}
	
	public DecoratorVariables(IVariableSetProvider variableSetProvider) {
		this.variableSetProvider = variableSetProvider;
	}
	
}
