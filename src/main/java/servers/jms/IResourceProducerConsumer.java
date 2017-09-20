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

import javax.jms.Connection;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * @author Gabriel Dimitriu
 *
 */
public interface IResourceProducerConsumer extends MessageListener {

	/**
	 * @return queue name associated with this consumer.
	 */
	public String getQueueName();
	
	
	/**
	 * @return factory name as string
	 */
	public String getFactoryName();
	
	
	public Session getSession();
	
	/**
	 * @return the producer which has to reply.
	 */
	public MessageProducer getReplyTo();
	
	
	/**
	 * @param session the session in which he has to reply.
	 * @param replyTo the producer which has to be reply.
	 */
	public void setReplyTo(final Session session, final MessageProducer replyTo);
	
	public Connection getCurrentConnection();

	public void setCurrentConnection(Connection currentConnection);
}
