package server.hazelcast.sessions;

import com.hazelcast.collection.IQueue;
import com.hazelcast.collection.ISet;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.transaction.*;
import com.hazelcast.transaction.TransactionOptions.TransactionType;

public class SessionExamples {

	private HazelcastInstance hz = null;
	private int position  = 0;
	public SessionExamples(final int pos) {
		Config cfg = new Config();
		hz = Hazelcast.newHazelcastInstance(cfg);
		position = pos;
	}
	
	public void transaction1() {
		IQueue<String> queue = hz.getQueue("myQueue");
		queue.offer(new String("val" + position));
	}
	
	public void transaction2() {
		TransactionOptions options = new TransactionOptions().setTransactionType(TransactionType.TWO_PHASE);
		TransactionContext context = hz.newTransactionContext(options);
		context.beginTransaction();
		TransactionalQueue<String> queue = context.getQueue("myQueue");
		TransactionalMap<Integer, String> map = context.getMap("myMap");
		TransactionalSet<String> set = context.getSet("mySet");
		
		try {
			if (position == 1) {
				String obj1 = queue.poll();
				System.out.println(position + " value=" + obj1);
			}
			String obj = queue.poll();
			System.out.println(position + " value=" + obj);
			map.put(position, "value" + position);
			set.add("value" + position);
			if (position == 0) {
				Thread.sleep(1000);
				throw new Exception("roolback");
			}
			context.commitTransaction();
			if (position == 1) {
				Thread.sleep(1000);
			}
		} catch (Throwable th) {
			context.rollbackTransaction();
		}
		
	}
	
	public void transaction3() {
		IQueue<String> queue = hz.getQueue("myQueue");
		IMap<Integer, String> map = hz.getMap("myMap");
		ISet<String> set = hz.getSet("mySet");
		queue.stream().forEach(a -> System.out.println("queue pos=" + position + " value=" + a));
		map.entrySet().stream().forEach(a -> System.out.println("map pos=" + position + " key=" + a.getKey() + " value=" + a.getValue()));
		set.stream().forEach(a -> System.out.println("set pos=" + position + " value=" + a));
	}

	public static void main(String[] args) {
		Thread[] threads = new Thread[2];
		SessionExamples[] examples = new SessionExamples[threads.length];
		for (int i = 0 ; i < threads.length; i++) {
			examples[i] = new SessionExamples(i);
		}
		for (int i = 0; i < threads.length; i++) {
			final int pos = i;
			threads[i] = new Thread() {
				@Override
				public void run() {
					examples[pos].transaction1();
					examples[pos].transaction2();
					examples[pos].transaction3();
				};
			};			
		}
		for (Thread th : threads) {
			th.start();
		}
		for (Thread th : threads) {
			try {
				th.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (SessionExamples ex : examples) {
			ex.disconnect();
		}
	}

	private void disconnect() {
		hz.shutdown();
	}

}
