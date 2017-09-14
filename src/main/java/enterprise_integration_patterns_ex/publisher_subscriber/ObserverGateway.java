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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;

/**
 * @author Gabriel Dimitriu
 *
 */
public class ObserverGateway extends Observable implements MessageListener {

	private Observer observer = null;
	private Connection connection = null;
	private MessageConsumer updateConsumer = null;

	/**
	 * 
	 */
	private ObserverGateway() {
		super();
	}
	
	public static ObserverGateway newObserverGaterway(EmbeddedJMS jmsServer, String factoryName, Observer observer, String queueName) throws JMSException {
		ObserverGateway gateway = new ObserverGateway();
		gateway.initialize(jmsServer, observer, factoryName, queueName);
		return gateway;
	}

	private void initialize(EmbeddedJMS jmsServer, Observer observer, String factoryName, String queueName) throws JMSException {
		this.observer  = observer;
		
		ConnectionFactory cf = (ConnectionFactory) jmsServer.lookup(factoryName);
		connection = cf.createConnection();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination updateQueue = session.createQueue(queueName);
		updateConsumer = session.createConsumer(updateQueue);
		updateConsumer.setMessageListener(this);
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message message) {			 
			try {
				TextMessage textMessage = (TextMessage) message;
				String newState = textMessage.getText();
				update(newState);
			} catch (JMSException e) {
				e.printStackTrace();
			}
	}

	private void update(String newState) {
		observer.update(this, newState);
	}

	public void attach() throws JMSException {
		connection.start();
	}
	
	public void detach() throws JMSException {
		if (connection != null) {
			connection.stop();
			connection.close();
			connection = null;
		}
	}
}
