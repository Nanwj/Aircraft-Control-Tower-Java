package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.util.Encodable;

import java.util.List;
import java.util.StringJoiner;

/**
 * Abstract representation of a queue containing aircraft.
 */
public abstract class AircraftQueue implements Encodable {

    /**
     * Adds the given aircraft to the queue.
     *
     * @param aircraft aircraft to add to queue
     */
    public abstract void addAircraft(Aircraft aircraft);


    /**
     * Removes and returns the aircraft at the front of the queue.
     * Returns null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    public abstract Aircraft removeAircraft();

    /**
     * Returns the aircraft at the front of the queue without
     * removing it from the queue, or null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    public abstract Aircraft peekAircraft();

    /**
     * Returns a list containing all aircraft in the queue, in order.
     *
     * @return list of all aircraft in queue, in queue order
     */
    public abstract List<Aircraft> getAircraftInOrder();


    /**
     * Returns true if the given aircraft is in the queue.
     *
     * @param aircraft aircraft to find in queue
     * @return true if aircraft is in queue; false otherwise
     */
    public abstract boolean containsAircraft(Aircraft aircraft);

    /**
     * Returns the human-readable string representation of this aircraft queue.
     *
     * @return string representation of this queue
     */
    @Override
    public String toString() {
        // a human-readable string represents the queue
        StringJoiner aircraftInOrder = new StringJoiner(", ", " [", "]");
        for (Aircraft aircraft : this.getAircraftInOrder()) {
            aircraftInOrder.add(aircraft.getCallsign());
        }
        return this.getClass().getSimpleName() + aircraftInOrder.toString();
    }

    /**
     * Returns the machine-readable string representation of this aircraft queue.
     *
     * @return encoded string representation of this aircraft queue
     */
    @Override
    public String encode() {
        // the first line of queue encode
        StringJoiner queue = new StringJoiner(":");
        queue.add(getClass().getSimpleName());
        queue.add("" + getAircraftInOrder().size());

        if (getAircraftInOrder().size() > 0) {
            // the second line of queue encode, if necessary
            StringJoiner aircraftInQueue = new StringJoiner(",");
            for (Aircraft aircraft : getAircraftInOrder()) {
                // join the callsign of aircraft together with ","
                aircraftInQueue.add(aircraft.getCallsign());
            }
            return queue.toString() + System.lineSeparator() + aircraftInQueue.toString();
        } else {
            return queue.toString();
        }
    }
}
