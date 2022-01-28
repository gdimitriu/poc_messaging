/**
 * 
 */
package server.hazelcast.topics;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;

/**
 * @author Gabriel Dimitriu
 *
 */
public class SampleTopics implements MessageListener<MyEvent> {

	/**
	 * 
	 */
	public SampleTopics() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SampleTopics sample = new SampleTopics();
		Config cfg = new Config();
		HazelcastInstance hz = Hazelcast.newHazelcastInstance(cfg);
		ITopic<MyEvent> topic = hz.getTopic("default");
		topic.addMessageListener(sample);
		topic.publish(new MyEvent("test message"));
		
	}

	@Override
	public void onMessage(Message<MyEvent> message) {
		MyEvent myEvent = message.getMessageObject();
		System.out.println("Message received = " + myEvent.toString());
	}

}
