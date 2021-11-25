package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftType;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.TaskType;
import towersim.util.NoSpaceException;
import towersim.util.NoSuitableGateException;
import towersim.util.Tickable;

import java.util.*;

/**
 * Represents a the control tower of an airport.
 * <p>
 * The control tower is responsible for managing the operations of the airport, including arrivals
 * and departures in/out of the airport, as well as aircraft that need to be loaded with cargo
 * at gates in terminals.
 * @ass1
 */
public class ControlTower implements Tickable {
    /** List of all aircraft managed by the control tower. */
    private final List<Aircraft> aircraft;

    /** List of all terminals in the airport. */
    private final List<Terminal> terminals;

    /** number of ticks that have elapsed since the tower was first created */
    private long ticksElapsed;

    /** queue of aircraft waiting to land */
    private LandingQueue landingQueue;

    /** queue of aircraft waiting to take off */
    private TakeoffQueue takeoffQueue;

    /** mapping of aircraft that are loading cargo to the number
     * of ticks remaining for loading */
    private Map<Aircraft, Integer> loadingAircraft;

    /**
     * Creates a new ControlTower.
     *
     * @param ticksElapsed number of ticks that have elapsed
     *                     since the tower was first created
     * @param aircraft list of aircraft managed by the control tower
     * @param landingQueue queue of aircraft waiting to land
     * @param takeoffQueue queue of aircraft waiting to take off
     * @param loadingAircraft mapping of aircraft that are loading cargo to
     *                        the number of ticks remaining for loading
     */
    public ControlTower(long ticksElapsed,
                        List<Aircraft> aircraft,
                        LandingQueue landingQueue,
                        TakeoffQueue takeoffQueue,
                        Map<Aircraft, Integer> loadingAircraft) {
        this.ticksElapsed = ticksElapsed;
        this.aircraft = aircraft;
        this.landingQueue = landingQueue;
        this.takeoffQueue = takeoffQueue;
        this.loadingAircraft = loadingAircraft;
        this.terminals = new ArrayList<>();
    }

    /**
     * Adds the given terminal to the jurisdiction of this control tower.
     *
     * @param terminal terminal to add
     * @ass1
     */
    public void addTerminal(Terminal terminal) {
        this.terminals.add(terminal);
    }

    /**
     * Returns a list of all terminals currently managed by this control tower.
     * <p>
     * The order in which terminals appear in this list should be the same as the order in which
     * they were added by calling {@link #addTerminal(Terminal)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all terminals
     * @ass1
     */
    public List<Terminal> getTerminals() {
        return new ArrayList<>(this.terminals);
    }

    /**
     * Adds the given aircraft to the jurisdiction of this control tower.
     * <p>
     * If the aircraft's current task type is {@code WAIT} or {@code LOAD}, it should be parked at a
     * suitable gate as found by the {@link #findUnoccupiedGate(Aircraft)} method.
     * If there is no suitable gate for the aircraft, the {@code NoSuitableGateException} thrown by
     * {@code findUnoccupiedGate()} should be propagated out of this method.
     *
     * @param aircraft aircraft to add
     * @throws NoSuitableGateException if there is no suitable gate for an aircraft with a current
     *                                 task type of {@code WAIT} or {@code LOAD}
     * @ass1
     */
    public void addAircraft(Aircraft aircraft) throws NoSuitableGateException {
        // the current task type of given aircraft
        TaskType currentTaskType = aircraft.getTaskList().getCurrentTask().getType();
        if (currentTaskType == TaskType.WAIT || currentTaskType == TaskType.LOAD) {
            try {
                findUnoccupiedGate(aircraft).parkAircraft(aircraft);
            } catch (NoSpaceException ignored) {
                // no suitable gate for the aircraft
                throw new NoSuitableGateException();
            }
        }
        this.aircraft.add(aircraft);
        placeAircraftInQueues(aircraft);
    }

    /**
     * Returns a list of all aircraft currently managed by this control tower.
     * <p>
     * The order in which aircraft appear in this list should be the same as the order in which
     * they were added by calling {@link #addAircraft(Aircraft)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all aircraft
     * @ass1
     */
    public List<Aircraft> getAircraft() {
        return new ArrayList<>(this.aircraft);
    }

    /**
     * Finds the gate where the given aircraft is parked, and returns null if the aircraft is
     * not parked at any gate in any terminal.
     *
     * @param aircraft aircraft whose gate to find
     * @return gate occupied by the given aircraft; or null if none exists
     * @ass1
     */
    public Gate findGateOfAircraft(Aircraft aircraft) {
        for (Terminal terminal : this.terminals) {
            for (Gate gate : terminal.getGates()) {
                if (Objects.equals(gate.getAircraftAtGate(), aircraft)) {
                    return gate;
                }
            }
        }
        return null;
    }

    /**
     * public long getTicksElapsed()
     *
     * @return number of ticks elapsed
     */
    public long getTicksElapsed() {
        return ticksElapsed;
    }

    /**
     * Returns the queue of aircraft waiting to land.
     *
     * @return landing queue
     */
    public AircraftQueue getLandingQueue() {
        return landingQueue;
    }

    /**
     * Returns the queue of aircraft waiting to take off.
     *
     * @return takeoff queue
     */
    public AircraftQueue getTakeoffQueue() {
        return takeoffQueue;
    }

    /**
     * Returns the mapping of loading aircraft to their remaining load times.
     *
     * @return loading aircraft map
     */
    public Map<Aircraft, Integer> getLoadingAircraft() {
        return loadingAircraft;
    }

    /**
     * Attempts to find an unoccupied gate in a compatible terminal for the given aircraft.
     * <p>
     * Only terminals of the same type as the aircraft's AircraftType (see
     * {@link towersim.aircraft.AircraftCharacteristics#type}) should be considered. For example,
     * for an aircraft with an AircraftType of {@code AIRPLANE}, only AirplaneTerminals may be
     * considered.
     * <p>
     * For each compatible terminal, the {@link Terminal#findUnoccupiedGate()} method should be
     * called to attempt to find an unoccupied gate in that terminal. If
     * {@code findUnoccupiedGate()} does not find a suitable gate, the next compatible terminal
     * in the order they were added should be checked instead, and so on.
     * <p>
     * If no unoccupied gates could be found across all compatible terminals, a
     * {@code NoSuitableGateException} should be thrown.
     *
     * @param aircraft aircraft for which to find gate
     * @return gate for given aircraft if one exists
     * @throws NoSuitableGateException if no suitable gate could be found
     * @ass1
     */
    public Gate findUnoccupiedGate(Aircraft aircraft) throws NoSuitableGateException {
        AircraftType aircraftType = aircraft.getCharacteristics().type;
        for (Terminal terminal : terminals) {
            /*
             * Only check for available gates at terminals that are of the same aircraft type as
             * the aircraft
             */
            if ((terminal instanceof AirplaneTerminal && aircraftType == AircraftType.AIRPLANE)
                    || (terminal instanceof HelicopterTerminal
                    && aircraftType == AircraftType.HELICOPTER)) {
                try {
                    // This terminal found a gate, return it
                    if (!terminal.hasEmergency()) {
                        // return the game in the terminal which is
                        // not currently in a state of emergency
                        return terminal.findUnoccupiedGate();
                    }
                } catch (NoSuitableGateException e) {
                    // If this terminal has no unoccupied gates, try the next one
                }
            }
        }
        throw new NoSuitableGateException("No gate available for aircraft");
    }

    /**
     * Attempts to land one aircraft waiting in the landing queue
     * and park it at a suitable gate.
     *
     * @return true if an aircraft was successfully landed and parked; false otherwise
     */
    public boolean tryLandAircraft() {
        // the aircraft in the front of the landing queue
        Aircraft firstAircraft = landingQueue.peekAircraft();
        if (firstAircraft != null) {
            try {
                // try to find a suitable gate parking the aircraft
                findUnoccupiedGate(firstAircraft).parkAircraft(firstAircraft);
                // remove the aircraft after successfully park the aircraft into the gate.
                landingQueue.removeAircraft();
                firstAircraft.unload();
                firstAircraft.getTaskList().moveToNextTask();
                return true;
            } catch (NoSuitableGateException | NoSpaceException exception) {
                // there is no suitable gate to part the aircraft in landing queue
                return false;
            }
        }
        // the landing queue is empty
        return false;
    }

    /**
     * Attempts to allow one aircraft waiting in the takeoff queue to take off.
     */
    public void tryTakeOffAircraft() {
        if (takeoffQueue.peekAircraft() != null) {
            // remove the aircraft from the takeoff queue and move task of that aircraft
            takeoffQueue.removeAircraft().getTaskList().moveToNextTask();
        }
    }

    /**
     * Updates the time remaining to load on all currently loading aircraft
     * and removes aircraft from their gate once finished loading.
     */
    public void loadAircraft() {
        for (Aircraft aircraft : loadingAircraft.keySet()) {
            loadingAircraft.put(aircraft, loadingAircraft.get(aircraft) - 1);
        }
        // remove the aircraft with zero loading tick from the map
        removeZeroTicks(loadingAircraft, this);
    }

    /* remove all of the key-values entry set in given map which value is equal to 0,
    and leave the aircraft from correspond gate and finally move task. */
    private static void removeZeroTicks(Map<Aircraft, Integer> loadingAircraft,
                                        ControlTower controlTower) {
        // a iterator of loadingAircraft
        Iterator<Map.Entry<Aircraft, Integer>> iterator = loadingAircraft.entrySet().iterator();
        while (iterator.hasNext()) {
            // the entry of loadingAircraft (ie. aircraft:tickNumber)
            Map.Entry<Aircraft, Integer> loadingAircraftWithTick = iterator.next();
            if (loadingAircraftWithTick.getValue() == 0) {

                Aircraft aircraftWithZeroTick = loadingAircraftWithTick.getKey();

                controlTower.findGateOfAircraft(aircraftWithZeroTick).aircraftLeaves();
                aircraftWithZeroTick.getTaskList().moveToNextTask();
                iterator.remove();
            }
        }
    }

    /**
     * Calls placeAircraftInQueues(Aircraft) on all aircraft
     * managed by the control tower.
     */
    public void placeAllAircraftInQueues() {
        for (Aircraft aircraft : this.aircraft) {
            placeAircraftInQueues(aircraft);
        }
    }

    /**
     * Moves the given aircraft to the appropriate queue based on its current task.
     *
     * @param aircraft aircraft to move to appropriate queue
     */
    public void placeAircraftInQueues(Aircraft aircraft) {
        switch (aircraft.getTaskList().getCurrentTask().getType()) {
            case LAND:
                if (!landingQueue.containsAircraft(aircraft)) {
                    this.getLandingQueue().addAircraft(aircraft);
                }
                break;
            case TAKEOFF:
                if (!takeoffQueue.containsAircraft(aircraft)) {
                    this.getTakeoffQueue().addAircraft(aircraft);
                }
                break;
            case LOAD:
                if (!loadingAircraft.containsKey(aircraft)) {
                    this.getLoadingAircraft().put(aircraft, aircraft.getLoadingTime());
                }
                break;
        }
    }

    /**
     * Advances the simulation by one tick.
     * <p>
     * On each tick, the control tower should call {@link Aircraft#tick()} on all aircraft managed
     * by the control tower.
     * <p>
     * Note that the actions performed by {@code tick()} are very simple at the moment and will be
     * expanded on in assignment 2.
     * @ass1
     */
    @Override
    public void tick() {
        // Call tick() on all other sub-entities
        tickAircraft(aircraft);
        // Move all aircraft with a current task type
        // of AWAY or WAIT to their next task.
        moveTask(aircraft);
        // Process loading aircraft
        loadAircraft();
        if (this.getTicksElapsed() % 2 == 0) {
            // do something in every even tick:
            if (!tryLandAircraft()) {
                // if an aircraft cannot be landed try to takeoff one aircraft
                tryTakeOffAircraft();
            }
        } else {
            // try to takeoff an aircraft on every odd tick
            tryTakeOffAircraft();
        }
        // Place all aircraft in their appropriate queues
        placeAllAircraftInQueues();
        ticksElapsed++;
    }

    /* call Aircraft.tick() on all Aircraft */
    private static void tickAircraft(List<Aircraft> aircraftTicking) {
        for (Aircraft aircraft : aircraftTicking) {
            aircraft.tick();
        }
    }

    /* Move all aircraft with a current task type of AWAY or WAIT to their next task. */
    private static void moveTask(List<Aircraft> aircraftUnderCheck) {
        TaskType currentTaskType;
        for (Aircraft aircraft : aircraftUnderCheck) {
            currentTaskType = aircraft.getTaskList().getCurrentTask().getType();
            if (currentTaskType == TaskType.AWAY || currentTaskType == TaskType.WAIT) {
                aircraft.getTaskList().moveToNextTask();
            }
        }
    }

    /**
     * Returns the human-readable string representation of this control tower.
     *
     * @return string representation of this control tower
     */
    public String toString() {
        // human-readable string of the information about the
        // queues and loading aircraft map controlled by control tower
        StringJoiner queueInformation = new StringJoiner(", ", "(", ")");
        queueInformation.add(this.getLandingQueue().getAircraftInOrder().size() + " LAND");
        queueInformation.add(this.getTakeoffQueue().getAircraftInOrder().size() + " TAKEOFF");
        queueInformation.add(this.getLoadingAircraft().keySet().size() + " LOAD");

        // human-readable string of control tower
        StringJoiner controlTower = new StringJoiner(", ");
        controlTower.add("ControlTower: " + this.getTerminals().size() + " terminals");
        controlTower.add(this.getAircraft().size() + " total aircraft "
                + queueInformation.toString());
        return controlTower.toString();
    }
}