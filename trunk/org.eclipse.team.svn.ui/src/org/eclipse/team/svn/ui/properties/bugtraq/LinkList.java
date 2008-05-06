/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.properties.bugtraq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Link placements
 * 
 * @author Alexander Gurov
 */
public class LinkList {
	protected ArrayList links = new ArrayList();

	public LinkList() {
		super();
	}
	
	public List getLinks() {
		return this.links;
	}

	public boolean hasLinkAt(int offset) {
		for (Iterator iter = this.links.iterator(); iter.hasNext();) {
			LinkPlacement link = (LinkPlacement) iter.next();
			if (link.existAtOffset(offset)) {
				return true;
			}
		}
		return false;
	}
	
	public LinkPlacement getLinkAt(int offset) {
		for (Iterator iter = this.links.iterator(); iter.hasNext();) {
			LinkPlacement link = (LinkPlacement)iter.next();
			if (link.existAtOffset(offset)) {
				return link;
			}
		}
		return null;
	}
	
	public static class LinkPlacement {
		protected int start;
		protected int end;
		protected String linkText;
		
		public LinkPlacement(int start, int end, String message) {
			this.linkText =  message.substring(start, end);
			this.start = start;
			this.end = end;
		}	
		protected boolean existAtOffset(int offset) {
			return (this.start <= offset) && (offset < this.end);
		}
		public int getStart() {
			return this.start;
		}
		public void setStart(int start) {
			this.start = start;
		}
		public int getEnd() {
			return this.end;
		}
		public void setEnd(int end) {
			this.end = end;
		}
		public String getURL() {
			return this.linkText;
		}
	}

}
