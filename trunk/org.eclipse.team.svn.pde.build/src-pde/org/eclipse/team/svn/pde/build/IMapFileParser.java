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

package org.eclipse.team.svn.pde.build;

import java.util.Properties;

import org.eclipse.pde.build.IFetchFactory;

/**
 * Map file parser
 * Each parser supports its own map file format
 * 
 * @author Igor Burilo
 */
public interface IMapFileParser {

	public static class FetchData {
		public String url;
		public String tag;		
		public String path;
		public String revision;
		public String peg;
		public String username;
		public String password;
		public String force;
		
	}	
	public FetchData parse(String rawEntry, String []arguments, Properties overrideTags);
	
	public static IMapFileParser DEFAULT = new IMapFileParser() {
		/*
		 * Map file entry format:
		 * mapEntry
		 * 	:	elementType '@' elementID (',' elementVersion)? = svnContent
		 * 	;
		 * elementType
		 * 	:	'bundle' | 'feature' | 'plugin' | 'fragment'
		 * 	;
		 * elementID
		 * 	:	... //plug-in, feature, fragment or bundle ID
		 * 	;
		 * elementVersion
		 *  :	... //plug-in, feature, fragment or bundle version
		 *  ;
		 * svnContent
		 * 	:	'SVN' (',' arg)+
		 * 	;
		 * arg
		 * 	:	key '=' value
		 * 	;
		 * key
		 * 	:	'url'		// project root URL
		 * 	|	'tag'		// optional tag name (trunk, tags/some_name etc.)
		 * 	|	'path'		// optional element, path relative to project root URL
		 * 	|	'revision'	// optional element, revision
		 * 	|	'peg'		// optional element, peg revision
		 * 	|	'username'
		 * 	|	'password'
		 *  |   'force'
		 * 	;
		 */
		public FetchData parse(String rawEntry, String []arguments, Properties overrideTags) {
			FetchData data = new FetchData();			
			for (String argument : arguments) {
				int idx = argument.indexOf(SVNFetchFactory.VALUE_PAIR_SEPARATOR);
				if (idx != -1) {
					String key = argument.substring(0, idx);
					String value = argument.substring(idx + 1);
					
					if (SVNFetchFactory.KEY_URL.equals(key)) {
						data.url = value;
					} else if (SVNFetchFactory.KEY_ELEMENT_TAG.equals(key)) {
						data.tag = value;
					} else if (SVNFetchFactory.KEY_PATH.equals(key)) {
						data.path = value;
					} else if (SVNFetchFactory.KEY_REVISION.equals(key)) {
						data.revision = value;
					} else if (SVNFetchFactory.KEY_PEG.equals(key)) {
						data.peg = value;
					} else if (SVNFetchFactory.KEY_USERNAME.equals(key)) {
						data.username = value;
					} else if (SVNFetchFactory.KEY_PASSWORD.equals(key)) {
						data.password = value;
					} else if (SVNFetchFactory.KEY_FORCE.equals(key)) {
						data.force = value;
					}														
				}				
			}
			
			if (overrideTags != null) {
				String overrideTag = overrideTags.getProperty(SVNFetchFactory.OVERRIDE_TAG);
				if (overrideTag != null && overrideTag.length() > 0) {								
					data.tag = overrideTag;
				}
			}

			// handle optional path
//			if (data.path == null) {
//				data.path = IFetchFactory.KEY_ELEMENT_NAME;
//			}						
			// handle optional tag			
//			if (data.tag == null) {
//				data.tag = ""; //$NON-NLS-1$
//			}
			return data;
		}		
	};
	
	public static IMapFileParser SOURCE_FORGE_PARSER = new IMapFileParser() {
		
		public static final String HEAD ="HEAD"; //$NON-NLS-1$
		public static final String TRUNK = "trunk"; //$NON-NLS-1$
		
		/*
		 * SVN map files format
		 * <type>@<id>=SVN,<tag>[:revision],<svnRepositoryURL>,<preTagPath>,<postTagPath>
		 */
		public FetchData parse(String rawEntry, String[] arguments, Properties overrideTags) {
			FetchData data = new FetchData();
			if (arguments.length < 2) {
				throw new RuntimeException("Incorrect map file entry format, arguments munber should be more than 2. Entry: " + rawEntry); //$NON-NLS-1$
			}						
			
			//url = <svnRepositoryURL> + <preTagPath>
			data.url = arguments[1]; 
			String preTagPath = arguments.length > 2 && !arguments[2].equals("") ? arguments[2] : null; //$NON-NLS-1$
			if (preTagPath != null) {
				data.url += "/" + preTagPath; //$NON-NLS-1$
			}
						
			//path = <postTagPath>
			data.path = arguments.length > 3 && !arguments[3].equals("") ? arguments[3] : null; //$NON-NLS-1$
						
			String tagText = this.getTagText(overrideTags, arguments);
			data.tag = this.getSvnTag(tagText);
			
			data.revision = this.getRevision(tagText);
			data.peg = data.revision;	
			
			data.username = ""; //$NON-NLS-1$
			data.password = ""; //$NON-NLS-1$
			data.force = "true"; //$NON-NLS-1$
			return data;
		}					
		protected String getSvnTag(String tagText) {
			int index = tagText.indexOf(":"); //$NON-NLS-1$
			String string = index > 0 ? tagText.substring(0, index) : tagText;
			if (HEAD.equals(string) || TRUNK.equals(string))
				return TRUNK;
			else
				return string;
		}
		
		protected String getRevision(String tagText) {
			int index=tagText.indexOf(":"); //$NON-NLS-1$
			return index > 0 ? tagText.substring(index+1) : HEAD;
		}
		protected String getTagText(Properties overrideTags, String[] arguments) {
			String overrideTag = overrideTags.getProperty(SVNFetchFactory.OVERRIDE_TAG);
			return overrideTag != null && overrideTag.length()!=0 ? overrideTag : arguments[0];
		}
	};
}
