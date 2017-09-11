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
package embedded_listeners;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.StreamMessage;

/**
 * @author Gabriel Dimitriu
 *
 */
public class Consumer implements MessageListener {

	private Server jmsServer = null;
	
	private MessageProducer replyTo = null;
	
	/**
	 * @param replyTo 
	 * 
	 */
	public Consumer(Server jmsServer, MessageProducer replyTo) {
		this.jmsServer = jmsServer;
		this.replyTo = replyTo;
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message message) {
		if (message instanceof StreamMessage) {
			String rec = null;
			try {
				rec = ((StreamMessage) message).readString();
				System.out.println(rec);
				System.out.println(((StreamMessage) message).readBoolean());
			} catch (JMSException e) {
				e.printStackTrace();
			}
			try {
				StreamMessage sendMessage = jmsServer.session.createStreamMessage();
				sendMessage.writeString("ack for " + rec);
				sendMessage.setJMSMessageID(message.getJMSMessageID());
				sendMessage.setJMSCorrelationID(message.getJMSMessageID());
				replyTo.send(message.getJMSReplyTo(), sendMessage);
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
}
