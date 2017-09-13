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
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.camel.component.jms.reply.ReplyManager;

/**
 * Replier
 * @author Gabriel Dimitriu
 *
 */
public class Replier implements MessageListener {

	private Session session;
	private MessageProducer invalidProducer;
	
	/**
	 * 
	 */
	private Replier() {
		super();
	}
	
	public static Replier newReplier(Connection connection, String requestQueueName, String invalidQueueName) throws JMSException {
		Replier replier = new Replier();
		replier.initialize(connection, requestQueueName, invalidQueueName);
		return replier;
	}

	private void initialize(Connection connection, String requestQueueName, String invalidQueueName) throws JMSException {
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination requestQueue = session.createQueue(requestQueueName);
		Destination invalidQueue = session.createQueue(invalidQueueName);
		MessageConsumer requestConsumer = session.createConsumer(requestQueue);
		
		requestConsumer.setMessageListener(this);
		invalidProducer = session.createProducer(invalidQueue);
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message message) {
		try {
			if ((message instanceof TextMessage) && (message.getJMSReplyTo() != null)) {
				TextMessage requestMessage = (TextMessage) message;
				System.out.println("Received request:");
				System.out.println("\tTime: " + System.currentTimeMillis() + " ms");
				System.out.println("\tMessageId :" + requestMessage.getJMSMessageID());
				System.out.println("\tCorrelationId :" + requestMessage.getJMSCorrelationID());
				System.out.println("\tReply to :" + requestMessage.getJMSReplyTo());
				System.out.println("\tContents: " + requestMessage.getText());
				
				String contents = requestMessage.getText();
				Destination replyDestination = requestMessage.getJMSReplyTo();
				MessageProducer replyProducer = session.createProducer(replyDestination);
				
				TextMessage replyMessage = session.createTextMessage();
				replyMessage.setText(contents);
				replyMessage.setJMSCorrelationID(requestMessage.getJMSMessageID());
				replyProducer.send(replyMessage);
				
				System.out.println("Send reply");
				System.out.println("\tTime: " + System.currentTimeMillis() + " ms");
				System.out.println("\tMessageId :" + replyMessage.getJMSMessageID());
				System.out.println("\tCorrelationId:" + replyMessage.getJMSCorrelationID());
				System.out.println("\tReply to:" + replyMessage.getJMSReplyTo());
				System.out.println("\tContents: " + replyMessage.getText());
			} else {
				System.out.println("Invalid message detected:");
				System.out.println("\tType :"  + message.getClass().getName());
				System.out.println("\tTime: " + System.currentTimeMillis() + " ms");
				System.out.println("\tMessageId :" + message.getJMSMessageID());
				System.out.println("\tCorrelationId :" + message.getJMSCorrelationID());
				System.out.println("\tReply to:" + message.getJMSReplyTo());
				
				message.setJMSCorrelationID(message.getJMSCorrelationID());
				invalidProducer.send(message);
				
				System.out.println("Sent to invalid message queue:");
				System.out.println("\tType :"  + message.getClass().getName());
				System.out.println("\tTime: " + System.currentTimeMillis() + " ms");
				System.out.println("\tMessageId :" + message.getJMSMessageID());
				System.out.println("\tCorrelationId :" + message.getJMSCorrelationID());
				System.out.println("\tReply to:" + message.getJMSReplyTo());
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
