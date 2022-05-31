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

package org.eclipse.team.svn.tests.ui;

import org.eclipse.team.svn.ui.decorator.DecoratorVariables;
import org.eclipse.team.svn.ui.decorator.IVariable;
import org.eclipse.team.svn.ui.decorator.TextVariableSetProvider;
import org.eclipse.team.svn.ui.decorator.UserVariable;
import org.junit.Test;

/**
 * DecoratorVariables test
 * 
 * @author Alexander Gurov
 */
public class DecoratorVariablesTest {

	protected static IVariable[] format = new IVariable[] { TextVariableSetProvider.VAR_ADDED_FLAG,
			TextVariableSetProvider.VAR_AUTHOR, TextVariableSetProvider.VAR_DATE,
			TextVariableSetProvider.VAR_LOCATION_LABEL, TextVariableSetProvider.VAR_LOCATION_URL,
			TextVariableSetProvider.VAR_NAME, TextVariableSetProvider.VAR_OUTGOING_FLAG,
			TextVariableSetProvider.VAR_RESOURCE_URL, TextVariableSetProvider.VAR_REVISION,
			new UserVariable(TextVariableSetProvider.DOMAIN_NAME, "[test_data]") };

	protected static String formatLine = "{" + TextVariableSetProvider.NAME_OF_ADDED_FLAG + "}" + "{"
			+ TextVariableSetProvider.NAME_OF_AUTHOR + "}" + "{" + TextVariableSetProvider.NAME_OF_DATE + "}" + "{"
			+ TextVariableSetProvider.NAME_OF_LOCATION_LABEL + "}" + "{" + TextVariableSetProvider.NAME_OF_LOCATION_URL
			+ "}" + "{" + TextVariableSetProvider.NAME_OF_NAME + "}" + "{"
			+ TextVariableSetProvider.NAME_OF_OUTGOING_FLAG + "}" + "{" + TextVariableSetProvider.NAME_OF_RESOURCE_URL
			+ "}" + "{" + TextVariableSetProvider.NAME_OF_REVISION + "}" + "[test_data]";

	@Test
	public void testGetVariable() {
		if (!TextVariableSetProvider.instance.getVariable(TextVariableSetProvider.NAME_OF_ADDED_FLAG)
				.equals(TextVariableSetProvider.VAR_ADDED_FLAG)
				|| !TextVariableSetProvider.instance.getVariable(TextVariableSetProvider.NAME_OF_OUTGOING_FLAG)
						.equals(TextVariableSetProvider.VAR_OUTGOING_FLAG)
				|| !TextVariableSetProvider.instance.getVariable(TextVariableSetProvider.NAME_OF_AUTHOR)
						.equals(TextVariableSetProvider.VAR_AUTHOR)
				|| !TextVariableSetProvider.instance.getVariable(TextVariableSetProvider.NAME_OF_DATE)
						.equals(TextVariableSetProvider.VAR_DATE)
				|| !TextVariableSetProvider.instance.getVariable(TextVariableSetProvider.NAME_OF_LOCATION_LABEL)
						.equals(TextVariableSetProvider.VAR_LOCATION_LABEL)
				|| !TextVariableSetProvider.instance.getVariable(TextVariableSetProvider.NAME_OF_LOCATION_URL)
						.equals(TextVariableSetProvider.VAR_LOCATION_URL)
				|| !TextVariableSetProvider.instance.getVariable(TextVariableSetProvider.NAME_OF_NAME)
						.equals(TextVariableSetProvider.VAR_NAME)
				|| !TextVariableSetProvider.instance.getVariable(TextVariableSetProvider.NAME_OF_RESOURCE_URL)
						.equals(TextVariableSetProvider.VAR_RESOURCE_URL)
				|| !TextVariableSetProvider.instance.getVariable(TextVariableSetProvider.NAME_OF_REVISION)
						.equals(TextVariableSetProvider.VAR_REVISION)) {
			throw new RuntimeException("testGetVariable() failed");
		}
	}

	@Test
	public void testPrepareFormatLine() {
		if (!DecoratorVariables.prepareFormatLine(DecoratorVariablesTest.format)
				.equals(DecoratorVariablesTest.formatLine)) {
			throw new RuntimeException("testPrepareFormatLine() failed");
		}
	}

	@Test
	public void testParseFormatLine() {
		IVariable[] vars = new DecoratorVariables(TextVariableSetProvider.instance)
				.parseFormatLine(DecoratorVariablesTest.formatLine);
		for (int i = 0; i < vars.length; i++) {
			if (!vars[i].equals(DecoratorVariablesTest.format[i])) {
				throw new RuntimeException("testParseFormatLine() failed");
			}
		}
	}

}
