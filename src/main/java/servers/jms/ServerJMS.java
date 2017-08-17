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
import java.util.Arrays;
import java.util.List;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSQueueConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;

/**
 * @author Gabriel Dimitriu
 *
 */
public class ServerJMS implements IServiceJMS {

	/** JMS embedded server */
	private EmbeddedJMS jmsServer = null;
	
	/** JMS communication port */
	private int serverPort = 61616;
	
	/** name of the server */
	private String serverName = null;
	
	//typical to JMS
	
	/** name of the protocol */
	private String protocolType = "tcp";
	
	/** name of the queue */
	private List<String> queuesNames = null;
	
	/** name of the connection factory */
	private String connectionFactoryName = null;
	
	/** name of the journal directory */
	private String journalDirectory = "target/data/journal";
	
	/** name of the server host */
	private String serverHost = "localhost";
	
	/** durability of queue */
	private boolean queueDurable = false;
	
	/** server security is enabled */
	private boolean serverSecurityEnabled = false;
	
	/** server persistence is enabled */
	private boolean serverPersistenceEnabled = false;
	
	public ServerJMS(final int port) {
		serverPort = port;
		queuesNames = new ArrayList<String>();
	}
	
	public ServerJMS() {
		queuesNames = new ArrayList<String>();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IServiceServer server = new ServerJMS();
		try {
			server.startServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stopServer() throws Exception {
		jmsServer.stop();
	}

	@Override
	public void startServer() throws Exception {
		connectionFactoryName = serverName +"_" + serverHost + "_factory";
		// Step 1. Create ActiveMQ Artemis core configuration, and set the properties accordingly
	    Configuration configuration = new ConfigurationImpl().setPersistenceEnabled(serverPersistenceEnabled)
	    		.setJournalDirectory(journalDirectory)
	    		.setSecurityEnabled(serverSecurityEnabled)
	    		.addAcceptorConfiguration(protocolType, protocolType + "://" + serverHost + ":" + serverPort)
	    		.addConnectorConfiguration(serverName, protocolType + "://" + serverHost + ":" + serverPort);

	    // Step 2. Create the JMS configuration
	    JMSConfiguration jmsConfig = new JMSConfigurationImpl();

	    // Step 3. Configure the JMS ConnectionFactory
	    ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl()
	    		.setName(connectionFactoryName).setConnectorNames(Arrays.asList(serverName))
	    		.setBindings(connectionFactoryName);
	    jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

	    // Step 4. Configure the JMS Queue
	    for (String queueName : queuesNames) {
	    	JMSQueueConfiguration queueConfig = new JMSQueueConfigurationImpl()
	    		.setName(queueName).setDurable(queueDurable)
	    		.setBindings("queue/" + queueName);
	    	jmsConfig.getQueueConfigurations().add(queueConfig);
	    }

	    // Step 5. Start the JMS Server using the ActiveMQ Artemis core server and the JMS configuration
	    jmsServer = new EmbeddedJMS().setConfiguration(configuration).setJmsConfiguration(jmsConfig).start();
	    System.out.println("Started Embedded JMS Server");
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
		connectionFactoryName = serverName +"_" + serverHost + "_factory";
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
	public String getQueueName(final int index) {
		return queuesNames.get(index);
	}

	@Override
	public void addQueueName(final String queueName, final int index) {
		this.queuesNames.add(index, queueName);
	}

	@Override
	public String getConnectionFactoryName() {
		return connectionFactoryName;
	}

	@Override
	public void setConnectionFactoryName(final String connectionFactoryName) {
		this.connectionFactoryName = connectionFactoryName;
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
		connectionFactoryName = serverName +"_" + serverHost + "_factory";
	}

	@Override
	public void setQueuesNames(final List<String> queues) {
		this.queuesNames.addAll(queues);
	}

	@Override
	public List<String> getQueuesNames() {
		return queuesNames;
	}

}
