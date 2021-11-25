package towersim.control;

import org.junit.Before;
import org.junit.Test;
import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class TakeoffQueueTest {
    private Aircraft aircraft1;
    private Aircraft aircraft2;
    private Aircraft aircraft3;

    private TakeoffQueue takeoffQueue;

    @Before
    public void setup() {
        TaskList taskList = new TaskList(List.of(
                new Task(TaskType.LAND),
                new Task(TaskType.LOAD, 0), // load no freight
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY)));

        this.aircraft1 = new FreightAircraft("ABC001",
                AircraftCharacteristics.BOEING_747_8F, taskList, 0, 0);

        this.aircraft2 = new FreightAircraft("ABC002",
                AircraftCharacteristics.BOEING_747_8F, taskList, 0, 0);

        this.aircraft3 = new PassengerAircraft("ABC004",
                AircraftCharacteristics.AIRBUS_A320, taskList, 0, 0);

        takeoffQueue = new TakeoffQueue();
    }

    @Test
    public void addAircraftTest() {
        assertEquals(List.of(), takeoffQueue.getAircraftInOrder());
        takeoffQueue.addAircraft(aircraft1);
        List<Aircraft> expected = new ArrayList<>();
        expected.add(aircraft1);
        assertEquals(expected, takeoffQueue.getAircraftInOrder());
    }

    @Test
    public void peekAircraftTest() {
        takeoffQueue.addAircraft(aircraft2);
        assertEquals("peekAircraft is not correct", aircraft2,
                takeoffQueue.peekAircraft());
    }

    @Test
    public void peekAircraftNullTest() {
        assertNull("peekAircraft is not correct", takeoffQueue.peekAircraft());
    }

    @Test
    public void removeAircraftNullTest() {
        assertNull("removeAircraft is not correct", takeoffQueue.removeAircraft());
    }

    @Test
    public void removeAircraftTest() {
        takeoffQueue.addAircraft(aircraft3);
        assertEquals("removeAircraft is not correct", aircraft3,
                takeoffQueue.removeAircraft());
        assertEquals("removeAircraft didn't remove aircraft in the list", List.of(),
                takeoffQueue.getAircraftInOrder());
    }

    @Test
    public void getAircraftInOrderTest() {
        List<Aircraft> expected = new ArrayList<>();
        expected.add(aircraft1);
        expected.add(aircraft2);
        expected.add(aircraft3);
        takeoffQueue.addAircraft(aircraft1);
        takeoffQueue.addAircraft(aircraft2);
        takeoffQueue.addAircraft(aircraft3);
        assertEquals("getAircraftInOrder is not correct", expected,
                takeoffQueue.getAircraftInOrder());
    }

    @Test
    public void containsAircraftTest() {
        assertFalse("containsAircraft is not correct",
                takeoffQueue.containsAircraft(aircraft1));
        takeoffQueue.addAircraft(aircraft2);
        assertTrue("containsAircraft is not correct",
                takeoffQueue.containsAircraft(aircraft2));
    }
}
