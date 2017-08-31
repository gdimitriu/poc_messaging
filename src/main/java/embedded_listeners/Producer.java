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
package embedded_listeners;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.StreamMessage;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;

/**
 * first attempt to create a producer.
 * @author Gabriel Dimitriu
 *
 */
public class Producer {

	

	/**
	 * 
	 */
	public Producer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws JMSException 
	 */
	public static void main(String[] args) {
		Producer producer = new Producer();
		try {
			producer.produce();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void produce() throws Exception {
		ConnectionFactory connFactory = ActiveMQJMSClient.createConnectionFactory(IConstants.TCP_LOCALHOST, "connector");
		Connection connection = connFactory.createConnection("user","passwd");
		
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		Destination destination = session.createQueue(IConstants.DATA_QUEUE);
		
		MessageProducer producer = session.createProducer(destination);
		Destination replayTo = session.createTemporaryQueue();
		connection.start();
		
		StreamMessage message = session.createStreamMessage();
		message.writeString("bla bla bla");
		message.writeBoolean(false);
		message.setJMSReplyTo(replayTo);
		
		producer.send(message);
		
		String messageID = message.getJMSMessageID();
		
		session.close();
		
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		
		MessageConsumer consumer = session.createConsumer(replayTo, "JMSCorrelationID = '" + 
				messageID + "'");
		
		Message receivedMessage = consumer.receive();
		
		if (receivedMessage instanceof StreamMessage) {
			System.out.println("received:" + ((StreamMessage) receivedMessage).readString());
			System.out.println("send " + messageID + " messageId" + receivedMessage.getJMSMessageID() + " message correlation " + receivedMessage.getJMSCorrelationID());
		}
		
		session.close();
		connection.close();
	}
}
