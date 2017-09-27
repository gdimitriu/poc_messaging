/**
    Copyright (c) 2017 Gabriel Dimitriu All rights reserved.
	DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

    This file is part of poc_messaging project.

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
package servers.jms.queues.client;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.StreamMessage;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;

import servers.jms.queues.IQueueNameConstants;

/**
 * @author Gabriel Dimitriu
 *
 */
public class ConsumerFailOver {

	

	/**
	 * 
	 */
	public ConsumerFailOver() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws JMSException 
	 */
	public static void main(String[] args) {
		ConsumerFailOver producer = new ConsumerFailOver();
		try {
			producer.produce();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void produce() throws Exception {
		ConnectionFactory connFactory = ActiveMQJMSClient.createConnectionFactory("tcp://localhost:61616", "default");
		Connection connection = connFactory.createConnection("system","system");
//do no need for queue
//		connection.setClientID("system_system");
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		Queue destination = session.createQueue(IQueueNameConstants.TRANSACTION_RETURN);
		
		
		MessageConsumer consumer = session.createConsumer(destination);
		
		connection.start();
		Message receivedMessage = consumer.receive();
		
		if (receivedMessage instanceof StreamMessage) {
			System.out.println("received:" + ((StreamMessage) receivedMessage).readString());
			System.out.println("received " + receivedMessage.getJMSMessageID() + " message correlation " + receivedMessage.getJMSCorrelationID());
		}
		
		session.close();
		connection.close();
	}
}
