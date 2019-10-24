/**
    Copyright (c) 2018 Gabriel Dimitriu All rights reserved.
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
		ConnectionFactory cf = ActiveMQJMSClient.createConnectionFactory(IConstants.TCP_LOCALHOST, "connector");
		Connection connection = cf.createConnection("user", "password");
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
		ConnectionFactory cf = ActiveMQJMSClient.createConnectionFactory(IConstants.TCP_LOCALHOST, "connector");
		Connection connection = cf.createConnection("user", "password");
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
