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
package servers.jms.queues;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.StreamMessage;

import servers.jms.AbstractProducerConsumer;
import servers.jms.protocol.ICommands;

/**
 * Router for input messages to the correct internal queues.
 * @author Gabriel Dimitriu
 *
 */
public class RCTransactionRouter extends AbstractProducerConsumer {

	/**
	 * 
	 */
	public RCTransactionRouter() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see servers.jms.IResourceProducerConsumer#getQueueName()
	 */
	@Override
	public String getQueueName() {
		// TODO Auto-generated method stub
		return IQueueNameConstants.TRANSACTION;
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message message) {
		if (message instanceof StreamMessage) {
			StreamMessage strMsg = (StreamMessage) message;
			Destination replyTo = null;
			try {
				replyTo = message.getJMSReplyTo();
			} catch (JMSException e2) {
				e2.printStackTrace();
			}
			try {
				strMsg.setJMSCorrelationID(strMsg.getJMSMessageID());
				
				//read the command
				String command = strMsg.getStringProperty(ICommands.COMMAND_PROPERTY);
				if (ICommands.getAllCommands().contains(command)) {
					String route = RouteProvider.getInstance().getRouteQueueName(command);
					routeMessage(route, strMsg);
				}
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					returnErrorMessage(replyTo, e, message);
				} catch (JMSException e1) {
					e1.printStackTrace();
				}
				return;
			}
		}

	}

	private void returnErrorMessage(Destination replyTo, JMSException exception, Message message) throws JMSException{
		StreamMessage retMsg = getSession().createStreamMessage();
		retMsg.writeString(exception.getLocalizedMessage());
		retMsg.setJMSCorrelationID(message.getJMSMessageID());
		getSession().createProducer(replyTo).send(retMsg);
	}

	private void routeMessage(String route, StreamMessage strMsg) throws JMSException {
		Destination destination = getSession().createQueue(route);
		MessageProducer producer = getSession().createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		producer.send(strMsg);
	}

}
