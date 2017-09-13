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
package servers.jms.protocol;

/**
 * @author Gabriel Dimitriu
 *
 */
public enum JmsTransactionProtocol {

	/**
	 * this has the following capabilities.
	 * - it has to receive a login
	 * - it has to receive a cookie (userData from REST)
	 * - it has to receive an add Operation
	 * - it has to receive a transform Operation
	 * - it has to receive a logof operation
	 * - the logon, add, transform, logof operation are routed to the internal queues.
	 */
}
