package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.PassengerAircraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a rule-based queue of aircraft waiting in the air to land.
 */
public class LandingQueue extends AircraftQueue {

    /** a list of aircraft in queue */
    private List<Aircraft> aircraft;

    /** Constructs a new LandingQueue with an initially empty queue of aircraft */
    public LandingQueue() {
        this.aircraft = new ArrayList<>();
    }

    /**
     * Adds the given aircraft to the queue.
     *
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
        if (aircraft.size() != 0) {
            return aircraft.remove(aircraft.indexOf(peekAircraft()));
        }
        return null;
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
            if (findEmergencyAircraft(aircraft) != null) {
                // try to return the first emergency aircraft in the aircraft
                return findEmergencyAircraft(aircraft);
            } else if (findLessFuelAircraft(aircraft) != null) {
                // if no emergency aircraft, try to return the first aircraft
                // remains less than 20% of fuel in the landing queue
                return findLessFuelAircraft(aircraft);
            } else if (findPassengerAircraft(aircraft) != null) {
                // if neither emergency nor less fuel remains aircraft,
                // try to find the first passenger aircraft in the landing queue
                return findPassengerAircraft(aircraft);
            } else {
                // return the first "normal" aircraft in the landing queue
                return aircraft.get(0);
            }
        }
    }

    /* try to find a passenger aircraft in a list of aircraft
    and return the first one, return null if no aircraft satisfied. */
    private static Aircraft findPassengerAircraft(List<Aircraft> aircraftToCheck) {
        for (Aircraft aircraft : aircraftToCheck) {
            if (aircraft instanceof PassengerAircraft) {
                return aircraft;
            }
        }
        return null;
    }

    /* try to find an aircraft has less than or equal to 20 percent fuel remaining
    and return the first one in the list of aircraft, return null if no aircraft satisfied. */
    private static Aircraft findLessFuelAircraft(List<Aircraft> aircraftToCheck) {
        for (Aircraft aircraft : aircraftToCheck) {
            if (aircraft.getFuelPercentRemaining() < 20) {
                return aircraft;
            }
        }
        return null;
    }

    /* try to find the first aircraft under emergency in the given aircraft list
    return null if no aircraft satisfied. */
    private static Aircraft findEmergencyAircraft(List<Aircraft> aircraftToCheck) {
        for (Aircraft aircraft : aircraftToCheck) {
            if (aircraft.hasEmergency()) {
                return aircraft;
            }
        }
        return null;
    }

    /**
     * Returns a list containing all aircraft in the queue, in order.
     *
     * @return list of all aircraft in queue, in queue order
     */
    @Override
    public List<Aircraft> getAircraftInOrder() {
        // a copy of aircraft list to recover aircraft list at the end of this method
        List<Aircraft> copyOfAircraft = new ArrayList<>(aircraft);
        // the list will be returned
        List<Aircraft> result = new ArrayList<>();

        // check whether all of aircraft are added into result list
        while (result.size() != copyOfAircraft.size()) {
            result.add(this.removeAircraft());
        }

        // recover the aircraft list
        this.aircraft = new ArrayList<>(copyOfAircraft);
        return result;
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
