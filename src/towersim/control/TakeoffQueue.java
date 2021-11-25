package towersim.control;

import towersim.aircraft.Aircraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a first-in-first-out (FIFO) queue of aircraft waiting to take off.
 */
public class TakeoffQueue extends AircraftQueue {

    /** A list of Aircraft waiting for take off */
    private List<Aircraft> aircraft;

    /**
     * Constructs a new TakeoffQueue with an initially empty queue of aircraft.
     */
    public TakeoffQueue() {
        aircraft = new ArrayList<>();
    }

    /**
     * Adds the given aircraft to the queue.
     * @param aircraft aircraft to add to queue
     */
    @Override
    public void addAircraft(Aircraft aircraft) {
        this.aircraft.add(aircraft);
    }

    /**
     * Removes and returns the aircraft at the front of the queue.
     * Returns null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft removeAircraft() {
        if (aircraft.size() == 0) {
            return null;
        } else {
            return aircraft.remove(0);
        }
    }

    /**
     * Returns the aircraft at the front of the queue without
     * removing it from the queue, or null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft peekAircraft() {
        if (aircraft.size() == 0) {
            return null;
        } else {
            return aircraft.get(0);
        }
    }

    /**
     * Returns a list containing all aircraft in the queue, in order.
     *
     * @return list of all aircraft in queue, in queue order
     */
    @Override
    public List<Aircraft> getAircraftInOrder() {
        return aircraft;
    }

    /**
     * Returns true if the given aircraft is in the queue.
     *
     * @param aircraft aircraft to find in queue
     * @return true if aircraft is in queue; false otherwise
     */
    @Override
    public boolean containsAircraft(Aircraft aircraft) {
        return this.aircraft.contains(aircraft);
    }
}
