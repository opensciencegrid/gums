/*
 * GridUser.java
 *
 * Created on August 20, 2008, 4:00 PM
 */

package gov.bnl.gums;

import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/** 
 * Represent a Logger than only logs the first time (optional) a certain message
 * is passed to it or when warn is invoked, which can be done at an appropriate
 * interval of time.  This is particularly useful for limiting number of emails
 * when log4j is set up to email logs.
 *
 * @author  Gabriele Carcassi, Jay Packard
 */
public class QuietLog {
	Logger log;
	SortedMap messages = Collections.synchronizedSortedMap(new TreeMap());
	
	public QuietLog(String logName) {
		log = Logger.getLogger(logName);
	}
	
	public void logMessages() {
		log.warn(createMessage());
		messages.clear();
	}
	
	/*
	 * If this is the first type of this error, log it immediately,
	 * otherwise just add it to messages to be logged later
	 */
	public void put(String key, String message, boolean logImmediately) {
		if (!messages.containsKey(key) && logImmediately)
			log.error(message);
		messages.put(key, message);
	}
	
	public boolean hasMessages() {
		return messages.size()>0;
	}
	
	private String createMessage() {
		synchronized(messages) {
			Iterator it = messages.values().iterator();
			StringBuffer buffer = new StringBuffer();
			while (it.hasNext()) {
				buffer.append((String)it.next());
				buffer.append("\n");
			}
			return buffer.toString();
		}
	}
}
