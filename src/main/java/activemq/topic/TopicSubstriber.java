/**
 * 
 */
package activemq.topic;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;

/**
 * @author Gabriel Dimitriu
 *
 */
public class TopicSubstriber {
	
	private ConnectionFactory cf = null;
	private Connection connection = null;

	private Session session = null;
	private MessageConsumer consumer = null;

	/**
	 * 
	 */
	public TopicSubstriber() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new TopicSubstriber().startConsumerServer();

	}
	
	public void startConsumerServer() {
		Destination destination = null;		
		try {
			cf = ActiveMQJMSClient.createConnectionFactory(IConstants.TCP_LOCALHOST, "connector");
			connection = cf.createConnection("user", "password");
			connection.start();
			session = connection.createSession(true, Session.SESSION_TRANSACTED);
			destination = session.createTopic(IConstants.TOPIC_QUEUE);
			consumer = session.createConsumer(destination);	
			while(true) {
							
				Message message = consumer.receive();
				String text = null;
				if (message instanceof TextMessage) {
					text = ((TextMessage) message).getText();
					System.out.println(text);
				}
			}
		} catch (Exception e1) {			
			e1.printStackTrace();
			try {
				if (session != null) {
					session.close();
				}
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if (connection != null) {
					connection.stop();
				}
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
	}

}
