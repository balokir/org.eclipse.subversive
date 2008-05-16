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

package org.eclipse.team.svn.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * Clipboard remote resource transfer implementation
 * 
 * @author Alexander Gurov
 */
public class RemoteResourceTransfer extends ByteArrayTransfer {
	
	protected static final String TYPE_NAME = RemoteResourceTransferrable.class.getName();
	protected static final int TYPE_ID = Transfer.registerType(RemoteResourceTransfer.TYPE_NAME);
	
	public RemoteResourceTransfer() {
		super();
	}

    public void javaToNative(Object object, TransferData transferData) {
    	if (object == null || 
    		!(object instanceof RemoteResourceTransferrable) || 
			!this.isSupportedType(transferData)) {
    		return;
    	}
    	RemoteResourceTransferrable transferrable = (RemoteResourceTransferrable)object;

    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	try {
    		stream.write(transferrable.operation);
    		if (transferrable.resources != null && transferrable.operation != RemoteResourceTransferrable.OP_NONE) {
            	for (int i = 0; i < transferrable.resources.length; i++) {
            		byte []data = SVNRemoteStorage.instance().repositoryResourceAsBytes(transferrable.resources[i]);
            		stream.write(data.length & 0xFF);
            		stream.write((data.length >> 8) & 0xFF);
            		stream.write(data);
            	}
    		}
    	}
    	catch (RuntimeException ex) {
    		throw ex;
    	}
    	catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    	finally {
        	try {stream.close();} catch (Exception ex) {}
    	}
    	super.javaToNative(stream.toByteArray(), transferData);
    }
    
    public Object nativeToJava(TransferData transferData){
    	if (!this.isSupportedType(transferData)) {
    		return null;
    	}
		byte []bytes = (byte [])super.nativeToJava(transferData);
		if (bytes == null) {
			return null;
		}
		
    	IRemoteStorage storage = SVNRemoteStorage.instance();
		List<IRepositoryResource> retVal = new ArrayList<IRepositoryResource>();
		int operation = RemoteResourceTransferrable.OP_COPY;
    	ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
    	try {
    		if (stream.available() > 0) {
    			operation = stream.read();
    		}
    		if (operation == RemoteResourceTransferrable.OP_NONE) {
    			return null;
    		}
    		while (stream.available() > 0) {
    			int length = stream.read();
    			length |= stream.read() << 8;
    			byte []data = new byte[length];
    			stream.read(data);
    			IRepositoryResource remote = storage.repositoryResourceFromBytes(data);
    			if (remote != null) {
        			retVal.add(remote);
    			}
    		}
    	}
    	catch (RuntimeException ex) {
    		throw ex;
    	}
    	catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    	finally {
        	try {stream.close();} catch (Exception ex) {}
    	}
    	
    	return 
			new RemoteResourceTransferrable(
				retVal.toArray(new IRepositoryResource[retVal.size()]),
				operation);
    }
    
    protected String []getTypeNames(){
    	return new String[] {RemoteResourceTransfer.TYPE_NAME};
    }
    
    protected int []getTypeIds(){
    	return new int[] {RemoteResourceTransfer.TYPE_ID};
    }

}
