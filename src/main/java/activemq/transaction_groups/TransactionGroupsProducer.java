/**
 * 
 */
package activemq.transaction_groups;

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
 * @author gdimitriu
 *
 */
public class TransactionGroupsProducer {

	private List<String> messages = new ArrayList<>();
	private List<String> messagesGroups = new ArrayList<>();
	/**
	 * 
	 */
	public TransactionGroupsProducer() {
		for (int i = 0; i < 20; i++) {
			messages.add(Integer.toString(i));
		}
		for(int i = 0; i< 20; i++) {
			messagesGroups.add("message group=" + i);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new TransactionGroupsProducer().populateTransactionalQueue();
		new TransactionGroupsProducer().populateGroupingQueue();
	}

	private void populateTransactionalQueue(){
		ConnectionFactory cf = null;
		Connection connection = null;
		Session session = null;
		Destination destination = null;
		MessageProducer producer = null;
		try {
			cf = ActiveMQJMSClient.createConnectionFactory(IConstants.TCP_LOCALHOST, "connector");
			connection = cf.createConnection("user", "password");
			session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
			destination = session.createQueue(IConstants.TRANSACTIONAL_QUEUE);
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
			for (String str : messages) {
				TextMessage message = session.createTextMessage(str);
				producer.send(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				session.rollback();
			} catch (JMSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} finally {
			try {
				session.commit();
			} catch (JMSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
	
	private void populateGroupingQueue(){
		ConnectionFactory cf = null;
		Connection connection = null;
		Session session = null;
		Destination destination = null;
		MessageProducer producer = null;
		try {
			cf = ActiveMQJMSClient.createConnectionFactory(IConstants.TCP_LOCALHOST, "connector");
			connection = cf.createConnection("user", "password");
			session = connection.createSession(true, Session.SESSION_TRANSACTED);
			destination = session.createQueue(IConstants.TRANSACTIONAL_QUEUE);
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
			for (String str : messagesGroups) {
				TextMessage message = session.createTextMessage(str);
				message.setStringProperty("JMSXGroupID","myGroup");
				producer.send(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				session.rollback();
			} catch (JMSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} finally {
			try {
				session.commit();
			} catch (JMSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
