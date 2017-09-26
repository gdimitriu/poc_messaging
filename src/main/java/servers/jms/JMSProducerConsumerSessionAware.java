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
package servers.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;

/**
 * @author Gabriel Dimitriu
 *
 */
public class JMSProducerConsumerSessionAware implements MessageListener, Runnable{

	private IJMSRuntimeResource resource = null;
	private Session currentSession = null;
	private Queue queue = null;
	
	public JMSProducerConsumerSessionAware(Session session, IJMSRuntimeResource res, Queue queue) {
		currentSession = session;
		resource = res;
		this.queue  = queue;
	}
	@Override
	public void onMessage(Message message) {
		resource.processMessage(currentSession, message);
	}
	
	@Override
	public void run() {		
		try {
			MessageConsumer consumer = currentSession.createConsumer(queue);
			consumer.setMessageListener(this);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
	}

}
