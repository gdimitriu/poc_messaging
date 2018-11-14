/**
 * 
 */
package server.hazelcast.gettingstarted;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author Gabriel Dimitriu
 *
 */
public class GettingStartedClient {

	/**
	 * 
	 */
	public GettingStartedClient() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
		
		clientNetworkConfig.addAddress("127.0.0.1:5701");
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setNetworkConfig(clientNetworkConfig);
		HazelcastInstance instance = HazelcastClient.newHazelcastClient(clientConfig);
		IMap map = instance.getMap("customers");
		System.out.println("Map Size: " + map.size());
		instance.shutdown();
	}

}
