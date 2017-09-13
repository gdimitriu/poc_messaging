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

import servers.jms.queues.QueuesProvider;
import servers.jms.queues.RCAddTransformer;
import servers.jms.queues.RCFlow;
import servers.jms.queues.RCransactionRouter;
import servers.jms.queues.RCAuthentication;

/**
 * @author Gabriel Dimitriu
 *
 */
public class MainServer {

	/**
	 * 
	 */
	public MainServer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IServiceServer server = new ServerJMS();
		IServiceJMS serverJms = (IServiceJMS) server;
		serverJms.setName("MainServer");
		serverJms.setQueuesNames(QueuesProvider.getInstance().getAllQueues());
		serverJms.registerResourceConsumer(new RCransactionRouter());
		serverJms.registerResourceConsumer(new RCAddTransformer());
		serverJms.registerResourceConsumer(new RCFlow());
		serverJms.registerResourceConsumer(new RCAuthentication());
		try {
			server.startServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
