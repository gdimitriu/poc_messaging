/**
 * 
 */
package server.hazelcast.generals;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.hazelcast.core.IMap;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;

/**
 * @author Gabriel Dimitriu
 *
 */
public class GeneralsServer {

	private static final String GENERALS = "generals";
	private static final String WEHRMACHT = "wehrmacht";
	private static final String USARMY = "usarmy";
	private static final String REDARMY = "redarmy";

	/**
	 * 
	 */
	public GeneralsServer() {
		Config cfg = new Config(WEHRMACHT);
		HazelcastInstance hz = Hazelcast.newHazelcastInstance(cfg);
		Map<String, General> mapGenerals = hz.getMap(GENERALS);
		Queue<General> queueGenerals = hz.getQueue(GENERALS);
		IAtomicLong server = hz.getAtomicLong("cluster");
		if (server.get() == 0) {
			populateMap(mapGenerals, server);
		} else if (server.get() == 1){
			 updateMap(mapGenerals, server);
			 putInQueueByYear(mapGenerals,queueGenerals);
		} else if (server.get() == 2) {
			System.out.println(WEHRMACHT + " Generals:");
			mapGenerals.values().stream().forEach(a -> System.out.println(a));
			EntryObject entry = new PredicateBuilder().getEntryObject();
			Predicate predicate = entry.get("deathYear").greaterThan(1945);
			Set<General> surviving = (Set<General>) ((IMap) mapGenerals).values(predicate);
			System.out.println("Surviving generals:");
			surviving.stream().forEach(a -> System.out.println(a));
			System.out.println("Queue of Generals:");
			queueGenerals.stream().forEach(a -> System.out.println(a));
		}
	}

	private void putInQueueByYear(final Map<String, General> mapGenerals, final Queue<General> queueGenerals) {
		mapGenerals.values().stream().sorted().forEach(a -> queueGenerals.offer(a));		
	}

	/**
	 * @param mapGenerals
	 * @param server
	 */
	private void updateMap(Map<String, General> mapGenerals, IAtomicLong server) {
		Collection<General> generals = mapGenerals.values();
		 for (General general : generals) {
			 if (general.getSecondName().equals("Rommel")) {
				 general.setDeathYear(1944);
			 }
			 if (general.getSecondName().equals("Rundstedt")) {
				 general.setDeathYear(1953);
			 }
			 if (general.getSecondName().equals("Manstein")) {
				 general.setDeathYear(1973);
			 }
			 updateGeneral(general);
		 }
		 server.addAndGet(1);
	}

	/**
	 * @param mapGenerals
	 * @param server
	 */
	private void populateMap(Map<String, General> mapGenerals, IAtomicLong server) {
		mapGenerals.put("1", new General("Erwing", "Rommel", "1"));
		mapGenerals.put("2", new General("Gerd", "Rundstedt", "2"));
		mapGenerals.put("3", new General("Erich", "Manstein", "3"));
		server.addAndGet(1);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GeneralsServer server = new GeneralsServer();
	}
	
	public General getGeneral(final String id) {
		ConcurrentMap<String, General> map = Hazelcast.getHazelcastInstanceByName(WEHRMACHT).getMap(GENERALS);
		General general = map.get(id);
		if (general == null) {
			general = new General(id);
			general = map.putIfAbsent(id, general);
		}
		return general;
	}

	public boolean updateGeneral(final General general) {
		ConcurrentMap<String, General> map = Hazelcast.getHazelcastInstanceByName(WEHRMACHT).getMap(GENERALS);
		return (map.replace(general.getId(), general) != null);
	}
	
	public boolean removeGeneral(final General general) {
		ConcurrentMap<String, General> map = Hazelcast.getHazelcastInstanceByName(WEHRMACHT).getMap(GENERALS);
		return map.remove(general.getId(), general);
	}
}
