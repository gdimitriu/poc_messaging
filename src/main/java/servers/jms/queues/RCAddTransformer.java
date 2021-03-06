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

import javax.jms.Message;
import javax.jms.Session;

import servers.jms.AbstractProducerConsumer;

/**
 * @author Gabriel Dimitriu
 *
 */
public class RCAddTransformer extends AbstractProducerConsumer {

	/**
	 * 
	 */
	public RCAddTransformer() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see servers.jms.IResourceProducerConsumer#getQueueName()
	 */
	@Override
	public String getQueueName() {
		return IQueueNameConstants.ADD;
	}

	@Override
	public void processMessage(Session session, Message message) {
		// TODO Auto-generated method stub
		
	}
}
