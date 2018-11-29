/**
 * 
 */
package server.activemq.generals;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import embedded_listeners.IConstants;
import server.hazelcast.generals.General;

/**
 * @author Gabriel Dimitriu
 *
 */
public class PopulateWhermacht {
	//list of generals
	private List<General> generals = new ArrayList<>(); 

	/**
	 * @throws Exception 
	 * @throws URISyntaxException 
	 * 
	 */
	public PopulateWhermacht() throws URISyntaxException, Exception {
		Random random = new Random();
		generals.add(new General("Erwing", "Rommel", "1", 1944, random.nextInt()));
		generals.add(new General("Gerd", "Rundstedt", "2", 1953, random.nextInt()));
		generals.add(new General("Erich", "Manstein", "3", 1973, random.nextInt()));
        try {
        	populateQueue();
        	sendTopic();
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new PopulateWhermacht();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendTopic() throws Exception {
		ConnectionFactory cf = ActiveMQJMSClient.createConnectionFactory(IConstants.TCP_LOCALHOST, "connector");
		Connection conn = cf.createConnection("user", "password");
		Session session = conn.createSession(false,  Session.AUTO_ACKNOWLEDGE);
		Topic topic = session.createTopic(Constants.COMMAND);
		MessageProducer producer = session.createProducer(topic);
		TextMessage message = session.createTextMessage(Constants.COMMAND_WEHRMACHT);
		producer.send(message);
		try {
			session.close();
		} catch (JMSException e) {
		}
		try {
			conn.stop();
		} catch (JMSException e) {
		}		
	}

	private void populateQueue() throws Exception {
		ConnectionFactory cf = ActiveMQJMSClient.createConnectionFactory(IConstants.TCP_LOCALHOST, "connector");
		Connection conn = cf.createConnection("user", "password");
		Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createQueue(Constants.WEHRMACHT + Constants.GENERALS);
		MessageProducer producer = session.createProducer(destination);
		conn.start();
		try {
			for (General general : generals) {
				ObjectMessage msg = session.createObjectMessage(general);
				producer.send(msg);
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
				conn.stop();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
