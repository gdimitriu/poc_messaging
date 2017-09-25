/**
    Copyright (c) 2017 Gabriel Dimitriu All rights reserved.
	DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

    This file is part of poc_messaging project.
    This file is a copy of a code from Enterprise Integration Patterns,
    	by Gregor Hohpe & Bobby Woolf.

    poc_messaging is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    poc_messaging is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with poc_messaging.  If not, see <http://www.gnu.org/licenses/>.
 */
package enterprise_integration_patterns_ex.dispatcher;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author Gabriel Dimitriu
 *
 */
public class Performer implements Runnable {

	private int performerID = 0;
	private Message message;
	
	public Performer(int id, Message message) {
		performerID = id;
		this.message = message; 
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			processMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processMessage() throws JMSException, InterruptedException {
		int id = message.getIntProperty("cust_id");
		
		System.out.println(System.currentTimeMillis() + ": Performer #" + performerID + " starting; message ID = " + id);
		Thread.sleep(500);
		System.out.println(System.currentTimeMillis() + ": Performer #" + performerID + " processing");
		Thread.sleep(500);
		System.out.println(System.currentTimeMillis() + ": Performer #" + performerID + " finished");
	}

}
