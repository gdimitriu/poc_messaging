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
package embedded;

import java.util.Arrays;
import java.util.Formatter;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.StreamMessage;

import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSQueueConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSQueueConfigurationImpl;

/**
 * first attempt to create a customer.
 * @author Gabriel Dimitriu
 *
 */
public class Consumer {

	static public EmbeddedJMS jmsServer = null;
	/**
	 * 
	 */
	public Consumer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			startServer();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		Consumer consumer = new Consumer();
		try {
			consumer.consume();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void consume() throws JMSException {
	      // Step 6. Lookup JMS resources defined in the configuration
		ConnectionFactory cf = (ConnectionFactory) jmsServer.lookup("cf");

		ConnectionFactory connFactory = cf ; //new ActiveMQConnectionFactory();
		
		Connection connection = connFactory.createConnection();
		
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		Queue queue = (Queue) jmsServer.lookup("queue/" + Constants.FIRST_EXAMPLE_QUEUE);

		MessageConsumer consumer = session.createConsumer(queue);
		
		connection.start();
		Message message = consumer.receive();
		if (message instanceof StreamMessage) {
			System.out.println(((StreamMessage) message).readString());
			System.out.println(((StreamMessage) message).readBoolean());
		}
		connection.close();
		try {
			jmsServer.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void startServer() throws Exception {
		 // Step 1. Create ActiveMQ Artemis core configuration, and set the properties accordingly
	      Configuration configuration = new ConfigurationImpl().setPersistenceEnabled(false).setJournalDirectory("target/data/journal").setSecurityEnabled(false).addAcceptorConfiguration("tcp", "tcp://localhost:61616").
	         addConnectorConfiguration("connector", "tcp://localhost:61616");

	      // Step 2. Create the JMS configuration
	      JMSConfiguration jmsConfig = new JMSConfigurationImpl();

	      // Step 3. Configure the JMS ConnectionFactory
	      ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl().setName("cf").setConnectorNames(Arrays.asList("connector")).setBindings("cf");
	      jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

	      // Step 4. Configure the JMS Queue
	      JMSQueueConfiguration queueConfig = new JMSQueueConfigurationImpl().setName(Constants.FIRST_EXAMPLE_QUEUE).setDurable(false).setBindings("queue/" + Constants.FIRST_EXAMPLE_QUEUE);
	      jmsConfig.getQueueConfigurations().add(queueConfig);

	      // Step 5. Start the JMS Server using the ActiveMQ Artemis core server and the JMS configuration
	      jmsServer = new EmbeddedJMS().setConfiguration(configuration).setJmsConfiguration(jmsConfig).start();
	      System.out.println("Started Embedded JMS Server");
	}
}
