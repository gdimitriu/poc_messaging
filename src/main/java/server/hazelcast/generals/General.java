/**
 * 
 */
package server.hazelcast.generals;

import java.io.Serializable;

/**
 * @author Gabriel Dimitriu
 *
 */
public class General implements Serializable, Comparable<General> {

	/**
	 * default serial number 
	 */
	private static final long serialVersionUID = 1L;
	
	private String firstName;
	private String secondName;
	private String id;
	private int deathYear;
	private int score = 0;
	
	/**
	 * 
	 */
	public General() {
		// TODO Auto-generated constructor stub
	}

	public General(final String firstName, final String secondName, final String id, final int points) {
		this.firstName = firstName;
		this.secondName = secondName;
		this.id = id;
		this.score = points;
	}
	
	public General(final String firstName, final String secondName, final String id, final int deathyear, final int points) {
		this(firstName, secondName, id, points);
		this.deathYear= deathyear;
	}

	public General(final String id) {
		this.id = id;
		this.firstName = "";
		this.secondName = "";
	}
	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @return the secondName
	 */
	public String getSecondName() {
		return secondName;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return the deathYear
	 */
	public int getDeathYear() {
		return deathYear;
	}

	/**
	 * @param deathYear the deathYear to set
	 */
	public void setDeathYear(int deathYear) {
		this.deathYear = deathYear;
	}

	@Override
	public String toString() {
		return firstName + " " + secondName + " died in " + deathYear;
	}

	@Override
	public int compareTo(General o) {
		return Integer.compare(deathYear, o.getDeathYear());
	}
	
	public int getScore() {
		return this.score;
	}
}
