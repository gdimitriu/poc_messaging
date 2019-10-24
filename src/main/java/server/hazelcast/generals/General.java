/**
    Copyright (c) 2018 Gabriel Dimitriu All rights reserved.
	DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

    This file is part of poc_messaging project.

    poc_messaging is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    poc_messaging is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with poc_messaging.  If not, see <http://www.gnu.org/licenses/>.
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
