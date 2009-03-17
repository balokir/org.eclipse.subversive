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

package org.eclipse.team.svn.projectset;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.csc.dip.projectset.AbstractProjectReferenceAnalyser;

/**
 * SVN project set reference analyzer extension point implementation
 * 
 * @author Alexander Gurov
 */
public class SVNProjectReferenceAnalyser extends AbstractProjectReferenceAnalyser {
	public SVNProjectReferenceAnalyser() {
		super();
	}

	public boolean areEqual(String projectReference1, String projectReference2) {
		String data1[] = projectReference1.split(",");
		String data2[] = projectReference2.split(",");
		return data1[0].equals(data2[0]) && data1[1].equals(data2[1]) && data1[2].equals(data2[2]);
	}

	public String getLocation(String projectReference) {
		String []data = projectReference.split(",", 4);
		return data[1];
	}

	public String getProjectName(String projectReference) {
		String []data = projectReference.split(",", 4);
		return data[2];
	}

	public String getProviderName() {
		return "SVN";
	}

	public String getTag(String projectReference) {
		IPath url = new Path(this.getLocation(projectReference));
		String name = url.lastSegment();
		if (!name.equalsIgnoreCase("trunk")) {
			url = url.removeLastSegments(1);
			String structureNode = url.lastSegment();
			if (structureNode.equalsIgnoreCase("tags") || structureNode.equalsIgnoreCase("branches")) {
				return name;
			}
		}
		return "TRUNK";
	}

}
