/**
 * 
 */
package server.hazelcast.gettingstarted;

import java.util.Map;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author gdimitriu
 *
 */
public class MapOperations {

	private Map<String, String> mapa;
	private Map<Integer, String> mapb;
	private Map<String, String> mapc;
	/**
	 * 
	 */
	public MapOperations() {
		Config cfg = new Config();
		HazelcastInstance hz = Hazelcast.newHazelcastInstance(cfg);
		mapa = hz.getMap("mapa");
		mapb = hz.getMap("mapb");
		mapc = hz.getMap("mapc");
		mapa.put("Key1", "Value1");
		System.out.println(mapc.get("Key1"));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MapOperations map = new MapOperations();

	}

}
