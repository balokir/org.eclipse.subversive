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

package org.eclipse.team.svn.core.connector;


/**
 * Progress monitor interface
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public interface ISVNProgressMonitor {
	public static final int TOTAL_UNKNOWN = -1;

	public static class ItemState {
		public final String path;

		public final int action;

		public final int kind;

		public final String mimeType;

		public final int contentState;

		public final int propState;

		public final long revision;
		
		public final String error;

		public ItemState(String path, int action, int kind, String mimeType, int contentState, int propState, long revision, String error) {
			this.path = path;
			this.action = action;
			this.kind = kind;
			this.mimeType = mimeType;
			this.contentState = contentState;
			this.propState = propState;
			this.revision = revision;
			this.error = error;
		}
	}

	public void progress(int current, int total, ItemState state);
	
	public void commitStatus(SVNCommitStatus status);

	public boolean isActivityCancelled();
}
