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

import java.util.List;

/**
 * @author Gabriel Dimitriu
 *
 */
public interface IServiceJMS extends IServiceServer{
	/**
	 * @return the protocolType
	 */
	public String getProtocolType();

	/**
	 * @param protocolType the protocolType to set
	 */
	public void setProtocolType(final String protocolType);

	/**
	 * @param index the queue name index
	 * @return the queueName
	 */
	public String getQueueName(final int index);

	/**
	 * @param queueName the queueName to set
	 * @param index of the queue
	 */
	public void addQueueName(final String queueName, final int index);
	
	
	/**
	 * set the list of queues
	 * @param queues list of queues
	 */
	public void setQueuesNames(final List<String> queues);
	
	
	/**
	 * get the list of the queues.
	 * @return list of queue names
	 */
	public List<String> getQueuesNames();

	/**
	 * @return the connectionFactoryName
	 */
	public String getConnectionFactoryName();

	/**
	 * @param connectionFactoryName the connectionFactoryName to set
	 */
	public void setConnectionFactoryName(final String connectionFactoryName);

	/**
	 * @return the journalDirectory
	 */
	public String getJournalDirectory();

	/**
	 * @param journalDirectory the journalDirectory to set
	 */
	public void setJournalDirectory(final String journalDirectory);
	
	/**
	 * @return the queueDurable
	 */
	public boolean isQueueDurable();
	
	/**
	 * @param queueDurable the queueDurable to set
	 */
	public void setQueueDurable(final boolean queueDurable);
	
	/**
	 * @return the serverSecurityEnabled
	 */
	public boolean isServerSecurityEnabled();
	
	/**
	 * @param serverSecurityEnabled the serverSecurityEnabled to set
	 */
	public void setServerSecurityEnabled(final boolean serverSecurityEnabled);
	
	/**
	 * @return the serverPersistenceEnabled
	 */
	public boolean isServerPersistenceEnabled();

	/**
	 * @param serverPersistenceEnabled the serverPersistenceEnabled to set
	 */
	public void setServerPersistenceEnabled(final boolean serverPersistenceEnabled);
}
