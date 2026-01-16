/**
 * Copyright (c) 2024, Openflexo
 *
 * This file is part of Pamela-core, a component of the software infrastructure
 * developed at Openflexo.
 *
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either
 * version 1.1 of the License, or any later version), which is available at
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any
 * later version), which is available at http://www.gnu.org/licenses/gpl.html.
 */

package org.openflexo.pamela.sync;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vector Clock implementation for tracking causality in distributed PAMELA instances.
 * Used to establish happened-before relationships between operations from different replicas.
 * 
 * @author PAMELA Team
 */
public class VectorClock implements Serializable, Comparable<VectorClock> {

	private static final long serialVersionUID = 1L;

	private final Map<String, Long> clock;

	public VectorClock() {
		this.clock = new ConcurrentHashMap<>();
	}

	public VectorClock(Map<String, Long> initialClock) {
		this.clock = new ConcurrentHashMap<>(initialClock);
	}

	/**
	 * Copy constructor
	 */
	public VectorClock(VectorClock other) {
		this.clock = new ConcurrentHashMap<>(other.clock);
	}

	/**
	 * Increment the clock for a specific replica
	 * 
	 * @param replicaId the replica identifier
	 * @return the new clock value for this replica
	 */
	public synchronized long increment(String replicaId) {
		long newValue = clock.getOrDefault(replicaId, 0L) + 1;
		clock.put(replicaId, newValue);
		return newValue;
	}

	/**
	 * Get the clock value for a specific replica
	 * 
	 * @param replicaId the replica identifier
	 * @return the clock value, or 0 if not set
	 */
	public long get(String replicaId) {
		return clock.getOrDefault(replicaId, 0L);
	}

	/**
	 * Set the clock value for a specific replica
	 * 
	 * @param replicaId the replica identifier
	 * @param value the clock value
	 */
	public synchronized void set(String replicaId, long value) {
		clock.put(replicaId, value);
	}

	/**
	 * Merge this vector clock with another one, taking the maximum of each component
	 * 
	 * @param other the other vector clock to merge with
	 */
	public synchronized void merge(VectorClock other) {
		for (Map.Entry<String, Long> entry : other.clock.entrySet()) {
			clock.merge(entry.getKey(), entry.getValue(), Math::max);
		}
	}

	/**
	 * Check if this vector clock happened before another one
	 * 
	 * @param other the other vector clock
	 * @return true if this clock happened before the other
	 */
	public boolean happenedBefore(VectorClock other) {
		boolean atLeastOneLess = false;

		for (String replicaId : clock.keySet()) {
			long thisValue = this.get(replicaId);
			long otherValue = other.get(replicaId);

			if (thisValue > otherValue) {
				return false;
			}
			if (thisValue < otherValue) {
				atLeastOneLess = true;
			}
		}

		// Check for replicas in other but not in this
		for (String replicaId : other.clock.keySet()) {
			if (!clock.containsKey(replicaId) && other.get(replicaId) > 0) {
				atLeastOneLess = true;
			}
		}

		return atLeastOneLess;
	}

	/**
	 * Check if this vector clock is concurrent with another one
	 * (neither happened before the other)
	 * 
	 * @param other the other vector clock
	 * @return true if the clocks are concurrent
	 */
	public boolean isConcurrent(VectorClock other) {
		return !this.happenedBefore(other) && !other.happenedBefore(this) && !this.equals(other);
	}

	/**
	 * Create a copy of this vector clock
	 * 
	 * @return a new VectorClock with the same values
	 */
	public VectorClock copy() {
		return new VectorClock(this);
	}

	/**
	 * Get all clock entries
	 * 
	 * @return a copy of the internal clock map
	 */
	public Map<String, Long> getClockMap() {
		return new HashMap<>(clock);
	}

	@Override
	public int compareTo(VectorClock other) {
		if (this.happenedBefore(other)) {
			return -1;
		} else if (other.happenedBefore(this)) {
			return 1;
		}
		return 0; // Concurrent or equal
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		VectorClock other = (VectorClock) obj;
		return clock.equals(other.clock);
	}

	@Override
	public int hashCode() {
		return clock.hashCode();
	}

	@Override
	public String toString() {
		return "VectorClock" + clock;
	}
}
