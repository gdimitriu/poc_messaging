/**
    Copyright (c) 2019 Gabriel Dimitriu All rights reserved.
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

package server.solace.generals;

import javax.jms.Connection;
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

import com.solacesystems.jcsmp.DurableTopicEndpoint;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;

import server.activemq.generals.Constants;
import server.hazelcast.generals.General;
import server.solace.generals.SolaceUtils;

/**
 * @author Gabriel Dimitriu
 *
 */
public class GeneralsServerWithProvision implements MessageListener {

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
	public GeneralsServerWithProvision() {
		// TODO Auto-generated constructor stub
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
		GeneralsServerWithProvision server = new GeneralsServerWithProvision();
		try {
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
	}
	
	public void createQueuesAndTopics() throws Exception {
		final EndpointProperties endpointProps = new EndpointProperties();
        // set queue permissions to "consume" and access-type to "exclusive"
        endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
        endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
        // Actually provision it, and do not fail if it already exists
        JCSMPSession solaceSession = SolaceUtils.createJMSPSession(); 
        solaceSession.connect();
        com.solacesystems.jcsmp.Queue queue = JCSMPFactory.onlyInstance().createQueue(Constants.WEHRMACHT + Constants.GENERALS);        
        solaceSession.provision(queue, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
        queue = JCSMPFactory.onlyInstance().createQueue(Constants.ALLIES + Constants.GENERALS);        
        solaceSession.provision(queue, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
        DurableTopicEndpoint topic = JCSMPFactory.onlyInstance().createDurableTopicEndpoint(Constants.COMMAND);
        solaceSession.provision(topic, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
        solaceSession.closeSession();
        
		commandConnection = SolaceUtils.createConnection();		
		commandConnection.start();
		commandSession = commandConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		queueW = commandSession.createQueue(Constants.WEHRMACHT + Constants.GENERALS);
		queueA = commandSession.createQueue(Constants.ALLIES + Constants.GENERALS);
		commands = commandSession.createTopic(Constants.COMMAND);
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
		Connection connection = SolaceUtils.createConnection();
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
			
				try {
					connection = SolaceUtils.createConnection();
					connection.start();
					session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				} catch (Exception e) {
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
