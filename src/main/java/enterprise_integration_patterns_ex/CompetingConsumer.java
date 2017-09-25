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
package enterprise_integration_patterns_ex;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

/**
 * @author Gabriel Dimitriu
 *
 */
public class CompetingConsumer implements Runnable {

	private int performerID;
	private MessageConsumer consumer;
	private boolean isRunning;
	
	/**
	 * 
	 */
	public CompetingConsumer() {
		super();
	}
	
	public static CompetingConsumer newCompetingConsumer(int id, Connection connection, String queueName) throws JMSException {
		CompetingConsumer consumer = new CompetingConsumer();
		consumer.initialize(id, connection, queueName);
		return consumer;
	}
	
	protected void initialize(int id, Connection connection, String queueName) throws JMSException {
		performerID =id;
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination dispatcherQueue = session.createQueue(queueName);
		consumer = session.createConsumer(dispatcherQueue);
		isRunning = true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			while (isRunning()) {
				receiveSync();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private synchronized boolean isRunning() {
		return isRunning;
	}

	public synchronized void stopRunning() {
		isRunning = false;
	}
	private void receiveSync() throws JMSException, InterruptedException {
		Message message = consumer.receive();
		if (message != null) {
			processMessage(message);
		}
	}

	private void processMessage(Message message) throws JMSException, InterruptedException {
		int id = message.getIntProperty("cust_id");
		System.out.println(System.currentTimeMillis() + ": Performer #" + performerID + " starting; message ID " + id);
		Thread.sleep(500);
		System.out.println(System.currentTimeMillis() + ": Performer #" + performerID + " processing");
		Thread.sleep(500);
		System.out.println(System.currentTimeMillis() + ": Performer #" + performerID + " finished");
	}

}
