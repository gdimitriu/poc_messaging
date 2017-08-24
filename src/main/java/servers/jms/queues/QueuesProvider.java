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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Dimitriu
 *
 */
public class QueuesProvider {

	/** singleton provider */
	private static QueuesProvider singleton = new QueuesProvider();
	/** system queues */
	private List<String> systemQueues = new ArrayList<String>();
	/**
	 * 
	 */
	private QueuesProvider() {
		systemQueues.add(IQueueNameConstants.LOGIN);
		systemQueues.add(IQueueNameConstants.TRANSFORMER);
		systemQueues.add(IQueueNameConstants.FLOW);
		systemQueues.add(IQueueNameConstants.FEEDBACK);
	}

	
	/**
	 * get all the queue from the system.
	 * @return all queues.
	 */
	public List<String> getAllQueues() {
		return systemQueues;
	}
	
	
	/**
	 * get the singleton instance.
	 * @return singleton instance.
	 */
	public static QueuesProvider getInstance() {
		return singleton;
	}
}
