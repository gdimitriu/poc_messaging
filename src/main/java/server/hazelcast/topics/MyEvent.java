/**
 * 
 */
package server.hazelcast.topics;

import java.io.Serializable;

/**
 * @author Gabriel Dimitriu
 *
 */
public class MyEvent implements Serializable{

	private String message; 
	/**
	 * 
	 */
	public MyEvent(final String msg) {
		this.message = msg;
	}

	@Override
	public String toString() {
		return message;
	}
}
