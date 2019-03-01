/**
 * 
 */
package activemq.topic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.TopicConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.TopicConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;

/**
 * @author Gabriel Dimitriu
 *
 */
public class TopicServer {

	private EmbeddedJMS jmsServer = null;
	
	/**
	 * 
	 */
	public TopicServer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TopicServer server = new TopicServer();
		try {
			server.startActiveMQEmbeddeServer();
		} catch (Exception e) {
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
		TopicConfiguration topicConfiguration = new TopicConfigurationImpl()
					.setName(IConstants.TOPIC_QUEUE).setBindings("topic/" + IConstants.TOPIC_QUEUE);
		jmsConfiguration.getTopicConfigurations().add(topicConfiguration);
		//Step 5: Start the JMS Server using ActiveMQ Artemis core server with JMS configuration
		jmsServer = new EmbeddedJMS().setConfiguration(configuration).setJmsConfiguration(jmsConfiguration);
		jmsServer.setSecurityManager(new MySecurityManager());
		jmsServer = jmsServer.start();
		System.out.println("JMS server is started");
		
	}
	
	
	private Map<String, Set<Role>> generateRoles() {
		Map<String, Set<Role>> roles = new HashMap<>();
		Role role = new Role("user",true, true, true, true, true, true, true, true, true, true);
		Set<Role> userRole = new HashSet<>();
		userRole.add(role);
		role = new Role("system", true, true, true, true, true, true, true, true, true, true);
		userRole.add(role);
		roles.put(IConstants.TOPIC_QUEUE, userRole);
		roles.put("*", userRole);
		return roles;
	}
}
