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

import org.eclipse.team.svn.core.client.INotificationCallback;
import org.eclipse.team.svn.core.client.Notification;

/**
 * Notify composite listener
 * 
 * @author Alexander Gurov
 */
public class Notify2Composite implements INotificationCallback {
	protected INotificationCallback []listeners;

	public Notify2Composite() {
		this.listeners = new INotificationCallback[0];
	}
	
	public void add(INotificationCallback listener) {
		List tmp = new ArrayList(Arrays.asList(this.listeners));
		if (!tmp.contains(listener)) {
			tmp.add(listener);
		}
		this.listeners = (INotificationCallback [])tmp.toArray(new INotificationCallback[tmp.size()]);
	}
	
	public void remove(INotificationCallback listener) {
		List tmp = new ArrayList(Arrays.asList(this.listeners));
		tmp.remove(listener);
		this.listeners = (INotificationCallback [])tmp.toArray(new INotificationCallback[tmp.size()]);
	}

	public void notify(Notification info) {
		// thread safe...
	    INotificationCallback []tmp = this.listeners;
		for (int i = 0; i < tmp.length; i++) {
			tmp[i].notify(info);
		}
	}

}
