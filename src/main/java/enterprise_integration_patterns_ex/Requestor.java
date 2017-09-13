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
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Requestor.
 * @author Gabriel Dimitriu
 *
 */
public class Requestor {

	private Session session;	
	private Destination replyQueue;
	private MessageProducer requestProducer;
	private MessageConsumer replyConsumer;
	private MessageProducer invalidProducer;
	/**
	 * 
	 */
	private  Requestor() {
		super();
	}
	
	public static Requestor newRequestor(Connection connection, String requestQueueName, String replyQueueName, String invalidQueueName) throws JMSException {
		Requestor requestor = new Requestor();
		requestor.initialize(connection, requestQueueName, replyQueueName, invalidQueueName);
		return requestor;
	}

	private void initialize(Connection connection, String requestQueueName, String replyQueueName,
			String invalidQueueName) throws JMSException {
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination requestQueue = session.createQueue(requestQueueName);
		replyQueue = session.createQueue(replyQueueName);
		Destination invalidQueue = session.createQueue(invalidQueueName);
		
		requestProducer = session.createProducer(requestQueue);
		replyConsumer = session.createConsumer(replyQueue);
		invalidProducer = session.createProducer(invalidQueue);
	}
	
	public void send() throws JMSException {
		TextMessage requestMessage = session.createTextMessage();
		requestMessage.setText("Hello World !");
		requestMessage.setJMSReplyTo(replyQueue);
		requestProducer.send(requestMessage);
		
		System.out.println("Sent request:");
		System.out.println("\tTime: " + System.currentTimeMillis() + " ms");
		System.out.println("\tMessageId :" + requestMessage.getJMSMessageID());
		System.out.println("\tCorrelationId :" + requestMessage.getJMSCorrelationID());
		System.out.println("\tReply to :" + requestMessage.getJMSReplyTo());
		System.out.println("\tContents: " + requestMessage.getText());
	}

	public void receiveSync() throws JMSException {
		Message msg = replyConsumer.receive();
		if (msg instanceof TextMessage) {
			TextMessage replyMessage = (TextMessage) msg;
			System.out.println("Received reply:");
			System.out.println("\tTime: " + System.currentTimeMillis() + " ms");
			System.out.println("\tMessageId :" + replyMessage.getJMSMessageID());
			System.out.println("\tCorrelationId:" + replyMessage.getJMSCorrelationID());
			System.out.println("\tReply to:" + replyMessage.getJMSReplyTo());
			System.out.println("\tContents: " + replyMessage.getText());
		} else {
			System.out.println("Invalid message detected:");
			System.out.println("\tType :"  + msg.getClass().getName());
			System.out.println("\tTime: " + System.currentTimeMillis() + " ms");
			System.out.println("\tMessageId :" + msg.getJMSMessageID());
			System.out.println("\tCorrelationId :" + msg.getJMSCorrelationID());
			System.out.println("\tReply to:" + msg.getJMSReplyTo());
			
			msg.setJMSCorrelationID(msg.getJMSCorrelationID());
			invalidProducer.send(msg);
			
			System.out.println("Sent to invalid message queue:");
			System.out.println("\tType :"  + msg.getClass().getName());
			System.out.println("\tTime: " + System.currentTimeMillis() + " ms");
			System.out.println("\tMessageId :" + msg.getJMSMessageID());
			System.out.println("\tCorrelationId :" + msg.getJMSCorrelationID());
			System.out.println("\tReply to:" + msg.getJMSReplyTo());
		}
	}
}
