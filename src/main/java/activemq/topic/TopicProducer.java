/**
 * 
 */
package activemq.topic;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;

/**
 * @author Gabriel Dimitriu
 *
 */
public class TopicProducer {

	private List<String> messages = new ArrayList<>();
	/**
	 * 
	 */
	public TopicProducer() {
		for (int i = 0; i < 20 ; i++) {
			messages.add("Topic nr=" + i);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new TopicProducer().populateTopics();
	}

	public void populateTopics() {
		ConnectionFactory cf = null;
		Connection connection = null;
		Session session = null;
		Destination destination = null;
		MessageProducer producer = null;
		try {
			cf = ActiveMQJMSClient.createConnectionFactory(IConstants.TCP_LOCALHOST, "connector");
			connection = cf.createConnection("user", "password");
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			destination = session.createTopic(IConstants.TOPIC_QUEUE);
			producer = session.createProducer(destination);
			connection.start();
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
		try {
			int i = 0;
			for (String str : messages) {
				if (i%4 == 0) {
					Thread.sleep(2000);
				}
				TextMessage message = session.createTextMessage(str);
				producer.send(message);
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
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
	}
}
