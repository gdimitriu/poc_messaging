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
package enterprise_integration_patterns_ex.publisher_subscriber;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;

/**
 * @author Gabriel Dimitriu
 *
 */
public class PullSubjectGateway {

	private PullSubject subject = null;
	private Connection connection = null;
	private Session session = null;
	private MessageProducer updateProducer = null;
	public static final String UPDATE_QUEUE_NAME = "jms/Update";
	
	/**
	 * 
	 */
	private PullSubjectGateway() {
		super();
	}
	
	public static PullSubjectGateway newGateway(EmbeddedJMS jmsServer, String factoryName, PullSubject subject) throws JMSException {
		PullSubjectGateway gateway = new PullSubjectGateway();
		gateway.initialize(jmsServer, factoryName, subject);
		return gateway;
	}

	private void initialize(EmbeddedJMS jmsServer, String factoryName, PullSubject subject) throws JMSException {
		this.subject = subject;
		ConnectionFactory cf = (ConnectionFactory) jmsServer.lookup(factoryName);
		connection = cf.createConnection();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createQueue(UPDATE_QUEUE_NAME);
		updateProducer = session.createProducer(destination);
		new Thread(new GetStateReplier()).start();
		connection.start();
	}
	
	public void notifyNoState() throws JMSException {
		TextMessage message = session.createTextMessage();
		updateProducer.send(message);
	}
	
	public void release() throws JMSException {
		if (connection != null) {
			connection.stop();
			connection.close();
		}
	}
	
	private class GetStateReplier implements Runnable, MessageListener {

		public static final String GET_STATE_QUEUE_NAME = "jms/GetState";
		private Session session = null;
		private MessageConsumer requestConsumer = null;
		
		@Override
		public void onMessage(Message message) {
			try {
				Destination replyQueue = message.getJMSReplyTo();
				MessageProducer replayProducer = session.createProducer(replyQueue);
				Message replyMessage = session.createTextMessage(subject.getState());
				replayProducer.send(replyMessage);
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				Destination getStateQueue = session.createQueue(GET_STATE_QUEUE_NAME);
				requestConsumer = session.createConsumer(getStateQueue);
				requestConsumer.setMessageListener(this);
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		
	}
}
