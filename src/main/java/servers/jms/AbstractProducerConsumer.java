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

/**
 * @author Gabriel Dimitriu
 *
 */
public abstract class AbstractProducerConsumer implements IJMSRuntimeResource {
	
	private Connection currentConnection = null;
	
	/* (non-Javadoc)
	 * @see servers.jms.IResourceProducerConsumer#getFactoryName()
	 */
	@Override
	public String getFactoryName() {
		return this.getClass().getSimpleName();
	}
	
	public Connection getCurrentConnection() {
		return currentConnection;
	}

	public void setCurrentConnection(Connection currentConnection) {
		this.currentConnection = currentConnection;
	}

}
