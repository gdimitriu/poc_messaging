/**
 * 
 */
package server.solace.generals;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import server.activemq.generals.Constants;
import server.hazelcast.generals.General;

/**
 * @author Gabriel Dimitriu
 *
 */
public class PopulateAllies {
	//list of generals
	private List<General> generals = new ArrayList<>(); 
	/**
	 * @throws Exception 
	 * @throws URISyntaxException 
	 * 
	 */
	public PopulateAllies() throws URISyntaxException, Exception {
		Random random = new Random();
		generals.add(new General("Georgy", "Zhukov", "1", 1974, random.nextInt()));
		generals.add(new General("Aleksandr", "Vasilievsky","2", 1977, random.nextInt()));
		generals.add(new General("George", "Patton", "1", 1945, random.nextInt()));
        try {
        	populateQueue();
        	sendTopic();
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	private void sendTopic() throws Exception {
		
		Connection connection = SolaceUtils.createConnection();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Topic topic = session.createTopic(Constants.COMMAND);
		MessageProducer producer = session.createProducer(topic);
		connection.start();
		TextMessage message = session.createTextMessage(Constants.COMMAND_ALLIES);
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

	private void populateQueue() throws Exception {
		Connection connection = SolaceUtils.createConnection();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createQueue(Constants.ALLIES + Constants.GENERALS);
		MessageProducer producer = session.createProducer(destination);
		connection.start();
		try {
			for (General general : generals) {
				ObjectMessage message = session.createObjectMessage(general);
				producer.send(message);
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 5) {
			System.out.println("ussage: host port user passwd vpn");
			System.exit(-1);
		}
		SolaceUtils.setHost(args[0]);
		SolaceUtils.setPort(Integer.parseInt(args[1]));
		SolaceUtils.setUsername(args[2]);
		SolaceUtils.setPassword(args[3]);
		SolaceUtils.setVpn(args[4]);
		try {
			new PopulateAllies();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

}
