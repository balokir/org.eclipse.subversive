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

package org.eclipse.team.svn.test.ui;

import org.eclipse.team.svn.ui.decorator.DecoratorVariables;

import junit.framework.TestCase;

/**
 * DecoratorVariables test
 * 
 * @author Alexander Gurov
 */
public class DecoratorVariablesTest extends TestCase {

	protected static DecoratorVariables.IVariable []format = new DecoratorVariables.IVariable[] {
			DecoratorVariables.VAR_ADDED_FLAG,
			DecoratorVariables.VAR_AUTHOR,
			DecoratorVariables.VAR_DATE,
			DecoratorVariables.VAR_LOCATION_LABEL,
			DecoratorVariables.VAR_LOCATION_URL,
			DecoratorVariables.VAR_NAME,
			DecoratorVariables.VAR_OUTGOING_FLAG,
			DecoratorVariables.VAR_RESOURCE_URL,
			DecoratorVariables.VAR_REVISION,
			new DecoratorVariables.UserVariable("[test_data]")
		};
	
	protected static String formatLine = 
		"{" + DecoratorVariables.NAME_OF_ADDED_FLAG + "}" + 
		"{" + DecoratorVariables.NAME_OF_AUTHOR + "}" + 
		"{" + DecoratorVariables.NAME_OF_DATE + "}" + 
		"{" + DecoratorVariables.NAME_OF_LOCATION_LABEL + "}" + 
		"{" + DecoratorVariables.NAME_OF_LOCATION_URL + "}" + 
		"{" + DecoratorVariables.NAME_OF_NAME + "}" + 
		"{" + DecoratorVariables.NAME_OF_OUTGOING_FLAG + "}" + 
		"{" + DecoratorVariables.NAME_OF_RESOURCE_URL + "}" + 
		"{" + DecoratorVariables.NAME_OF_REVISION + "}" + 
		"[test_data]";
	
	public void testGetVariable() {
		if (!DecoratorVariables.getVariable(DecoratorVariables.NAME_OF_ADDED_FLAG).equals(DecoratorVariables.VAR_ADDED_FLAG) ||
			!DecoratorVariables.getVariable(DecoratorVariables.NAME_OF_OUTGOING_FLAG).equals(DecoratorVariables.VAR_OUTGOING_FLAG) ||
			!DecoratorVariables.getVariable(DecoratorVariables.NAME_OF_AUTHOR).equals(DecoratorVariables.VAR_AUTHOR) ||
			!DecoratorVariables.getVariable(DecoratorVariables.NAME_OF_DATE).equals(DecoratorVariables.VAR_DATE) ||
			!DecoratorVariables.getVariable(DecoratorVariables.NAME_OF_LOCATION_LABEL).equals(DecoratorVariables.VAR_LOCATION_LABEL) ||
			!DecoratorVariables.getVariable(DecoratorVariables.NAME_OF_LOCATION_URL).equals(DecoratorVariables.VAR_LOCATION_URL) ||
			!DecoratorVariables.getVariable(DecoratorVariables.NAME_OF_NAME).equals(DecoratorVariables.VAR_NAME) ||
			!DecoratorVariables.getVariable(DecoratorVariables.NAME_OF_RESOURCE_URL).equals(DecoratorVariables.VAR_RESOURCE_URL) ||
			!DecoratorVariables.getVariable(DecoratorVariables.NAME_OF_REVISION).equals(DecoratorVariables.VAR_REVISION)
			) {
			throw new RuntimeException("testGetVariable() failed");
		}
	}

	public void testDecorateText() {
	}

	public void testPrepareFormatLine() {
		if (!DecoratorVariables.prepareFormatLine(DecoratorVariablesTest.format).equals(DecoratorVariablesTest.formatLine)) {
			throw new RuntimeException("testPrepareFormatLine() failed");
		}
	}

	public void testParseFormatLine() {
		DecoratorVariables.IVariable []vars = DecoratorVariables.parseFormatLine(DecoratorVariablesTest.formatLine);
		for (int i = 0; i < vars.length; i++) {
			if (!vars[i].equals(DecoratorVariablesTest.format[i])) {
				throw new RuntimeException("testParseFormatLine() failed");
			}
		}
	}

}
