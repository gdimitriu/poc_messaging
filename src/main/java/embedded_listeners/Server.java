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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSQueueConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.apache.activemq.security.TempDestinationAuthorizationEntry;


/**
 * @author Gabriel Dimitriu
 *
 */
public class Server {

	public EmbeddedJMS jmsServer = null;
	public Connection connection = null;
	public Session session = null;
	
	/**
	 * 
	 */
	public Server() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Server server = new Server();
			server.starteEmbeddedServer();
			server.startConsumerServer();
			server.stopServer();
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Map<String, Set<Role>> generateRoles() {
		TempDestinationAuthorizationEntry temp = new TempDestinationAuthorizationEntry();
		temp.getAdminACLs();
		Map<String, Set<Role>> roles = new HashMap<String, Set<Role>>();
		Role role = new Role("user", true, true, false, false, true, true, false, false, false, false);
		Set<Role> userRole = new HashSet<Role>();
		userRole.add(role);
		roles.put(IConstants.DATA_QUEUE, userRole);
		roles.put(IConstants.RETURN_QUEUE, userRole);
		roles.put("*", userRole);
		return roles;
	}

	public void starteEmbeddedServer() throws Exception {
		 // Step 1. Create ActiveMQ Artemis core configuration, and set the properties accordingly
	      Configuration configuration = new ConfigurationImpl().setPersistenceEnabled(false)
	    		  .setJournalDirectory("target/data/journal")
	    		  .addAcceptorConfiguration("tcp", IConstants.TCP_LOCALHOST)
	    		  .addConnectorConfiguration("connector", IConstants.TCP_LOCALHOST);

	      configuration = configuration.setSecurityEnabled(true)
	    		  .setSecurityRoles(generateRoles());
	      // Step 2. Create the JMS configuration
	      JMSConfiguration jmsConfig = new JMSConfigurationImpl();

	      // Step 3. Configure the JMS ConnectionFactory
	      ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl().setName(IConstants.FACTORY_NAME)
	    		  .setConnectorNames(Arrays.asList("connector")).setBindings(IConstants.FACTORY_NAME);
	      jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

	      // Step 4. Configure the JMS Queue
	      JMSQueueConfiguration queueConfig = new JMSQueueConfigurationImpl()
	    		  .setName(IConstants.DATA_QUEUE).setDurable(false).setBindings("queue/" + IConstants.DATA_QUEUE);
	      jmsConfig.getQueueConfigurations().add(queueConfig);
	      queueConfig = new JMSQueueConfigurationImpl().setName(IConstants.RETURN_QUEUE)
	    		  .setDurable(false).setBindings("queue/" + IConstants.RETURN_QUEUE);
	      jmsConfig.getQueueConfigurations().add(queueConfig);
	      // Step 5. Start the JMS Server using the ActiveMQ Artemis core server and the JMS configuration
	      
	      jmsServer = new EmbeddedJMS().setConfiguration(configuration).setJmsConfiguration(jmsConfig);
	      jmsServer.setSecurityManager(new MySecurityManager());
	      jmsServer = jmsServer.start();
	      System.out.println("Started Embedded JMS Server");
	}
	
	public void startConsumerServer() throws JMSException {
		// Step 6. Lookup JMS resources defined in the configuration
		ConnectionFactory cf = (ConnectionFactory) jmsServer.lookup(IConstants.FACTORY_NAME);

		ConnectionFactory connFactory = cf;

		connection = connFactory.createConnection("user", "passwd");
		
		connection.start();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		Queue queue = (Queue) jmsServer.lookup("queue/" + IConstants.DATA_QUEUE);
		
		MessageProducer replyTo = session.createProducer(null);
		
		MessageConsumer consumer = session.createConsumer(queue);
		
		consumer.setMessageListener(new Consumer(this, replyTo));
	}
	
	private void stopServer() {
		System.out.println(String.format("Hit enter to stop it..."));
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		try {
			jmsServer.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
