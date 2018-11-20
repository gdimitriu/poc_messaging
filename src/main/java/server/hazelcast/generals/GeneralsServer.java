/**
 * 
 */
package server.hazelcast.generals;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;

/**
 * @author Gabriel Dimitriu
 *
 */
public class GeneralsServer implements MessageListener<BattleEvent> {

	private static final String GENERALS = "generals";
	private static final String BATTLE = "battle";
	private static final String WEHRMACHT = "wehrmacht";
	private static final String ALLIES = "allies";
	private static final String USARMY = "usarmy";
	private static final String REDARMY = "redarmy";
	private Queue<General> queueWehrmachtGenerals;
	private Queue<General> queueAlliesGenerals;
	private ITopic<BattleEvent> commandTopic;
	private HazelcastInstance hazelcastInstance;
	private long serverOrder = 0;

	/**
	 * 
	 */
	public GeneralsServer() {
		Config cfg = new Config(BATTLE);
		hazelcastInstance = Hazelcast.newHazelcastInstance(cfg);
		queueWehrmachtGenerals = hazelcastInstance.getQueue(WEHRMACHT + GENERALS);
		queueAlliesGenerals = hazelcastInstance.getQueue(ALLIES + GENERALS);
		commandTopic = hazelcastInstance.getTopic("COMMAND");
		commandTopic.addMessageListener(this);
	}

	private void putInQueueByYear(final Map<String, General> mapGenerals, final Queue<General> queueGenerals) {
		mapGenerals.values().stream().sorted().forEach(a -> queueGenerals.offer(a));		
	}

	/**
	 * @param mapGenerals
	 * @param server
	 */
	private void updateWehrmachtMap() {
		Map<String, General> mapWehrmachtGenerals = hazelcastInstance.getMap(WEHRMACHT + GENERALS);
		Collection<General> generals = mapWehrmachtGenerals.values();
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
			 updateGeneral(general, WEHRMACHT);
		 }
		 
	}

	private void populateWehrmachtMap() {
		Map<String, General> mapWehrmachtGenerals = hazelcastInstance.getMap(WEHRMACHT + GENERALS);
		Random random = new Random();
		mapWehrmachtGenerals.put("1", new General("Erwing", "Rommel", "1", random.nextInt()));
		mapWehrmachtGenerals.put("2", new General("Gerd", "Rundstedt", "2", random.nextInt()));
		mapWehrmachtGenerals.put("3", new General("Erich", "Manstein", "3", random.nextInt()));
	}
	
	private void populateRedArmyMap() {
		Map<String, General> generals = hazelcastInstance.getMap(REDARMY + GENERALS);
		Random random = new Random();
		generals.put("1", new General("Georgy", "Zhukov", "1", 1974, random.nextInt()));
		generals.put("2",new General("Aleksandr", "Vasilievsky","2", 1977, random.nextInt()));
	}
	
	private void populateUsArmyMap() {
		Map<String, General> generals = hazelcastInstance.getMap(USARMY + GENERALS);
		Random random = new Random();		
		generals.put("1", new General("George", "Patton", "1", 1945, random.nextInt()));
	}
	
	public void start() {
		serverOrder = hazelcastInstance.getAtomicLong("cluster").addAndGet(1);
		System.out.println("Server Order in cluster:" + serverOrder);
		if (hazelcastInstance.getCluster().getMembers().size() == 3 && serverOrder == hazelcastInstance.getCluster().getMembers().size()) {
			commandTopic.publish(new BattleEvent("start"));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GeneralsServer server = new GeneralsServer();
		server.start();
	}
	
	public General getGeneral(final String id, final String army) {
		ConcurrentMap<String, General> map = Hazelcast.getHazelcastInstanceByName(BATTLE).getMap(army + GENERALS);
		General general = map.get(id);
		if (general == null) {
			general = new General(id);
			general = map.putIfAbsent(id, general);
		}
		return general;
	}

	public boolean updateGeneral(final General general, final String army) {
		ConcurrentMap<String, General> map = Hazelcast.getHazelcastInstanceByName(BATTLE).getMap(army + GENERALS);
		return (map.replace(general.getId(), general) != null);
	}
	
	public boolean removeGeneral(final General general, final String army) {
		ConcurrentMap<String, General> map = Hazelcast.getHazelcastInstanceByName(BATTLE).getMap(army + GENERALS);
		return map.remove(general.getId(), general);
	}

	@Override
	public void onMessage(Message<BattleEvent> message) {	
		BattleEvent event = message.getMessageObject();
		System.out.println("ServerOrder=" + serverOrder + ":" + event.getEvent() + ":" + message.getPublishingMember() + ":" + event);
		if (serverOrder == 1 && "start".equals(event.getEvent())) {
			populateWehrmachtMap();
			populateRedArmyMap();
			populateUsArmyMap();
			commandTopic.publish(new BattleEvent("putInQueue"));
		}
		if (serverOrder == 2 && "putInQueue".equals(event.getEvent())){
			updateWehrmachtMap();
			putInQueueByYear(hazelcastInstance.getMap(WEHRMACHT + GENERALS),queueWehrmachtGenerals);
			putInQueueByYear(hazelcastInstance.getMap(REDARMY + GENERALS),queueAlliesGenerals);
			putInQueueByYear(hazelcastInstance.getMap(USARMY + GENERALS),queueAlliesGenerals);
			System.out.println("Map of wehrmacht has =" + hazelcastInstance.getMap(WEHRMACHT + GENERALS).size());
			System.out.println("Map of red army has =" + hazelcastInstance.getMap(REDARMY + GENERALS).size());
			System.out.println("Map of us army has =" + hazelcastInstance.getMap(USARMY + GENERALS).size());
			commandTopic.publish(new BattleEvent("populated"));
		}
		if (serverOrder == 3 && "populated".equals(event.getEvent())) {
			System.out.println(BATTLE + " Generals:");
			Map<String, General> mapWehrmachtGenerals = hazelcastInstance.getMap(WEHRMACHT + GENERALS);
			mapWehrmachtGenerals.values().stream().forEach(a -> System.out.println(a));
			EntryObject entry = new PredicateBuilder().getEntryObject();
			Predicate predicate = entry.get("deathYear").greaterThan(1945);
			Set<General> surviving = (Set<General>) ((IMap) mapWehrmachtGenerals).values(predicate);
			System.out.println("Surviving generals:");
			surviving.stream().forEach(a -> System.out.println(a));
			System.out.println("Queue of Generals:");
			System.out.println("Nr of Wehrmacht generals=" + queueWehrmachtGenerals.size());
			System.out.println("Nr of allies generals=" + queueAlliesGenerals.size());
			queueWehrmachtGenerals.stream().forEach(a -> System.out.println(a));
			queueAlliesGenerals.stream().forEach(a -> System.out.println(a));
			commandTopic.publish(new BattleEvent("battle"));
		}
		if (serverOrder == 2 && "battle".equals(event.getEvent())) {
			while(!queueWehrmachtGenerals.isEmpty() && !queueAlliesGenerals.isEmpty()) {
				General german = queueWehrmachtGenerals.poll();
				General allied = queueAlliesGenerals.poll();
				if (german.getScore() < allied.getScore()) {
					System.out.println("allied victory: " + allied.getFirstName() + ":" + allied.getSecondName() + " vs " + german.getFirstName() + ":" + german.getSecondName());
				} else {
					System.out.println("german victory: " + german.getFirstName() + ":" + german.getSecondName() + " vs " + allied.getFirstName() + ":" + allied.getSecondName());
				}
			}
		}
	}
}
