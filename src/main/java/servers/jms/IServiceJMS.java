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
	 * @return the queueName
	 */
	public String[] getQueueNames();

	/**
	 * @param queueName the queueName to set
	 */
	public void addQueueName(final String queueName);
	
	
	/**
	 * set the list of queues
	 * @param queues list of queues
	 */
	public void setQueuesNames(final List<String> queues);
	
	/**
	 * @return the default connectionFactoryName
	 */
	public String getDefaultConnectionFactoryName();

	/**
	 * @param connectionFactoryName the default connectionFactoryName to set
	 */
	public void setDefaultConnectionFactoryName(final String connectionFactoryName);

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
	
	/**
	 * @param consumer that will consume the resource defined by it's queue.
	 * @return registeredkey
	 */
	public String registerResourceConsumer(final IJMSRuntimeResource consumer);
	
	
	/**
	 * get the resource consumer that was registered
	 * @param registeredkey
	 * @return resource consumer or null;
	 */
	public IJMSRuntimeResource getRegisterResourceConsumer(final String registeredkey);
	
	/**
	 * @param queueName for unregister consumer.
	 */
	public void unregisterResourceConsumer(final String registeredkey);
}
