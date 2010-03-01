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
package org.eclipse.team.svn.revision.graph.graphic.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * @author Igor Burilo
 */
public abstract class BaseRevisionGraphAction extends SelectionAction {

	public BaseRevisionGraphAction(IWorkbenchPart part) {
		super(part);
	}
		
	protected void runOperation(final IActionOperation op) {
		if (op == null) {
			return;
		}		
		UIMonitorUtility.doTaskScheduledDefault(this.getWorkbenchPart(), op);
	}
	
	protected RevisionEditPart[] getSelectedEditParts() {
		List<RevisionEditPart> res = new ArrayList<RevisionEditPart>();
		List<?> selected = this.getSelectedObjects();
		if (!selected.isEmpty()) {
			Iterator<?> iter = selected.iterator(); 
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof RevisionEditPart) {
					res.add((RevisionEditPart) obj);
				}
			}						
		} 	
		return res.toArray(new RevisionEditPart[0]);
	}
	
	protected RevisionEditPart getSelectedEditPart() {
		List<?> selected = this.getSelectedObjects();
		if (!selected.isEmpty()) {
			Object obj = selected.get(0);
			if (obj instanceof RevisionEditPart) {
				return (RevisionEditPart) obj;
			}
		}
		return null;
	}
}
