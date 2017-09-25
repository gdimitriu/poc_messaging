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

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

/**
 * @author Gabriel Dimitriu
 *
 */
public class AuctionAggregate implements Aggregate {

	static String PROC_AUCTIONID = "AuctionID";
	static String ITEMID = "ItemID";
	static String VENDOR = "Vendor";
	static String PRICE = "Price";
	
	private Session session = null;
	private Auction auction = null;
	
	public AuctionAggregate(Session session) {
		this.session = session;
		this.auction = new Auction();
	}

	/* (non-Javadoc)
	 * @see enterprise_integration_patterns_ex.aggregator.Aggregate#addMessage(javax.jms.Message)
	 */
	@Override
	public void addMessage(Message message) {
		Bid bid = null;
		if (message instanceof MapMessage) {
			try {
				MapMessage mapmsg = (MapMessage) message;
				String auctionID = mapmsg.getString(PROC_AUCTIONID);
				String itemID = mapmsg.getString(ITEMID);
				String vendor = mapmsg.getString(VENDOR);
				double price = mapmsg.getDouble(PRICE);
				bid = new Bid(auctionID, itemID, vendor, price);
				auction.addBid(bid);
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see enterprise_integration_patterns_ex.aggregator.Aggregate#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return auction.isComplete();
	}

	/* (non-Javadoc)
	 * @see enterprise_integration_patterns_ex.aggregator.Aggregate#getResultMessage()
	 */
	@Override
	public Message getResultMessage() {
		Bid bid = auction.getBestBid();
		try {
			MapMessage msg = session.createMapMessage();
			msg.setStringProperty(PROC_AUCTIONID, bid.getCorrelationID());
			msg.setString(ITEMID, bid.getItemID());
			msg.setString(VENDOR, bid.getVendor());
			msg.setDouble(PRICE, bid.getPrice());
			return msg;
		} catch (JMSException e) {
			e.printStackTrace();
			return null;
		}
	}

}
