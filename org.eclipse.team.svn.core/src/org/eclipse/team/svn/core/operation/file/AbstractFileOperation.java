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

package org.eclipse.team.svn.core.operation.file;

import java.io.File;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;

/**
 * Abstract class which allows to operate with java.io.File
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractFileOperation extends AbstractActionOperation {
	public static final LockingRule EXCLUSIVE = new LockingRule();
	
	private IFileProvider provider;
	private File []files;

	public AbstractFileOperation(String operationName, File []files) {
		super(operationName);
		this.files = files;
	}

	public AbstractFileOperation(String operationName, IFileProvider provider) {
		super(operationName);
		this.provider = provider;
	}

	public ISchedulingRule getSchedulingRule() {
		if (this.files == null) {
			return AbstractFileOperation.EXCLUSIVE;
		}
		HashSet ruleSet = new HashSet();
    	for (int i = 0; i < this.files.length; i++) {
    		ruleSet.add(this.getSchedulingRule(this.files[i]));
    	}
		return ruleSet.size() == 1 ? (ISchedulingRule)ruleSet.iterator().next() : new MultiRule((IResource [])ruleSet.toArray(new IResource[ruleSet.size()]));
	}

	protected File []operableData() {
		return this.files == null ? this.provider.getFiles() : this.files;
	}
	
	protected ISchedulingRule getSchedulingRule(File file) {
		File parent = file.getParentFile();
		return new LockingRule(parent != null ? parent : file);
	}
	
	public static class LockingRule implements ISchedulingRule {
		protected IPath filePath;
		
		// always exclusive
		public LockingRule() {
			this((IPath)null);
		}
		
		public LockingRule(File file) {
			this(file == null ? null : new Path(file.getAbsolutePath()));
		}
		
		public LockingRule(IPath filePath) {
			this.filePath = filePath;
		}
		
		public boolean isConflicting(ISchedulingRule arg) {
			if (arg instanceof LockingRule) {
				LockingRule rule = (LockingRule)arg;
				return 
					this.filePath == null || rule.filePath == null || 
					this.filePath.isPrefixOf(rule.filePath) || rule.filePath.isPrefixOf(this.filePath);
			}
			return false;
		}
		
		public boolean contains(ISchedulingRule arg) {
			if (this == arg) {
				return true;
			}
			if (arg instanceof LockingRule) {
				LockingRule rule = (LockingRule)arg;
				return this.filePath == rule.filePath || this.filePath.isPrefixOf(rule.filePath);
			}
			if (arg instanceof MultiRule) {
				MultiRule rule = (MultiRule)arg;
				ISchedulingRule []children = rule.getChildren();
				for (int i = 0; i < children.length; i++) {
					if (!this.contains(children[i])) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
		
		public int hashCode() {
			return this.filePath == null ? 0 : this.filePath.hashCode();
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof LockingRule) {
				LockingRule rule = (LockingRule)obj;
				return this.filePath == rule.filePath || this.filePath != null && this.filePath.equals(rule.filePath);
			}
			return false;
		}
		
	}
	
}
