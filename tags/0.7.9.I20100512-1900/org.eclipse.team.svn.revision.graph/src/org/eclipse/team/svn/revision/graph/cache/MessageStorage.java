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
package org.eclipse.team.svn.revision.graph.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compactly store messages 
 * 
 * @author Igor Burilo
 */
public class MessageStorage {
	
	protected final static int MIN_PAIRS_COUNT = 5; 

	protected final static int EMPTY_TOKEN = 0;
	
	protected final static char[] separators = new char[]{
		' ', '\t', '\n', '\\', '/', '(', ')',
		'<', '>', '{', '}', '"', '\'', '.', 
		':', '=', '-', '+', '*', '^'};
	
	//words which comprise messages
	protected StringStorage strings = new StringStorage();
	
	//compressed pairs
	protected IndexPairsStorage pairs = new IndexPairsStorage();
	
	/*
	 * message index is equals to corresponding revision index
	 * 
	 * Contains token indexes corresponding to message:
	 *  string indexes are stored as positive
	 *  pair indexes are stored as negative
	 *  0 - is a special case, it's EMPTY_TOKEN
	 */
	protected int[][] messages;
	
	public MessageStorage(int messagesCount) {
		if (messagesCount < 0) {
			throw new IllegalArgumentException("Messages count: " + messagesCount); //$NON-NLS-1$
		}
		
		this.init(messagesCount);
	}
	
	protected final void init(int count) {
		this.messages = new int[count][];		
		for (int i = 0; i < count; i ++) {
			this.messages[i] = new int[0];
		}		
	}
	
	public MessageStorage(byte[] bytes) {
		this.fromBytes(bytes);
	}
	
	public void expandMessagesCount(long messagesCount) {
		if (messagesCount < this.messages.length) {
			throw new IllegalArgumentException("Expand count: " + messagesCount + ", current length: " + this.messages.length); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (this.messages.length != 0) {
			int[][] tmp = this.messages;
			this.messages = new int[(int) messagesCount][];
			System.arraycopy(tmp, 0, this.messages, 0, tmp.length);
			
			for (int i = tmp.length; i < this.messages.length; i ++) {
				this.messages[i] = new int[0];
			}	
		} else {
			this.init((int) messagesCount);			
		}		 
	}
	
	public int add(String message, long revision) {
		if (revision >= this.messages.length) {
			throw new IndexOutOfBoundsException("Revision: " + revision + ", size: " + this.messages.length); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		String[] words = message != null && message.length() > 0 ? splitOnWords(message) : new String[0]; 								
		
		int messageIndex = (int) revision; 
		this.messages[messageIndex] = new int[words.length];
						
		for (int i = 0; i < words.length; i ++) {
			String word = words[i];
			int index = this.strings.add(word);
			this.messages[messageIndex][i] = this.getStringToken(index); 						
		}				
		
		return messageIndex;
	}
	
	public String getMessage(int messageIndex) {
		if (messageIndex < 0 || messageIndex >= this.messages.length) {
			throw new IllegalArgumentException("Index: " + messageIndex + ", size: " + this.messages.length); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		StringBuffer result = new StringBuffer();
		int[] tokens = this.messages[messageIndex];
		for (int i = 0; i < tokens.length; i ++) {			
			result.append(this.getIndexMessage(tokens[i]));								
		}
		return result.length() != 0 ? result.toString() : null;
	}
	
	protected String getIndexMessage(int tokenIndex) {
		StringBuffer result = new StringBuffer();		
		if (this.isPairToken(tokenIndex)) {
			int index = this.getPairIndex(tokenIndex);
			Pair pair = this.pairs.getValue(index);
			result.append(this.getIndexMessage(pair.first));
			result.append(this.getIndexMessage(pair.second));
			
		} else {
			int index = this.getStringIndex(tokenIndex);
			result.append(this.strings.getValue(index));	
		}		
		return result.toString();
	}
	
	protected static String[] splitOnWords(String message) {
		List<String> words = new ArrayList<String>();		
		char[] chars = message.toCharArray();		
		int nextPos = -1;
	    boolean isSeparator = isSeparator(chars[0]);	    
		for (int pos = 0, length = chars.length; pos < length; pos = nextPos) {
			//extract the next word
			for (nextPos = pos + 1; nextPos < length; nextPos ++) {
	            if (isSeparator(chars[nextPos]) != isSeparator) {
	                break;
	            }
			}

	        isSeparator = !isSeparator;
	        if (nextPos + 1 < length && isSeparator(chars[nextPos + 1]) != isSeparator) {
	            nextPos ++;
	            isSeparator = !isSeparator;
	        }
	        
			String word = message.substring(pos, nextPos);			
			words.add(word);
		}
		return words.toArray(new String[0]);
	}
	 
	protected static boolean isSeparator(char ch) {
		for (char separator : separators) {
			if (separator == ch) {
				return true;
			}
		}
		return false;
	}
	
	public void compress() {
		PairsCompressor compressor = new PairsCompressor();
		compressor.run();
	}
	
	protected boolean isPairToken(int token) {
		return token < 0;
	}
	
	protected int getPairIndex(int token) {
		return -token;
	}
	
	protected int getPairToken(int index) {
		return -index;
	}
	
	protected int getStringIndex(int token) {
		return token;
	}
	
	protected int getStringToken(int index) {
		return index;
	}
	
	
	protected static class MutableInteger {
		int value;
		
		public MutableInteger(int value) {
			this.value = value;
		}		
	}
	
	/*
	 * Helper class which allows to compress pairs
	 */
	protected class PairsCompressor {				
		
		/*
		 * Flags which indicate whether we can process message or not
		 * 
		 * This is needed as we don't want to process messages which
		 * didn't have compress pairs in previous compress
		 */
		protected boolean[] messagesForProcessingStatus;
		
		public PairsCompressor() {
			this.messagesForProcessingStatus = new boolean[messages.length];
			Arrays.fill(this.messagesForProcessingStatus, true);			
		}
		
		public int run() {			
			int totalCompressed = 0;			
			while (true) {
				int compressedPairs = this.compress();
				
				if (TimeMeasure.isDebug) {
					System.out.println("compressed pairs: " + compressedPairs);	 //$NON-NLS-1$
				}
				
				if (compressedPairs == 0) {
					break;
				}
				totalCompressed += compressedPairs;			
			}			
			return totalCompressed;
		}
		
		protected int compress() {						
			Map<Pair, MutableInteger> occurrence = this.calculatePairsOccurrence();			
			int compressedPairs = 0;
			if (this.createNewPairs(occurrence)) {
				compressedPairs = this.replaceTokensOnPairs();	
			}
			return compressedPairs;			
		}
		
		protected int replaceTokensOnPairs() {
			int compressedPairs = 0;
			
			//traverse messages
			for (int i = 0; i < this.messagesForProcessingStatus.length; i ++) {
				if (this.messagesForProcessingStatus[i]) {
										
					int[] messageTokens = messages[i];
					int messageCompressedPairs = 0;
					
					if (messageTokens.length >= 2) {
						int previousToken = messageTokens[0];
						
						//traverse tokens
						for (int j = 1; j < messageTokens.length; j ++) {
							int token = messageTokens[j];
							
							Pair pair = new Pair(previousToken, token);
							
							int pairIndex = pairs.find(pair);
							if (pairIndex != RepositoryCache.UNKNOWN_INDEX) {
																							
								messageCompressedPairs ++;
								
								//replace to pair index and empty token
								messageTokens[j - 1] = getPairToken(pairIndex);
								messageTokens[j] = EMPTY_TOKEN;
								
								if (j + 2 < messageTokens.length) {
									previousToken = messageTokens[++ j];
								} else {
									break;
								}
							} else {								
								previousToken = token;	
							}														
						}
					}
					
					if (messageCompressedPairs != 0) {
						compressedPairs += messageCompressedPairs;
						
						//process empties
						int[] newMessageTokens = new int[messageTokens.length - messageCompressedPairs];
						for (int k = 0, num = 0; k < messageTokens.length; k ++) {
							if (messageTokens[k] != EMPTY_TOKEN) {
								newMessageTokens[num ++] = messageTokens[k];
							}
						}	
						messages[i] = newMessageTokens;
					} else {
						this.messagesForProcessingStatus[i] = false;
					}
				}
			}
			
			return compressedPairs;
		}
		
		protected Map<Pair, MutableInteger> calculatePairsOccurrence() {
			Map<Pair, MutableInteger> occurrence = new HashMap<Pair, MutableInteger>();
			
			for (int i = 0; i < this.messagesForProcessingStatus.length; i ++) {				
				if (this.messagesForProcessingStatus[i]) {
					
					int[] messageTokens = messages[i];
					if (messageTokens.length >= 2) {
						int previousToken = messageTokens[0]; 
						for (int j = 1; j < messageTokens.length; j ++) {
							int token = messageTokens[j];
							
							Pair pair = new Pair(previousToken, token);
																					
							MutableInteger count = occurrence.get(pair);
							if (count != null) {
								count.value ++;
							} else {
								occurrence.put(pair, new MutableInteger(1));
							}														
							
							previousToken = token;
						}	
					}					
				}				
			}
			return occurrence;
		}
		
		protected boolean createNewPairs(Map<Pair, MutableInteger> occurrence) {
			boolean hasNewPairs = false;
			for (Map.Entry<Pair, MutableInteger> entry : occurrence.entrySet()) {
				if (entry.getValue().value >= MessageStorage.MIN_PAIRS_COUNT) {
					hasNewPairs = true;
					pairs.add(entry.getKey());
				}
			}
			return hasNewPairs;
		}
				
	}
	
	protected final void fromBytes(byte[] bytes) {
		try {
			DataInput bytesIn = new DataInputStream(new ByteArrayInputStream(bytes));
			
			//strings
			byte[] stringBytes = BytesUtility.readBytesWithLength(bytesIn);
			this.strings = new StringStorage(stringBytes);
			
			//pairs
			byte[] pairBytes = BytesUtility.readBytesWithLength(bytesIn);
			this.pairs = new IndexPairsStorage(pairBytes);
			
			//tokens
			int messagesLength = bytesIn.readInt();
			this.init(messagesLength);
			for (int i = 0; i < messagesLength; i ++) {
				int tokensCount = bytesIn.readInt();
				this.messages[i] = new int[tokensCount];
				for (int j = 0; j < tokensCount; j ++) {
					this.messages[i][j] = bytesIn.readInt();
				}
			}
			
		} catch (IOException e) {
			//ignore
		}
	}
	
	/*
	 * Write:
	 * 
	 * string storage
	 * pairs storage
	 * 
	 * messages count
	 * for each message {
	 *   tokens count
	 * 	 tokens
	 * }
	 */
	public byte[] toBytes() {
		try {
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			DataOutput bytesOut = new DataOutputStream(byteArray);
			
			//strings
			byte[] stringBytes = this.strings.toBytes();
			BytesUtility.writeBytesWithLength(bytesOut, stringBytes);
			
			//pairs
			byte[] pairsBytes = this.pairs.toBytes();
			BytesUtility.writeBytesWithLength(bytesOut, pairsBytes);
			
			//tokens
			bytesOut.writeInt(this.messages.length);
			for (int i = 0; i < this.messages.length; i ++) {
				int[] messageTokens = this.messages[i];
				bytesOut.writeInt(messageTokens.length);
				for (int j = 0; j < messageTokens.length; j ++) {
					bytesOut.writeInt(messageTokens[j]);
				}
			}
			
			return byteArray.toByteArray();
		} catch (IOException e) {
			//ignore
			return new byte[0];
		}	
	}
	
	public static void main(String[] args) {		
		MessageStorage storage = new MessageStorage(2);
		
		int i1 = storage.add("hello world a hello world b hello world f", 0);
		int i2 = storage.add("my hello world d hello world g", 1);
				
		storage.compress();
				
		System.out.println(storage.getMessage(i1));
		System.out.println(storage.getMessage(i2));		
	}

}
