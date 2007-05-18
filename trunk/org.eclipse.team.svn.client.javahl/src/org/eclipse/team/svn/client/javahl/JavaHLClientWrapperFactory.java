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

package org.eclipse.team.svn.client.javahl;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.text.MessageFormat;

import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.extension.factory.ISVNClientWrapperFactory;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.tigris.subversion.javahl.SVNClient;

/**
 * Default implementation. Works with native SVN client.
 * 
 * @author Alexander Gurov
 */
public class JavaHLClientWrapperFactory implements ISVNClientWrapperFactory {
	public static final String CLIENT_ID = "org.eclipse.team.svn.client.javahl";
	
	private static boolean librariesLoaded = false;
	
	public ISVNClientWrapper newInstance() {
		JavaHLClientWrapperFactory.checkLibraries();
		return new SubversionNativeClientProxy();
	}

	public String getName() {
		return JavaHLPlugin.instance().getResource("ClientName");
	}
	
	public String getId() {
		return JavaHLClientWrapperFactory.CLIENT_ID;
	}

	public String getClientVersion() {
		try {
			JavaHLClientWrapperFactory.checkLibraries();
			return SVNClient.version();
		}
		catch (Throwable ex) {
			if (ex.getMessage() != null) {
				String errMessage = JavaHLPlugin.instance().getResource("Error.CannotLoadLibraries0");
				return MessageFormat.format(errMessage, new String[] {ex.getMessage()});
			}
			else {
				return JavaHLPlugin.instance().getResource("Error.CannotLoadLibraries1");
			}
		}
	}

	public String getVersion() {
		return JavaHLPlugin.instance().getVersionString();
	}
	
	public String getCompatibilityVersion() {
		return "1.1.2";
	}
	
	public boolean isReportRevisionChangeAllowed() {
		return false;
	}
	
	public boolean isInteractiveMergeAllowed() {
		return false;
	}

	public boolean isAtomicCommitAllowed() {
		return false;
	}

	public boolean isCompareFoldersAllowed() {
		return false;
	}

	public boolean isFetchLocksAllowed() {
		return false;
	}

	public boolean isProxyOptionsAllowed() {
		return false;
	}

	public boolean isSSHOptionsAllowed() {
		return false;
	}

	public String toString() {
		return this.getId();
	}

	protected static void checkLibraries() {
		if (!JavaHLClientWrapperFactory.librariesLoaded) {
			File parent = new File(JavaHLPlugin.instance().getLocation()).getParentFile();
			String []names = parent.list(new FilenameFilter() {
				private String template = JavaHLClientWrapperFactory.CLIENT_ID + ".win32";
				public boolean accept(File dir, String name) {
					return name.startsWith(this.template) && name.indexOf("feature") == -1;
				}
			});
			if (names != null && names.length > 0) {
				FileUtility.sort(names);
				JavaHLClientWrapperFactory.preloadLibraries(parent.getAbsolutePath() + "/" + names[names.length - 1]);
			}
			// check if loaded
			SVNClient.version();
			JavaHLClientWrapperFactory.librariesLoaded = true;
		}		
	}
	
	protected static void preloadLibraries(String path) {
		final String []javaHL = new String[1];
		new File(path).listFiles(new FileFilter() {
			public boolean accept(File file) {
				String name = file.getName();
				if (name.endsWith(".dll") || name.endsWith(".so")) {
					if (name.indexOf("java") != -1) {
						javaHL[0] = file.getAbsolutePath();
					}
					else {
						System.load(file.getAbsolutePath());
					}
				}
				return false;
			}
		});
		if (javaHL[0] != null) {
			System.load(javaHL[0]);
		}
	}

}
