package towersim.control;

import org.junit.Before;
import org.junit.Test;
import towersim.aircraft.*;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class LandingQueueTest {
    private Aircraft aircraft1; // normal
    private Aircraft aircraft2; // less fuel
    private Aircraft aircraft3; // emergency
    private Aircraft aircraft4; // passenger

    private LandingQueue landingQueue;

    @Before
    public void setup() {
        TaskList taskList1 = new TaskList(List.of(
                new Task(TaskType.LAND),
                new Task(TaskType.LOAD, 0), // load no freight
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY)));

        this.aircraft1 = new FreightAircraft("ABC001",
                AircraftCharacteristics.BOEING_747_8F, taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity,
                AircraftCharacteristics.BOEING_747_8F.freightCapacity);

        this.aircraft2 = new FreightAircraft("ABC002",
                AircraftCharacteristics.BOEING_747_8F, taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0.1,
                0);

        this.aircraft3 = new FreightAircraft("ABC003",
                AircraftCharacteristics.BOEING_747_8F, taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity, 0);

        this.aircraft4 = new PassengerAircraft("ABC004",
                AircraftCharacteristics.AIRBUS_A320, taskList1,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity, 0);

        landingQueue = new LandingQueue();
    }

    @Test
    public void addAircraftTest() {
        assertEquals(landingQueue.getAircraftInOrder(), List.of());
        landingQueue.addAircraft(aircraft1);
        List<Aircraft> expected = new ArrayList<>();
        expected.add(aircraft1);
        assertEquals("addAircraft is not correct", expected,
                landingQueue.getAircraftInOrder());
    }

    @Test
    public void removeAircraftTest() {
        landingQueue.addAircraft(aircraft2);

        assertEquals("removeAircraft didn't return correct aircraft", aircraft2,
                landingQueue.removeAircraft());
        assertEquals("removeAircraft didn't remove aircraft from list", List.of(),
                landingQueue.getAircraftInOrder());
    }

    @Test
    public void removeAircraftNullTest() {
        assertNull("wrong implement when queue is empty",
                landingQueue.removeAircraft());
    }

    @Test
    public void containsAircraftTest() {
        assertFalse("there is no aircraft inside the landing queue",
                landingQueue.containsAircraft(aircraft1));

        landingQueue.addAircraft(aircraft2);

        assertTrue("containsAircraft is not correct",
                landingQueue.containsAircraft(aircraft2));
    }

    @Test
    public void peekAircraftNullTest() {
        assertNull("peekAircraft is not correct", landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftTest() {
        landingQueue.addAircraft(aircraft1);
        assertEquals("peekAircraft is not correct", aircraft1,
                landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftLessFuelTest() {
        landingQueue.addAircraft(aircraft1);
        landingQueue.addAircraft(aircraft4);
        landingQueue.addAircraft(aircraft2);
        assertEquals("peekAircraft is not correct", aircraft2,
                landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftEmergencyTest() {
        landingQueue.addAircraft(aircraft1);
        landingQueue.addAircraft(aircraft2);
        landingQueue.addAircraft(aircraft4);
        aircraft3.declareEmergency();
        landingQueue.addAircraft(aircraft3);
        assertEquals("peekAircraft is not correct", aircraft3,
                landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftPassengerTest() {
        landingQueue.addAircraft(aircraft1);
        landingQueue.addAircraft(aircraft4);
        assertEquals("peekAircraft is not correct", aircraft4,
                landingQueue.peekAircraft());
    }

    @Test
    public void getAircraftInOrderTest() {
        aircraft3.declareEmergency();
        List<Aircraft> expected = new ArrayList<>();
        expected.add(aircraft3); // emergency
        expected.add(aircraft2); // less fuel
        expected.add(aircraft4); // passenger aircraft
        expected.add(aircraft1); // normal freight aircraft
        landingQueue.addAircraft(aircraft1);
        landingQueue.addAircraft(aircraft2);
        landingQueue.addAircraft(aircraft3);
        landingQueue.addAircraft(aircraft4);
        assertEquals("getAircraftInOrder is not correct", expected,
                landingQueue.getAircraftInOrder());
    }
}