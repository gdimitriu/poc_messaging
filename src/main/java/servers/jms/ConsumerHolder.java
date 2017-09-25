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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
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
	
	private IResourceProducerConsumer resource = null;
		
	/** connection used internally */
	private Connection connection = null;

	private ConnectionFactory cf = null;
	
	public ConsumerHolder(final EmbeddedJMS server, final IResourceProducerConsumer resource) {
		jmsServer = server;
		this.resource = resource;
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

			Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);

			Queue queue = (Queue) jmsServer.lookup("queue/" + resource.getQueueName());

			MessageConsumer consumer = session.createConsumer(queue);
			
			MessageProducer replyTo = session.createProducer(null);
			
			resource.setReplyTo(session, replyTo);
			
			resource.setCurrentConnection(connection);
			
			consumer.setMessageListener(resource);

			connection.start();
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * stop the consumer.
	 */
	public void stop() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
