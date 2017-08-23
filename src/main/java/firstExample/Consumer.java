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
package firstExample;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.StreamMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * first attempt to create a customer.
 * @author Gabriel Dimitriu
 *
 */
public class Consumer {

	/**
	 * 
	 */
	public Consumer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Consumer consumer = new Consumer();
		try {
			consumer.consume();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void consume() throws JMSException {
		ConnectionFactory connFactory = new ActiveMQConnectionFactory();
		Connection connection = connFactory.createConnection("user", "passwd");
		
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		Destination destination = session.createQueue(Constants.FIRST_EXAMPLE_QUEUE);
		
		MessageConsumer consumer = session.createConsumer(destination);
		
		connection.start();
		Message message = consumer.receive();
		if (message instanceof StreamMessage) {
			System.out.println(((StreamMessage) message).readString());
			System.out.println(((StreamMessage) message).readBoolean());
		}
		connection.close();
	}
}
