package towersim.control;

import org.junit.Before;
import org.junit.Test;
import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.PassengerAircraft;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import towersim.util.NoSpaceException;
import towersim.util.NoSuitableGateException;

import java.util.*;

import static org.junit.Assert.*;

public class OtherControlTowerTest {
    private ControlTower controlTower;
    private Aircraft passengerAircraft1;
    private Aircraft passengerAircraft2;
    private Aircraft passengerAircraft3;
    private Aircraft passengerAircraft4;
    private Aircraft aircraftLanding;
    private Aircraft aircraftLoading;
    private Aircraft aircraftTakeoff;
    private Aircraft aircraftAway;

    private AirplaneTerminal airplaneTerminal1;
    private AirplaneTerminal airplaneTerminal2;
    private HelicopterTerminal helicopterTerminal1;

    private Gate gate1;
    private Gate gate2;
    private Gate gate3;

    private TakeoffQueue takeoffQueue;
    private LandingQueue landingQueue;


    @Before
    public void setup() {
        airplaneTerminal1 = new AirplaneTerminal(1);
        airplaneTerminal2 = new AirplaneTerminal(2);
        helicopterTerminal1 = new HelicopterTerminal(1);

        gate1 = new Gate(1);
        gate2 = new Gate(2);
        gate3 = new Gate(3);


        takeoffQueue = new TakeoffQueue();
        landingQueue = new LandingQueue();
        Map<Aircraft, Integer> loadingAircraft = new TreeMap<>(Comparator.comparing(Aircraft::getCallsign));

        controlTower = new ControlTower(0, new ArrayList<>(), landingQueue,
                takeoffQueue, loadingAircraft);

        TaskList taskList1 = new TaskList(List.of(
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 60),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY)));

        TaskList taskList2 = new TaskList(List.of(
                new Task(TaskType.LOAD, 60),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT)));

        TaskList taskList3 = new TaskList(List.of(
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 60)));

        TaskList taskList4 = new TaskList(List.of(
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 60),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY)));
        this.aircraftAway = new PassengerAircraft("VH-BFK",
                AircraftCharacteristics.AIRBUS_A320,
                taskList1, 40.00, 4);
        this.aircraftLoading = new PassengerAircraft("VH-BFK",
                AircraftCharacteristics.AIRBUS_A320,
                taskList2, 40.00, 4);
        this.aircraftTakeoff = new PassengerAircraft("VH-BFK",
                AircraftCharacteristics.AIRBUS_A320,
                taskList3, 40.00, 4);
        this.aircraftLanding = new PassengerAircraft("VH-BFK",
                AircraftCharacteristics.AIRBUS_A320,
                taskList4, 40.00, 100);

        this.passengerAircraft1 = new PassengerAircraft("QFA481",
                AircraftCharacteristics.AIRBUS_A320,
                taskList1, 10000.00, 132);

        this.passengerAircraft2 = new PassengerAircraft("UTD302",
                AircraftCharacteristics.AIRBUS_A320,
                taskList2, 10000.00, 0);

        this.passengerAircraft3 = new PassengerAircraft("UPS119",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1, 4000.00, 0);

        this.passengerAircraft4 = new PassengerAircraft("VH-BFK",
                AircraftCharacteristics.ROBINSON_R44,
                taskList1, 40.00, 4);
    }

    @Test
    public void addAircraft_BasicTest() {
        try {
            assertEquals("initialize is not correct", List.of(), controlTower.getAircraft());
            assertEquals("initialize is not correct", List.of(), controlTower.getTerminals());


            airplaneTerminal1.addGate(gate2);
            airplaneTerminal1.addGate(gate1);
            airplaneTerminal1.addGate(gate3);
            controlTower.addTerminal(airplaneTerminal1);
            controlTower.addAircraft(aircraftLoading);

            assertEquals("addAircraft is not correct, the aircraft didn't add into aircraft list",
                    List.of(aircraftLoading), controlTower.getAircraft());
            assertEquals("addAircraft is not correct, the aircraft didn't add into loadAircraft map",
                    Map.of(aircraftLoading, aircraftLoading.getLoadingTime()), controlTower.getLoadingAircraft());
            assertEquals("addAircraft is not correct, the aircraft should not add into landing queue",
                    List.of(), controlTower.getLandingQueue().getAircraftInOrder());
            assertEquals("addAircraft is not correct, the aircraft should not add into takeoff queue",
                    List.of(), controlTower.getTakeoffQueue().getAircraftInOrder());
            assertEquals("addAircraft is not correct, the aircraft didn't park aircraft into correspond gate",
                    gate2, controlTower.findGateOfAircraft(aircraftLoading));
            assertEquals("addAircraft is not correct, the aircraft didn't park aircraft into correspond gate",
                    aircraftLoading, gate2.getAircraftAtGate());
        } catch (NoSpaceException | NoSuitableGateException exception) {
            fail("addAircraft should not throw and exception");
        }
    }

    @Test
    public void addAircraft_TaskTest() {
        try {
            controlTower.addAircraft(aircraftLanding);
            controlTower.addAircraft(aircraftTakeoff);

            assertEquals("addAircraft is not correct, the aircraft didn't add into landing queue correctly",
                    List.of(aircraftLanding), controlTower.getLandingQueue().getAircraftInOrder());
            assertEquals("addAircraft is not correct, the aircraft didn't add into takeoff queue correctly",
                    List.of(aircraftTakeoff), controlTower.getTakeoffQueue().getAircraftInOrder());
        } catch (NoSuitableGateException exception) {
            fail("addAircraft should not throw NoSuitableGateException");
        }

    }

    @Test
    public void addAircraft_ExceptionTest() {
        try {
            helicopterTerminal1.addGate(gate1);
            controlTower.addTerminal(helicopterTerminal1);
            controlTower.addAircraft(aircraftLoading);
            fail("addAircraft should throw NoSuitableGateException exception since no suitable gate for parking aircraft");
        } catch (NoSuitableGateException | NoSpaceException ignored) {}
    }

    @Test
    public void getTicksElapsed_BasicTest() {
        assertEquals("ticks is not correct", 0, controlTower.getTicksElapsed());
        controlTower.tick();
        controlTower.tick();
        assertEquals("ticks is not correct", 2, controlTower.getTicksElapsed());
    }

    @Test
    public void getLandingQueue_BasicTest() {
        assertEquals("getLandingQueue is not correct", List.of(),
                controlTower.getLandingQueue().getAircraftInOrder());
        controlTower.getLandingQueue().addAircraft(aircraftLanding);
        assertEquals("getLandingQueue is not correct", List.of(aircraftLanding),
                controlTower.getLandingQueue().getAircraftInOrder());
    }

    @Test
    public void getTakeoffQueue_BasicTest() {
        assertEquals("getTakeoffQueue is not correct", List.of(),
                controlTower.getTakeoffQueue().getAircraftInOrder());
        controlTower.getTakeoffQueue().addAircraft(aircraftTakeoff);
        assertEquals("getTakeoffQueue is not correct", List.of(aircraftTakeoff),
                controlTower.getTakeoffQueue().getAircraftInOrder());
    }

    @Test
    public void getLoadingAircraft_BasicTest() {
        try {
            assertEquals("initialise is not correct", Map.of(),
                    controlTower.getLoadingAircraft());
            airplaneTerminal1.addGate(gate1);
            controlTower.addTerminal(airplaneTerminal1);
            controlTower.addAircraft(aircraftLoading);
            assertEquals("getLoadingAircraft is not correct",
                    Map.of(aircraftLoading, aircraftLoading.getLoadingTime()),
                    controlTower.getLoadingAircraft());

            controlTower.loadAircraft();
            assertEquals("loadAircraft is not correct",
                    Map.of(aircraftLoading, aircraftLoading.getLoadingTime() - 1),
                    controlTower.getLoadingAircraft());
        } catch (NoSuitableGateException | NoSpaceException exception) {
            fail("getLoadingAircraft should not throw a NoSuitableGateException");
        }
    }

    @Test
    public void findUnoccupiedGate_BasicTest() {
        try {
            airplaneTerminal1.addGate(gate1);
            airplaneTerminal1.addGate(gate2);
//            helicopterTerminal1.addGate(gate3);
            gate1.parkAircraft(passengerAircraft1);

            controlTower.addTerminal(airplaneTerminal1);
//            controlTower.addTerminal(helicopterTerminal1);

//            controlTower.addAircraft(aircraftAway);

            assertEquals("findUnoccupiedGate is not correct", gate2,
                    controlTower.findUnoccupiedGate(aircraftAway));
        } catch (NoSuitableGateException exception) {
            fail("findUnoccupiedGate should not throw a NoSuitableGateException");
        } catch (NoSpaceException exception) {
            // this would never happened
            fail("what have you done ???!!!");
        }
    }

    @Test
    public void findUnoccupiedGate_ExceptionTest() {
        try {
            helicopterTerminal1.addGate(gate3);

            controlTower.addTerminal(helicopterTerminal1);
            controlTower.findUnoccupiedGate(aircraftAway);
            fail("findUnoccupiedGate should throw a NoSuitableGateException since no suitable gate for parking aircraft");
        } catch (NoSuitableGateException ignore) {
        } catch (NoSpaceException exception) {
            // this would never happened
            fail("what have you done ???!!!");
        }
    }

    @Test
    public void tryLandAircraft_BasicTest() {
        try {
            airplaneTerminal1.addGate(gate1);
            controlTower.addTerminal(airplaneTerminal1);
            controlTower.getLandingQueue().addAircraft(aircraftLanding);
            assertTrue("the tryLandAircraft didn't return true",
                    controlTower.tryLandAircraft());
            assertEquals("the aircraft should remove from the landing queue",
                    List.of(), controlTower.getLandingQueue().getAircraftInOrder());
            assertEquals("the aircraft should be parked into suitable gate",
                    aircraftLanding, gate1.getAircraftAtGate());
            assertEquals("the aircraft should be parked into suitable gate",
                    gate1, controlTower.findGateOfAircraft(aircraftLanding));
            assertEquals("the passengers are not unload immediately",
                    AircraftCharacteristics.AIRBUS_A320.emptyWeight + 32, // fuel amount remains
                    controlTower.getTerminals().get(0).getGates().get(0).getAircraftAtGate().getTotalWeight(), 1);
            assertEquals("the task didn't move to the next one", TaskType.WAIT,
                    controlTower.getTerminals().get(0).getGates().get(0).getAircraftAtGate().getTaskList().getCurrentTask().getType());
        } catch (NoSpaceException exception) {
            fail("tryLandAircraft should not throw a NoSpaceException");
        }

    }

    @Test
    public void tryLandAircraft_FalseTest1() {
        assertFalse(controlTower.tryLandAircraft());
    }

    @Test
    public void tryLandAircraft_FalseTest2() {
        controlTower.getLandingQueue().addAircraft(aircraftLanding);
        assertFalse(controlTower.tryLandAircraft());
        assertEquals("the aircraft should remain in the landing queue",
                List.of(aircraftLanding), controlTower.getLandingQueue().getAircraftInOrder());
    }

    @Test
    public void tryTakeOffAircraft_BaiscTest() {
        controlTower.getTakeoffQueue().addAircraft(aircraftTakeoff);
        controlTower.getTakeoffQueue().addAircraft(aircraftLanding);
        controlTower.tryTakeOffAircraft();
        assertEquals("the tryTakeOffAircraft should remove the aircraft",
                List.of(aircraftLanding), controlTower.getTakeoffQueue().getAircraftInOrder());
        assertEquals("the task of aircraft should move to next one",
                TaskType.AWAY, aircraftTakeoff.getTaskList().getCurrentTask().getType());
    }

    @Test
    public void loadAircraft_BasicTest() {
        try {
            airplaneTerminal1.addGate(gate1);
            controlTower.addTerminal(airplaneTerminal1);
            controlTower.addAircraft(aircraftLoading); // aircraftLoading load time is 2
            assertEquals("the loadAircraft initialise is not correct",
                    Map.of(aircraftLoading, aircraftLoading.getLoadingTime()),
                    controlTower.getLoadingAircraft());
            controlTower.tick();
            assertEquals("the loadAircraft initialise is not correct",
                    Map.of(aircraftLoading, aircraftLoading.getLoadingTime() - 1),
                    controlTower.getLoadingAircraft());
            controlTower.tick();
            assertEquals("the loadAircraft initialise is not correct",
                    Map.of(), controlTower.getLoadingAircraft());

        } catch (NoSpaceException | NoSuitableGateException exception) {
            fail("the loadAircraft should not throw any exception");
        }
    }

    @Test
    public void placeAllAircraftInQueues_BasicTest() {
        try {
            airplaneTerminal1.addGate(gate1);
            airplaneTerminal1.addGate(gate2);
            airplaneTerminal1.addGate(gate3);
            controlTower.addTerminal(airplaneTerminal1);
            controlTower.addAircraft(aircraftLoading);
            controlTower.addAircraft(aircraftLanding);
            controlTower.addAircraft(aircraftTakeoff);
            controlTower.placeAllAircraftInQueues();
            assertEquals("the aircraft is not place into landing queue",
                    List.of(aircraftLanding), controlTower.getLandingQueue().getAircraftInOrder());
            assertEquals("the aircraft is not place into takeoff queue",
                    List.of(aircraftTakeoff), controlTower.getTakeoffQueue().getAircraftInOrder());
            assertEquals("the aircraft is not place into loading map",
                    Map.of(aircraftLoading, aircraftLoading.getLoadingTime()),
                    controlTower.getLoadingAircraft());

        } catch (NoSpaceException | NoSuitableGateException exception) {
            fail("the placeAllAircraftInQueues should not throw any exception");
        }
    }

    @Test
    public void controlTower_toStringTest() {
        try {
            airplaneTerminal1.addGate(gate1);
            airplaneTerminal1.addGate(gate2);
            airplaneTerminal1.addGate(gate3);
            helicopterTerminal1.addGate(gate3);
            controlTower.addTerminal(airplaneTerminal1);
            controlTower.addTerminal(airplaneTerminal2);
            controlTower.addTerminal(helicopterTerminal1);
            controlTower.addAircraft(aircraftTakeoff);
            controlTower.addAircraft(aircraftAway);
            controlTower.addAircraft(aircraftLanding);
            controlTower.addAircraft(aircraftLoading);
            controlTower.addAircraft(passengerAircraft1);

            controlTower.placeAllAircraftInQueues();
            String expected2 = "ControlTower: 3 terminals, 5 total aircraft (1 LAND, 1 TAKEOFF, 1 LOAD)";
            assertEquals("toString of control tower is not correct",
                    expected2, controlTower.toString());
        } catch (NoSpaceException | NoSuitableGateException exception) {
            fail("the toString should not throw any exception");
        }
    }

    @Test
    public void Aircraft_EncodeTest() {
        passengerAircraft1.declareEmergency();
        String expected1 = "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY:10000.00:true:132";
        assertEquals("the encode of aircraft is not correct",
                expected1, passengerAircraft1.encode());
    }

    @Test
    public void Queue_EncodeTest() {
        takeoffQueue.addAircraft(passengerAircraft1);
        takeoffQueue.addAircraft(passengerAircraft2);
        takeoffQueue.addAircraft(passengerAircraft3);
        String expected = "TakeoffQueue:3" + System.lineSeparator() + "QFA481,UTD302,UPS119";
        assertEquals("the encode of AircraftQueue is not correct",
                expected, takeoffQueue.encode());
    }

    @Test
    public void Queue_ToStringTest() {
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(passengerAircraft3);
        landingQueue.addAircraft(passengerAircraft4);
        StringJoiner expected = new StringJoiner(", ");
        for (Aircraft aircraft : landingQueue.getAircraftInOrder()) {
            expected.add(aircraft.getCallsign());
        }
        assertEquals("the encode of AircraftQueue is not correct",
                "LandingQueue [" + expected + "]", landingQueue.toString());
    }

    @Test
    public void Terminal_EncodeTest() {
        try {
            airplaneTerminal1.addGate(gate1);
            airplaneTerminal1.addGate(gate2);
            airplaneTerminal1.addGate(gate3);
            helicopterTerminal1.declareEmergency();
            gate2.parkAircraft(passengerAircraft2);

            StringJoiner expected1 = new StringJoiner(System.lineSeparator());
            expected1.add("AirplaneTerminal:1:false:3");
            expected1.add("1:empty");
            expected1.add("2:UTD302");
            expected1.add("3:empty");

            String expected2 = "HelicopterTerminal:1:true:0";

            assertEquals("the encode of Terminal is not correct",
                    expected1.toString(), airplaneTerminal1.encode());
            assertEquals("the encode of Terminal is not correct",
                    expected2, helicopterTerminal1.encode());

        } catch (NoSpaceException exception) {
            fail("the encode should not throw any exception");
        }
    }

}
