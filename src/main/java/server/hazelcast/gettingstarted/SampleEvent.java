/**
 * 
 */
package server.hazelcast.gettingstarted;

import com.hazelcast.collection.IQueue;
import com.hazelcast.collection.ISet;
import com.hazelcast.collection.ItemEvent;
import com.hazelcast.collection.ItemListener;
import com.hazelcast.config.Config;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.MapEvent;

/**
 * @author Gabriel Dimitriu
 *
 */
public class SampleEvent implements ItemListener, EntryListener {

    public static void main(String[] args) { 
        SampleEvent sample = new SampleEvent();
        Config cfg = new Config();
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(cfg);
        IQueue queue = hz.getQueue ("default");
        IMap map   = hz.getMap   ("default");
        ISet set   = hz.getSet   ("default");
        //listen for all added/updated/removed entries
        queue.addItemListener(sample, true);
        set.addItemListener  (sample, true); 
        map.addEntryListener (sample, true);        
        //listen for an entry with specific key 
        map.addEntryListener (sample, "keyobj", true);
        queue.offer("Test1");
        queue.add("Test2");
        queue.poll();
        map.put(1, "TestM1");
        map.put(1, "TestM2");
        map.put(2, "TestM2");
        map.delete(1);
    } 

    @Override
    public void entryAdded(EntryEvent event) {
        System.out.println("Entry added key=" + event.getKey() + ", value=" + event.getValue());
    }

    @Override
    public void entryRemoved(EntryEvent event) {
        System.out.println("Entry removed key=" + event.getKey() + ", value=" + event.getValue());
    }

    @Override
    public void entryUpdated(EntryEvent event) {
        System.out.println("Entry update key=" + event.getKey() + ", value=" + event.getValue());
    } 

    @Override
    public void entryEvicted(EntryEvent event) {
        System.out.println("Entry evicted key=" + event.getKey() + ", value=" + event.getValue());
    } 
    
    @Override
    public void itemAdded(ItemEvent item) {
        System.out.println("Item added = " + item);
    }

    @Override
    public void itemRemoved(ItemEvent item) {
        System.out.println("Item removed = " + item);
    }

	@Override
	public void mapCleared(MapEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mapEvicted(MapEvent event) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void entryExpired(EntryEvent entryEvent) {

    }
}