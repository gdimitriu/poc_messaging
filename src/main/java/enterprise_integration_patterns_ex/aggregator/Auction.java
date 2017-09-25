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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Gabriel Dimitriu
 *
 */
public class Auction {

	private List<Bid> bids = new ArrayList<Bid>();
	
	public void addBid(Bid bid) {
		bids.add(bid);
		System.out.println(bids.size() + "  Bids in auction.");
	}

	public boolean isComplete() {
		return (bids.size() >= 3);
	}

	public Bid getBestBid() {
		Bid bestBid = null;
		Iterator<Bid> iter = bids.iterator();
		if (iter.hasNext()) {
			bestBid = (Bid) iter.next();
			while(iter.hasNext()) {
				Bid b = (Bid) iter.next();
				if(b.getPrice() < bestBid.getPrice()) {
					bestBid = b;
				}
			}
		}
		return bestBid;
	}

}
