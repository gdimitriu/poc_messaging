/**
    Copyright (c) 2017 Gabriel Dimitriu All rights reserved.
	DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

    This file is part of poc_messaging project.
    This file is a copy of a code from Enterprise Integration Patterns,
    	by Gregor Hohpe & Bobby Woolf.

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
package enterprise_integration_patterns_ex.publisher_subscriber;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;

/**
 * @author Gabriel Dimitriu
 *
 */
public class SubjectGateway {

	private Connection connection = null;
	private Session session = null;
	private MessageProducer updateProducer = null;
	
	/**
	 * 
	 */
	private SubjectGateway() {
		super();
	}
	
	public static SubjectGateway newGateway(EmbeddedJMS jmsServer, String factoryName, String updateQueueName) throws JMSException {
		SubjectGateway gateway = new SubjectGateway();
		gateway.initialize(jmsServer, factoryName, updateQueueName);
		return gateway;
	}

	private void initialize(EmbeddedJMS jmsServer, String factoryName, String queueName) throws JMSException {
		ConnectionFactory cf = (ConnectionFactory) jmsServer.lookup(factoryName);
		connection = cf.createConnection();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination updateQueue = session.createQueue(queueName);
		updateProducer = session.createProducer(updateQueue);
		
		connection.start();
	}
	
	public void notify(String state) throws JMSException {
		TextMessage message = session.createTextMessage(state);
		updateProducer.send(message);
	}
	
	public void release() throws JMSException {
		if (connection != null) {
			connection.stop();
			connection.close();
			connection = null;
		}
	}

}
