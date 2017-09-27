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

import java.util.HashMap;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;

import servers.jms.protocol.ICommands;
import servers.jms.protocol.cookies.CookieTransactionsToken;
import servers.jms.queues.IQueueNameConstants;

/**
 * @author Gabriel Dimitriu
 *
 */
public class QueueProducerConsumer {

	

	/**
	 * 
	 */
	public QueueProducerConsumer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws JMSException 
	 */
	public static void main(String[] args) {
		QueueProducerConsumer producer = new QueueProducerConsumer();
		try {
			producer.produce();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void produce() throws Exception {
		ConnectionFactory connFactory = ActiveMQJMSClient.createConnectionFactory("tcp://localhost:61616", "default");
		Connection connection = connFactory.createConnection("system","system");
//		connection.setClientID("system:" + this.getClass().getSimpleName());
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		Destination destination = session.createQueue(IQueueNameConstants.TRANSACTION);
		
		MessageProducer producer = session.createProducer(destination);
		Destination replyTo = session.createQueue(IQueueNameConstants.TRANSACTION_RETURN);
		connection.start();
		
		StreamMessage message = session.createStreamMessage();
		message.setStringProperty(ICommands.COMMAND_PROPERTY, ICommands.LOGIN);
		message.writeString("user");
		message.writeString("passwd");
		message.setJMSReplyTo(replyTo);
		
		producer.send(message);
		
		String messageID = message.getJMSMessageID();				
		
		MessageConsumer consumer = session.createConsumer(replyTo, "JMSCorrelationID = '" + 
				messageID + "'");
		
		Message receivedMessage = consumer.receive();
		
		if (receivedMessage instanceof ObjectMessage) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> retObj = (HashMap<String, Object>) ((ObjectMessage) receivedMessage).getObject();
			System.out.println("received:" + retObj.get("message")); 
			System.out.println("received " + receivedMessage.getJMSMessageID() + " message correlation " + receivedMessage.getJMSCorrelationID());
			CookieTransactionsToken cookie = (CookieTransactionsToken) retObj.get("cookie");
			System.out.println("cookie token: " + cookie.getTransactionId());
		}
		
		session.close();
		connection.close();
	}
}
