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

import java.util.Observable;
import java.util.Observer;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueRequestor;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;

/**
 * @author Gabriel Dimitriu
 *
 */
public class PullObserverGateway extends Observable implements MessageListener {

	public static final String UPDATE_QUEUE_NAME = "jms/Update";
	public static final String GET_STATE_QUEUE_NAME = "jms/GetState";
	
	private Observer observer = null;
	private MessageConsumer updateConsumer = null;
	
	private QueueRequestor getStateRequestor = null;
	private QueueConnection connection = null;
	private QueueSession session = null;
	/**
	 * 
	 */
	private PullObserverGateway() {
		super();
	}
	
	public static PullObserverGateway newGateway(EmbeddedJMS jmsServer, String factoryName, Observer pullObserver) throws JMSException {
		PullObserverGateway gateway = new PullObserverGateway();
		gateway.initialize(jmsServer, factoryName, pullObserver);
		return gateway;
	}

	private void initialize(EmbeddedJMS jmsServer, String factoryName, Observer pullObserver) throws JMSException {
		this.observer = pullObserver;
		QueueConnectionFactory cf = (QueueConnectionFactory) jmsServer.lookup(factoryName);
		connection  = cf.createQueueConnection();
		session  = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
		Destination updateQueue = session.createQueue(UPDATE_QUEUE_NAME);
		updateConsumer = session.createConsumer(updateQueue);
		updateConsumer.setMessageListener(this);
		
		Queue getStateQueue = (Queue) session.createQueue(GET_STATE_QUEUE_NAME);
		getStateRequestor = new QueueRequestor(session, getStateQueue);
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message message) {
		try {
			updateNoState();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void attach() throws JMSException {
		connection.start();
	}
	
	public void detach() throws JMSException {
		if (connection != null) {
			connection.stop();
			connection.close();
		}
	}
	
	public void updateNoState() throws JMSException {
		TextMessage getStateRequestMessage = session.createTextMessage();
		Message getStateReplyMessage = getStateRequestor.request(getStateRequestMessage);
		if (getStateReplyMessage instanceof TextMessage) {
			TextMessage textMsg = (TextMessage) getStateReplyMessage;
			String newState = textMsg.getText();
			observer.update(this, newState);
		}
	}
}
