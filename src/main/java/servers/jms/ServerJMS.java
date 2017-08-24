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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSQueueConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;

import servers.jms.queues.QueuesProvider;
import servers.jms.security.RolesProvider;
import servers.jms.security.ServerSecurityManager;

/**
 * @author Gabriel Dimitriu
 *
 */
public class ServerJMS implements IServiceJMS {

	/** JMS embedded server */
	private EmbeddedJMS jmsServer = null;
	
	/** configuration for jms */
	private JMSConfiguration jmsConfig = null;
	
	/** JMS communication port */
	private int serverPort = 61616;
	
	/** name of the server */
	private String serverName = null;
	
	//typical to JMS
	
	/** name of the protocol */
	private String protocolType = "tcp";
	
	/** name of the queue */
	private Set<String> queuesNames = null;
	
	/** name of the default connection factory */
	private String connectionDefaultFactoryName = "default";
	
	/** name of the journal directory */
	private String journalDirectory = "target/data/journal";
	
	/** name of the server host */
	private String serverHost = "localhost";
	
	/** durability of queue */
	private boolean queueDurable = false;
	
	/** server security is enabled */
	private boolean serverSecurityEnabled = true;
	
	/** server persistence is enabled */
	private boolean serverPersistenceEnabled = false;
	
	/** map of consumers and queues */
	private Map<String, IResourceProducerConsumer> registeredConsumers = null;
	
	/** map of runtime consumers */
	private Map<String, ConsumerHolder> runtimeConsumers = null;
	
	public ServerJMS(final int port) {
		serverPort = port;
		queuesNames = new HashSet<String>();
		registeredConsumers = new HashMap<String, IResourceProducerConsumer>();
		runtimeConsumers = new HashMap<String, ConsumerHolder>();
	}
	
	public ServerJMS() {
		queuesNames = new HashSet<String>();
		registeredConsumers = new HashMap<String, IResourceProducerConsumer>();
		runtimeConsumers = new HashMap<String, ConsumerHolder>();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IServiceServer server = new ServerJMS();
		((IServiceJMS) server).setQueuesNames(QueuesProvider.getInstance().getAllQueues());
		try {
			server.startServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stopServer() throws Exception {
		runtimeConsumers.entrySet().stream().forEach(runtimeConsumer -> runtimeConsumer.getValue().stop());
		jmsServer.stop();
		runtimeConsumers.clear();
	}

	@Override
	public void startServer() throws Exception {
		if (connectionDefaultFactoryName != null) {
			connectionDefaultFactoryName = serverName +"_" + serverHost + "_factory";
		}
		// Step 1. Create ActiveMQ Artemis core configuration, and set the properties accordingly
	    Configuration configuration = new ConfigurationImpl().setPersistenceEnabled(serverPersistenceEnabled)
	    		.setJournalDirectory(journalDirectory)
	    		.setSecurityEnabled(serverSecurityEnabled)
	    		.addAcceptorConfiguration(protocolType, protocolType + "://" + serverHost + ":" + serverPort)
	    		.addConnectorConfiguration(serverName, protocolType + "://" + serverHost + ":" + serverPort);

	    RolesProvider rolesProvider = new RolesProvider(queuesNames);
	    configuration = configuration.setSecurityRoles(rolesProvider.getRoles());
	    
	    // Step 2. Create the JMS configuration
	    jmsConfig = new JMSConfigurationImpl();

	    // Step 3. Configure the JMS default ConnectionFactory
	    ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl()
	    		.setName(connectionDefaultFactoryName).setConnectorNames(Arrays.asList(serverName))
	    		.setBindings(connectionDefaultFactoryName);
	    jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

	    registeredConsumers.values().stream().forEach(consumer -> registerSpecificFactory(consumer));
	    
	    // Step 4. Configure the JMS Queue
	    for (String queueName : queuesNames) {
	    	JMSQueueConfiguration queueConfig = new JMSQueueConfigurationImpl()
	    		.setName(queueName).setDurable(queueDurable)
	    		.setBindings("queue/" + queueName);
	    	jmsConfig.getQueueConfigurations().add(queueConfig);
	    }

	    // Step 5. Start the JMS Server using the ActiveMQ Artemis core server and the JMS configuration
	    jmsServer = new EmbeddedJMS().setConfiguration(configuration).setJmsConfiguration(jmsConfig);
	    jmsServer.setSecurityManager(new ServerSecurityManager());
	    jmsServer = jmsServer.start();
	    System.out.println("Started Embedded JMS Server");
	    
	    registeredConsumers.entrySet().stream().forEach(consumer -> runtimeConsumers.put(consumer.getKey(), 
	    		new ConsumerHolder(jmsServer, consumer.getValue())));
	    
	    runtimeConsumers.entrySet().stream().forEach(runtimeConsumer -> runtimeConsumer.getValue().start());
	}
	
	private void registerSpecificFactory(final IResourceProducerConsumer consumer) {
		if (consumer.getFactoryName() == null || consumer.getFactoryName().equals(connectionDefaultFactoryName)) {
			return;
		}
		ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl()
	    		.setName(consumer.getFactoryName()).setConnectorNames(Arrays.asList(serverName))
	    		.setBindings(consumer.getFactoryName());
	    jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);
	}
	
	@Override
	protected void finalize() throws Throwable {
		stopServer();
	}
	
	@Override
	public void configure(Object configuration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setName(final String key) {
		serverName = key;
	}

	@Override
	public String getName() {
		return serverName;
	}

	public String getProtocolType() {
		return protocolType;
	}

	@Override
	public void setProtocolType(final String protocolType) {
		this.protocolType = protocolType;
	}

	@Override
	public String[] getQueueNames() {
		return queuesNames.toArray(new String[1]);
	}

	@Override
	public void addQueueName(final String queueName) {
		this.queuesNames.add(queueName);
	}

	@Override
	public String getDefaultConnectionFactoryName() {
		return connectionDefaultFactoryName;
	}

	@Override
	public void setDefaultConnectionFactoryName(final String connectionFactoryName) {
		this.connectionDefaultFactoryName = connectionFactoryName;
	}

	@Override
	public String getJournalDirectory() {
		return journalDirectory;
	}

	@Override
	public void setJournalDirectory(final String journalDirectory) {
		this.journalDirectory = journalDirectory;
	}

	@Override
	public boolean isQueueDurable() {
		return queueDurable;
	}

	@Override
	public void setQueueDurable(final boolean queueDurable) {
		this.queueDurable = queueDurable;
	}

	@Override
	public boolean isServerSecurityEnabled() {
		return serverSecurityEnabled;
	}

	@Override
	public void setServerSecurityEnabled(final boolean serverSecurityEnabled) {
		this.serverSecurityEnabled = serverSecurityEnabled;
	}

	@Override
	public boolean isServerPersistenceEnabled() {
		return serverPersistenceEnabled;
	}

	@Override
	public void setServerPersistenceEnabled(final boolean serverPersistenceEnabled) {
		this.serverPersistenceEnabled = serverPersistenceEnabled;
	}

	@Override
	public String getServerHost() {
		return serverHost;
	}

	@Override
	public void setServerHost(final String serverHost) {
		this.serverHost = serverHost;
	}

	@Override
	public void setQueuesNames(final List<String> queues) {
		this.queuesNames.addAll(queues);
	}

	@Override
	public String registerResourceConsumer(final IResourceProducerConsumer consumer) {
		if (!queuesNames.contains(consumer.getQueueName())) {
			return null;
		}
		String registeredkey = consumer.getQueueName() + ":" + consumer.getFactoryName();
		registeredConsumers.put(registeredkey, consumer);
		return registeredkey;
	}

	@Override
	public IResourceProducerConsumer getRegisterResourceConsumer(final String registeredkey) {
		if (registeredConsumers.containsKey(registeredkey)) {
			registeredConsumers.get(registeredkey);
		}
		return null;
	}

	@Override
	public void unregisterResourceConsumer(final String registeredkey) {
		if (runtimeConsumers.containsKey(registeredkey)) {
			runtimeConsumers.remove(registeredkey).stop();
		}
		if (registeredConsumers.containsKey(registeredkey)) {
			registeredConsumers.remove(registeredkey);
		}
	}

}
