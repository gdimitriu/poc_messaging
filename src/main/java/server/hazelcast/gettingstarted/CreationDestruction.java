/**
 * 
 */
package server.hazelcast.gettingstarted;

import java.util.Collection;

import com.hazelcast.config.Config;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.exception.DistributedObjectDestroyedException;

/**
 * @author Gabriel Dimitriu
 *
 */
public class CreationDestruction implements DistributedObjectListener {

	/* (non-Javadoc)
	 * @see com.hazelcast.core.DistributedObjectListener#distributedObjectCreated(com.hazelcast.core.DistributedObjectEvent)
	 */
	@Override
	public void distributedObjectCreated(final DistributedObjectEvent event) {
		DistributedObject instance = event.getDistributedObject();
		System.out.println("Created: " + instance.getName() + "," + instance.getPartitionKey());
	}

	/* (non-Javadoc)
	 * @see com.hazelcast.core.DistributedObjectListener#distributedObjectDestroyed(com.hazelcast.core.DistributedObjectEvent)
	 */
	@Override
	public void distributedObjectDestroyed(final DistributedObjectEvent event) {
		DistributedObject instance = null;
		try {
			 instance = event.getDistributedObject();
		} catch (DistributedObjectDestroyedException ex) {
			System.out.println("Destroyed:" + ex.getMessage());
		}
		if (instance != null) {
			System.out.println("Destroyed: " + instance.getName() + "," + instance.getPartitionKey());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CreationDestruction object = new CreationDestruction();
		Config cfg = new Config();
		HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
		
		instance.addDistributedObjectListener(object);
		
		Collection<DistributedObject> distributedObjects = instance.getDistributedObjects();
		for (DistributedObject distributedObject : distributedObjects) {
			System.out.println(distributedObject.getName() + "," + distributedObject.getPartitionKey());
		}
		
	}

}
