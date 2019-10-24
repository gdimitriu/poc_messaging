/**
    Copyright (c) 2018 Gabriel Dimitriu All rights reserved.
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

package server.activemq.generals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSQueueConfiguration;
import org.apache.activemq.artemis.jms.server.config.TopicConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.TopicConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;

import embedded_listeners.IConstants;
import server.hazelcast.generals.General;

/**
 * @author Gabriel Dimitriu
 *
 */
public class GeneralsServer implements MessageListener {

	private EmbeddedJMS jmsServer = null;
	private Connection connection = null;
	private Connection commandConnection = null;
	private Session session = null;
	private Session commandSession = null;
	private Queue queueW = null;
	private Queue queueA = null;
	private Topic commands = null;
	
	
	/**
	 * 
	 */
	public GeneralsServer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GeneralsServer server = new GeneralsServer();
		try {
			server.startActiveMQEmbeddeServer();
			server.createQueuesAndTopics();
			server.waitForBattle();
			server.closeAll();
		} catch (Exception e) {
			e.printStackTrace();
		} 

	}

	private void closeAll() {
		try {
			commandSession.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			commandConnection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			jmsServer.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startActiveMQEmbeddeServer() throws Exception {
		//Step 1: Create ActiveMQ Artemis core configuration and the the properties
		Configuration configuration = new ConfigurationImpl().setPersistenceEnabled(true)
				.setJournalDirectory("target/data/journal")
				.addAcceptorConfiguration("tcp", IConstants.TCP_LOCALHOST)
				.addConnectorConfiguration("connector", IConstants.TCP_LOCALHOST);
		configuration = configuration.setSecurityEnabled(true).setSecurityRoles(generateRoles());
		//Step 2: Create JMS configuration
		JMSConfiguration jmsConfiguration = new JMSConfigurationImpl();
		
		//Step 3: Configuration the JMS ConnectionFactory
		ConnectionFactoryConfiguration cfConfiguration = new ConnectionFactoryConfigurationImpl().setName(IConstants.FACTORY_NAME)
				.setConnectorNames(Arrays.asList("connector")).setBindings(IConstants.FACTORY_NAME);
		jmsConfiguration.getConnectionFactoryConfigurations().add(cfConfiguration);
		
		//Step 4: Configure the JMS Queue
		JMSQueueConfiguration queueConfiguration = new JMSQueueConfigurationImpl()
					.setName(Constants.WEHRMACHT + Constants.GENERALS).setDurable(false).setBindings("queue/" + Constants.WEHRMACHT + Constants.GENERALS);
		jmsConfiguration.getQueueConfigurations().add(queueConfiguration);
		queueConfiguration = new JMSQueueConfigurationImpl()
					.setName(Constants.ALLIES + Constants.GENERALS).setDurable(false).setBindings("queue/" + Constants.ALLIES + Constants.GENERALS);
		jmsConfiguration.getQueueConfigurations().add(queueConfiguration);
		TopicConfiguration topicConfiguration = new TopicConfigurationImpl()
					.setName(Constants.COMMAND).setBindings("topic/" + Constants.COMMAND);
		jmsConfiguration.getTopicConfigurations().add(topicConfiguration);
		
		//Step 5: Start the JMS Server using ActiveMQ Artemis core server with JMS configuration
		jmsServer = new EmbeddedJMS().setConfiguration(configuration).setJmsConfiguration(jmsConfiguration);
		jmsServer.setSecurityManager(new MySecurityManager());
		jmsServer = jmsServer.start();
		System.out.println("JMS server is started");
	}
	
	public void createQueuesAndTopics() throws JMSException {
		queueW = (Queue) jmsServer.lookup("queue/" + Constants.WEHRMACHT + Constants.GENERALS);
		queueA = (Queue) jmsServer.lookup("queue/" + Constants.ALLIES + Constants.GENERALS);
		commands = (Topic) jmsServer.lookup("topic/" + Constants.COMMAND);
		ConnectionFactory cf = (ConnectionFactory) jmsServer.lookup(IConstants.FACTORY_NAME);
		commandConnection = cf.createConnection("user", "password");
		commandConnection.start();
		commandSession = commandConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	public void waitForBattle() throws Exception {
		MessageConsumer consumer = commandSession.createConsumer(commands);
		commandConnection.start();
		int nrSteps = 0;
		while(nrSteps < 2) {
			Message message = consumer.receive();
			if (message instanceof TextMessage) {
				switch (((TextMessage) message).getText()) {
				case Constants.COMMAND_WEHRMACHT :
					System.out.println(((TextMessage) message).getText());
					nrSteps++;
					break;
				case Constants.COMMAND_ALLIES :
					System.out.println(((TextMessage) message).getText());
					nrSteps++;
					break;
				}
			}
		}
		consumer.setMessageListener(this);
		sendBattle();
	}
	
	private void sendBattle() throws Exception {
		ConnectionFactory cf = ActiveMQJMSClient.createConnectionFactory(IConstants.TCP_LOCALHOST, "connector");
		Connection connection = cf.createConnection("user", "password");
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Topic topic = session.createTopic(Constants.COMMAND);
		MessageProducer producer = session.createProducer(topic);
		connection.start();
		TextMessage message = session.createTextMessage(Constants.COMMAND_BATTLE);
		producer.send(message);
		try {
			session.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			connection.stop();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private General readGeneral(Message german) throws JMSException {
		if (german instanceof ObjectMessage) {
			ObjectMessage objectMessage = (ObjectMessage) german;
			return (General)objectMessage.getObject();
	    }
		return null;
	}
	
	private Map<String, Set<Role>> generateRoles() {
		Map<String, Set<Role>> roles = new HashMap<>();
		Role role = new Role("user",true, true, false, false, true, true, false, false, false, false);
		Set<Role> userRole = new HashSet<>();
		userRole.add(role);
		role = new Role("system", true, true, true, true, true, true, true, true, true, true);
		userRole.add(role);
		roles.put(Constants.WEHRMACHT + Constants.GENERALS, userRole);
		roles.put(Constants.ALLIES + Constants.GENERALS, userRole);
		roles.put("*", userRole);
		return roles;
	}

	@Override
	public void onMessage(final Message message) {
		try {
			if (!(message instanceof TextMessage && Constants.COMMAND_BATTLE.equals(((TextMessage)message).getText()))) {
				return;
			}
		} catch (JMSException e2) {
			e2.printStackTrace();
			return;
		}
		//Step 6: LookUp JMS resources defined in the configuration
			ConnectionFactory cf = (ConnectionFactory) jmsServer.lookup(IConstants.FACTORY_NAME);
				try {
					connection = cf.createConnection("user", "password");
					connection.start();
					session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					MessageConsumer consumerW = session.createConsumer(queueW);
					MessageConsumer consumerA = session.createConsumer(queueA);
					General german = readGeneral(consumerW.receive());;
					General allied = readGeneral(consumerA.receive());
					while(german != null && allied != null) {						
						if (german.getScore() < allied.getScore()) {
							System.out.println("allied victory: " + allied.getFirstName() + ":" + allied.getSecondName() + " vs " + german.getFirstName() + ":" + german.getSecondName());
						} else {
							System.out.println("german victory: " + german.getFirstName() + ":" + german.getSecondName() + " vs " + allied.getFirstName() + ":" + allied.getSecondName());
						}
						german = readGeneral(consumerW.receiveNoWait());
						allied = readGeneral(consumerA.receiveNoWait());
					}
				} catch (JMSException e) {
					e.printStackTrace();
				}
		//close all connections
		try {
			connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			session.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
