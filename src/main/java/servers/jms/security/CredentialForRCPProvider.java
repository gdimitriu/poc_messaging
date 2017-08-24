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
package servers.jms.security;

import java.util.HashMap;
import java.util.Map;

import servers.jms.queues.RCAddTransformer;
import servers.jms.queues.RCFlow;
import servers.jms.queues.RCLogin;

/**
 * @author Gabriel Dimitriu
 *
 */
public class CredentialForRCPProvider {

	private static CredentialForRCPProvider singleton = new CredentialForRCPProvider();
	
	/** credential storage ... should be in DB */
	private Map<String, UserPassword> credentials = new HashMap<String, UserPassword>(); 
	
	/**
	 * 
	 */
	private CredentialForRCPProvider() {
		credentials.put(RCAddTransformer.class.getCanonicalName(), new UserPassword("system", "system"));
		credentials.put(RCFlow.class.getCanonicalName(), new UserPassword("system", "system"));
		credentials.put(RCLogin.class.getCanonicalName(), new UserPassword("system", "system"));
	}

	/**
	 * get the singleton instance.
	 * @return singleton
	 */
	public static CredentialForRCPProvider getInstance() {
		return singleton;
	}
	
	/**
	 * get the credentials for a service.
	 * @param RCP the Resource Consumer Producer
	 * @return credentials
	 */
	public UserPassword getCredentials(final Object RCP) {
		return credentials.get(RCP.getClass().getCanonicalName());
	}
}
