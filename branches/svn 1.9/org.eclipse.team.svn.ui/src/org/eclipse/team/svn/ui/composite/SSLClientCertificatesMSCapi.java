/*******************************************************************************
 * Copyright (c) 2000-2011 ilogs information logistics GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Oberlassnig - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.composite;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.ui.utility.ArrayStructuredContentProvider;
import org.eclipse.ui.dialogs.ListDialog;

public class SSLClientCertificatesMSCapi extends ListDialog {
	protected String alias;

	/**
     *
	 * @param parent
     * @param url : the url from which we want to get the root url
	 */
	public SSLClientCertificatesMSCapi(Shell parent, String realm) {
		super(parent);
		// List<String[]> list = new ArrayList<String[]>();
		List list = new ArrayList();
		Provider pmscapi = Security.getProvider("SunMSCAPI");
		Provider pjacapi = Security.getProvider("CAPI");
		try {
			KeyStore keyStore = null;
			//use JACAPI
			if (pmscapi != null) {
				keyStore = KeyStore.getInstance("Windows-MY",pmscapi);
				pmscapi.setProperty("Signature.SHA1withRSA","sun.security.mscapi.RSASignature$SHA1");
			} else if (pjacapi != null) {
				keyStore = KeyStore.getInstance("CAPI");
			}
	        if (keyStore != null) {
	            keyStore.load(null, null);
	            //for (Enumeration<String> aliasEnumeration = keyStore.aliases();aliasEnumeration.hasMoreElements();) {
	            for (Enumeration aliasEnumeration = keyStore.aliases();aliasEnumeration.hasMoreElements();) {
	            	String alias = (String) aliasEnumeration.nextElement();
	            	String issuer = "";
	            	Certificate cert = keyStore.getCertificate(alias);
	            	if (cert instanceof X509Certificate) {
	            		issuer = ((X509Certificate) cert).getIssuerDN().getName();
	            	}
	            	list.add(new String[]{alias,issuer});
	            	//keyStore.getCertificate(alias)
	            }
	        }
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        setTitle("Select Certificate Alias"); //$NON-NLS-1$
        setAddCancelButton(true);
        LabelProvider lp = new LabelProvider(){
        	public String getText(Object element) {
        		if (element == null) {
        			return "";
        		} else if (element instanceof String[] && ((String[]) element).length > 1) {
        			return ((String[]) element)[0] + " | issued by: " + ((String[]) element)[1];
        		} else {
        			return element.toString();
        		}
        	}
        };
        setLabelProvider(lp);
        setMessage("select the right certificate alias"); //$NON-NLS-1$
        
        setContentProvider(new ArrayStructuredContentProvider());
        setInput(list.toArray());
	}


	public String getAlias() {
		if (getResult() != null && getResult().length>0) {
			Object result = getResult()[0];
			if (result instanceof String[]) {
				this.alias = ((String[]) result)[0];
			} else {
				this.alias = (String) result;
			}
		}
		return alias;
	}
}
