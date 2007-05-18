/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.tigris.subversion.javahl;

import java.util.Date;

import org.eclipse.team.svn.core.client.BlameCallback;
import org.eclipse.team.svn.core.client.ChangePath;
import org.eclipse.team.svn.core.client.Info2;
import org.eclipse.team.svn.core.client.Lock;
import org.eclipse.team.svn.core.client.LogMessage;
import org.eclipse.team.svn.core.client.Notify2;
import org.eclipse.team.svn.core.client.NotifyInformation;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Status;

/**
 * JavaHL <-> Subversive API conversions
 * 
 * @author Alexander Gurov
 */
public final class ConversionUtility {
	public static Info2 []convert(org.tigris.subversion.javahl.Info2 []infos) {
		if (infos == null) {
			return null;
		}
		Info2 []retVal = new Info2[infos.length];
		for (int i = 0; i < infos.length; i++) {
			retVal[i] = ConversionUtility.convert(infos[i]);
		}
		return retVal;
	}
	
	public static Info2 convert(org.tigris.subversion.javahl.Info2 info) {
		return new Info2(
				info.getPath(), info.getUrl(), info.getRev(), info.getKind(), info.getReposRootUrl(), info.getReposUUID(), 
				info.getLastChangedRev(), info.getLastChangedDate() == null ? 0 : info.getLastChangedDate().getTime(), 
				info.getLastChangedAuthor(), ConversionUtility.convert(info.getLock()), info.isHasWcInfo(), info.getSchedule(), 
				info.getCopyFromUrl(), info.getCopyFromRev(), info.getTextTime() == null ? 0 : info.getTextTime().getTime(), 
				info.getPropTime() == null ? 0 : info.getPropTime().getTime(), info.getChecksum(), info.getConflictOld(), 
				info.getConflictNew(), info.getConflictWrk(), info.getPrejfile());
	}
	
	public static LogMessage []convert(org.tigris.subversion.javahl.LogMessage []msgs) {
		if (msgs == null) {
			return null;
		}
		LogMessage []retVal = new LogMessage[msgs.length];
		for (int i = 0; i < msgs.length; i++) {
			retVal[i] = ConversionUtility.convert(msgs[i]);
		}
		return retVal;
	}
	
	public static LogMessage convert(org.tigris.subversion.javahl.LogMessage msg) {
		return new LogMessage(msg.getMessage(), msg.getDate() == null ? 0 : msg.getDate().getTime(), msg.getRevisionNumber(), msg.getAuthor(), ConversionUtility.convert(msg.getChangedPaths()));
	}
	
	public static ChangePath []convert(org.tigris.subversion.javahl.ChangePath []paths) {
		if (paths == null) {
			return null;
		}
		ChangePath []retVal = new ChangePath[paths.length];
		for (int i = 0; i < paths.length; i++) {
			retVal[i] = ConversionUtility.convert(paths[i]);
		}
		return retVal;
	}
	
	public static ChangePath convert(org.tigris.subversion.javahl.ChangePath path) {
		return new ChangePath(path.getPath(), path.getCopySrcRevision(), path.getCopySrcPath(), path.getAction());
	}
	
	public static PropertyData []convert(org.tigris.subversion.javahl.PropertyData []data) {
		if (data == null) {
			return null;
		}
		PropertyData []retVal = new PropertyData[data.length];
		for (int i = 0; i < data.length; i++) {
			retVal[i] = ConversionUtility.convert(data[i]);
		}
		return retVal;
	}
	
	public static PropertyData convert(org.tigris.subversion.javahl.PropertyData data) {
		return data == null ? null : new PropertyData(data.getName(), data.getValue(), data.getData());
	}
	
	public static Status []convert(org.tigris.subversion.javahl.Status []st) {
		if (st == null) {
			return null;
		}
		Status []retVal = new Status[st.length];
		for (int i = 0; i < st.length; i++) {
			retVal[i] = ConversionUtility.convert(st[i]);
		}
		return retVal;
	}
	
	public static Status convert(org.tigris.subversion.javahl.Status st) {
		return new Status(
				st.getPath(), st.getUrl(), st.getNodeKind(), 
				st.getRevisionNumber(), st.getLastChangedRevisionNumber(), 
				st.getLastChangedDate() == null ? 0 : st.getLastChangedDate().getTime(), 
				st.getLastCommitAuthor(), st.getTextStatus(), st.getPropStatus(), 
				st.getRepositoryTextStatus(), st.getRepositoryPropStatus(), st.isLocked(), 
				st.isCopied(), st.getConflictOld(), st.getConflictNew(), st.getConflictWorking(), 
				st.getUrlCopiedFrom(), st.getRevisionCopiedFromNumber(), st.isSwitched(), 
				st.getLockToken(), st.getLockOwner(), st.getLockComment(), 
				st.getLockCreationDate() == null ? 0 : st.getLockCreationDate().getTime(), 
				ConversionUtility.convert(st.getReposLock()), st.getReposLastCmtRevisionNumber(), 
				st.getReposLastCmtDate() == null ? 0 : st.getReposLastCmtDate().getTime(), 
				st.getReposKind(), st.getReposLastCmtAuthor(), null);
	}
	
	public static Lock convert(org.tigris.subversion.javahl.Lock lock) {
		return 
			lock == null ? 
			null :
			new Lock(
				lock.getOwner(), lock.getPath(), lock.getToken(), lock.getComment(), 
				lock.getCreationDate() == null ? 0 : lock.getCreationDate().getTime(), 
				lock.getExpirationDate() == null ? 0 : lock.getExpirationDate().getTime());
	}
	
	public static org.tigris.subversion.javahl.BlameCallback convert(final BlameCallback cb) {
		return new org.tigris.subversion.javahl.BlameCallback() {
			public void singleLine(Date changed, long revision, String author, String line) {
				cb.singleLine(changed.getTime(), revision, author, line);
			}
		};
	}
	
	public static org.tigris.subversion.javahl.Revision convert(Revision rev) {
		if (rev != null) {
			switch (rev.getKind()) {
	            case Revision.Kind.base: return org.tigris.subversion.javahl.Revision.BASE;
	            case Revision.Kind.committed: return org.tigris.subversion.javahl.Revision.COMMITTED;
	            case Revision.Kind.head: return org.tigris.subversion.javahl.Revision.HEAD;
	            case Revision.Kind.previous: return org.tigris.subversion.javahl.Revision.PREVIOUS;
	            case Revision.Kind.working: return org.tigris.subversion.javahl.Revision.WORKING;
	            case Revision.Kind.unspecified: return org.tigris.subversion.javahl.Revision.START;
	            case Revision.Kind.number: return org.tigris.subversion.javahl.Revision.getInstance(((Revision.Number)rev).getNumber());
	            case Revision.Kind.date:
	            default:
	            	return org.tigris.subversion.javahl.Revision.getInstance(((Revision.DateSpec)rev).getDate());
			}
		}
		return null;
	}
	
	public static NotifyInformation convert(org.tigris.subversion.javahl.NotifyInformation info) {
		return new NotifyInformation(info.getPath(), info.getAction(), info.getKind(), info.getMimeType(), ConversionUtility.convert(info.getLock()), info.getErrMsg(), info.getContentState(), info.getPropState(), info.getLockState(), info.getRevision());
	}
	
	public static Notify2 convert(org.tigris.subversion.javahl.Notify2 notify2) {
		return notify2 == null ? null : ((Notify2Wrapper)notify2).getNotify2();
	}
	
	public static org.tigris.subversion.javahl.Notify2 convert(Notify2 notify2) {
		return notify2 == null ? null : new Notify2Wrapper(notify2);
	}
	
	public static class Notify2Wrapper implements org.tigris.subversion.javahl.Notify2 {
		protected Notify2 notify;
		
		public Notify2Wrapper(Notify2 notify) {
			this.notify = notify;
		}
		
		public Notify2 getNotify2() {
			return this.notify;
		}
		
		public void onNotify(org.tigris.subversion.javahl.NotifyInformation info) {
			this.notify.onNotify(ConversionUtility.convert(info));
		}
	}
	
	private ConversionUtility() {
		
	}
}
