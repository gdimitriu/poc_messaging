/**
 * 
 */
package server.hazelcast.generals;

import java.io.Serializable;

/**
 * @author Gabriel Dimitriu
 *
 */
public class BattleEvent implements Serializable {

	private String event = "";
	/**
	 * 
	 */
	public BattleEvent(final String event) {
		this.event = event;
	}
	
	public String getEvent() {
		return this.event;
	}

}
