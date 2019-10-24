/**
    Copyright (c) 2018 Gabriel Dimitriu All rights reserved.
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

package server.activemq.generals;

import java.util.Set;

import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager;

/**
 * @author Gabriel Dimitriu
 *
 */
public class MySecurityManager implements ActiveMQSecurityManager {

	/**
	 * 
	 */
	public MySecurityManager() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager#validateUser(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean validateUser(String user, String password) {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager#validateUserAndRole(java.lang.String, java.lang.String, java.util.Set, org.apache.activemq.artemis.core.security.CheckType)
	 */
	@Override
	public boolean validateUserAndRole(String user, String password, Set<Role> roles, CheckType checkType) {
		if(!validateUser(user, password)) {
			return false;
		}
		if (checkType.equals(CheckType.CREATE_NON_DURABLE_QUEUE) || checkType.equals(CheckType.DELETE_NON_DURABLE_QUEUE)) {
			return true;
		}
		for (Role role : roles) {
			if(role.getName().equals(user) && checkType.hasRole(role)) {
				return true;
			}
		}
		return false;
	}

}
