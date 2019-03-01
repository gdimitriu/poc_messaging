/**
 * 
 */
package activemq.topic;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
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
public class TopicDurableSuscriber {

	private static final String UNSUSCRIBE = "unsuscribe";
	private static final String DURABLE = "durable";
	private ConnectionFactory cf = null;
	private Connection connection = null;

	private Session session = null;
	private MessageConsumer consumer = null;
	
	/**
	 * 
	 */
	public TopicDurableSuscriber() {
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			new TopicDurableSuscriber().startConsumerServer(UNSUSCRIBE, "0");
		} else if (args.length == 2){
			new TopicDurableSuscriber().startConsumerServer(args[0], args[1]);
		} else if (args.length == 1) {
			new TopicDurableSuscriber().startConsumerServer(UNSUSCRIBE, args[0]);
		}
	}
	public void startConsumerServer(final String command, final String id) {
		try {
			cf = ActiveMQJMSClient.createConnectionFactory(IConstants.TCP_LOCALHOST, "connector");
			connection = cf.createConnection("user", "password");
			connection.setClientID(DURABLE + id);
			connection.start();			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			consumer = session.createDurableSubscriber(session.createTopic(IConstants.TOPIC_QUEUE),DURABLE);
			for (int i = 0 ; i < 20; i++){
				Message message = consumer.receive();
				String text = null;
				if (message instanceof TextMessage) {
					text = ((TextMessage) message).getText();
					System.out.println(text);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (consumer != null) {
					consumer.close();
				}
			} catch (JMSException e2) {
				e2.printStackTrace();
			}
			if (UNSUSCRIBE.equals(command)) {
				try {
					session.unsubscribe(DURABLE);
				} catch (JMSException e2) {
					e2.printStackTrace();
				}
			}
			try {
				if (session != null) {
					session.close();
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
			try {
				if (connection != null) {
					connection.stop();
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
}
