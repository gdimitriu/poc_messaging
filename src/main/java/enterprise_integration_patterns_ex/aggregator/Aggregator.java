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
package enterprise_integration_patterns_ex.aggregator;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * @author Gabriel Dimitriu
 *
 */
public class Aggregator implements MessageListener, Runnable {

	static final String PROC_CORRID = "AuctionID";
	
	Map<String, Aggregate> activeAggregates = new HashMap<String, Aggregate>();
	
	Destination inputDest = null;
	Destination outputDest = null;
	
	Session session = null;
	MessageConsumer in = null;
	MessageProducer out = null;
	
	public Aggregator(Destination inDest, Destination outDest, Session session) {
		this.inputDest = inDest;
		this.outputDest = outDest;
		this.session = session;
		
	}
	
	/**
	 * 
	 */
	public Aggregator() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message message) {
		try {
			String correlationID = message.getStringProperty(PROC_CORRID);
			Aggregate aggregate = (Aggregate) activeAggregates.get(correlationID);
			if (aggregate == null) {
				aggregate = new AuctionAggregate(session);
				activeAggregates.put(correlationID, aggregate);
			}
			if (!aggregate.isComplete()) {
				aggregate.addMessage(message);
				if (aggregate.isComplete()) {
					MapMessage result = (MapMessage) aggregate.getResultMessage();
					out.send(result);
				}
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			in = session.createConsumer(inputDest);
			out = session.createProducer(outputDest);
			in.setMessageListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
