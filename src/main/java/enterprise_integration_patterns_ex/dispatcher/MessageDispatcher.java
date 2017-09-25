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
package enterprise_integration_patterns_ex.dispatcher;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

/**
 * @author Gabriel Dimitriu
 *
 */
public class MessageDispatcher {

	private MessageConsumer consumer = null;
	private int nextID = 1;
	
	protected MessageDispatcher() {
		super();
	}
	
	public static MessageDispatcher newDispatcher(Connection connection, String queueName) throws JMSException {
		MessageDispatcher dispatcher = new MessageDispatcher();
		dispatcher.initialize(connection, queueName);
		return dispatcher;
	}

	private void initialize(Connection connection, String queueName) throws JMSException {
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination dispatcherQueue = session.createQueue(queueName);
		consumer = session.createConsumer(dispatcherQueue);
	}
	
	public void receiveSync() throws JMSException {
		Message message = consumer.receive();
		Performer performer = new Performer(nextID++, message);
		new Thread(performer).start();
	}
}
