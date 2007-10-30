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

package org.eclipse.team.svn.core.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.team.svn.core.client.Notify2;
import org.eclipse.team.svn.core.client.NotifyInformation;

/**
 * Notify composite listener
 * 
 * @author Alexander Gurov
 */
public class Notify2Composite implements Notify2 {
	protected Notify2 []listeners;

	public Notify2Composite() {
		this.listeners = new Notify2[0];
	}
	
	public void add(Notify2 listener) {
		List tmp = new ArrayList(Arrays.asList(this.listeners));
		if (!tmp.contains(listener)) {
			tmp.add(listener);
		}
		this.listeners = (Notify2 [])tmp.toArray(new Notify2[tmp.size()]);
	}
	
	public void remove(Notify2 listener) {
		List tmp = new ArrayList(Arrays.asList(this.listeners));
		tmp.remove(listener);
		this.listeners = (Notify2 [])tmp.toArray(new Notify2[tmp.size()]);
	}

	public void onNotify(NotifyInformation info) {
		// thread safe...
	    Notify2 []tmp = this.listeners;
		for (int i = 0; i < tmp.length; i++) {
			tmp[i].onNotify(info);
		}
	}

}
