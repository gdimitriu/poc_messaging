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
package servers.jms;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;

import servers.jms.security.CredentialForRCPProvider;
import servers.jms.security.UserPassword;

/**
 * @author Gabriel Dimitriu
 *
 */
public class ConsumerHolder {
	
	private EmbeddedJMS jmsServer = null;
	
	private IJMSRuntimeResource resource = null;
		
	/** connection used internally */
	private Connection connection = null;

	private ConnectionFactory cf = null;
	
	private List<Session> sessions = null;
	
	public ConsumerHolder(final EmbeddedJMS server, final IJMSRuntimeResource resource) {
		jmsServer = server;
		this.resource = resource;
		sessions = new ArrayList<Session>();
	}

	/**
	 * start the consumer.
	 */
	public void start(){
		try {
			// Step 6. Lookup JMS resources defined in the configuration
			cf = (ConnectionFactory) jmsServer.lookup(resource.getFactoryName());

			UserPassword credentials = CredentialForRCPProvider.getInstance().getCredentials(resource);
			connection = cf.createConnection(credentials.getUser(), credentials.getPasswd());

			resource.setCurrentConnection(connection);

			if (resource.getMaxConcurentInstances() > 1) {
				for (int i = 0; i < resource.getMaxConcurentInstances(); i++) {
					Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
					sessions.add(session);
					Queue queue = (Queue) jmsServer.lookup("queue/" + resource.getQueueName());
					new Thread(new JMSProducerConsumerSessionAware(session, resource, queue)).start();
				}
			} else {
				Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
				sessions.add(session);
				Queue queue = (Queue) jmsServer.lookup("queue/" + resource.getQueueName());
				MessageConsumer consumer = session.createConsumer(queue);
				consumer.setMessageListener(new JMSConsumer(resource));
			}

			connection.start();
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * stop the consumer.
	 */
	public void stop() {
		sessions.stream().forEach(ses -> closeSession(ses));
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void closeSession(Session session) {
		try {
			session.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
