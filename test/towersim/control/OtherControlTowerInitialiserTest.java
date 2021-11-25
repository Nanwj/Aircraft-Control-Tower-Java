package towersim.control;

import org.junit.Before;
import org.junit.Test;
import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.PassengerAircraft;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import towersim.util.MalformedSaveException;
import towersim.util.NoSpaceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static org.junit.Assert.*;

public class OtherControlTowerInitialiserTest {
    private Aircraft passengerAircraft1;
    private Aircraft passengerAircraft2;
    private Aircraft passengerAircraft3;
    private Aircraft passengerAircraft4;
    private Aircraft passengerAircraft5;
    private Aircraft passengerAircraft6;

    @Before
    public void setup() {
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

        this.passengerAircraft1 = new PassengerAircraft("QFA481",
                AircraftCharacteristics.AIRBUS_A320,
                taskList1, 10000.00, 132);

        this.passengerAircraft2 = new PassengerAircraft("UTD302",
                AircraftCharacteristics.BOEING_787,
                taskList1, 10000.00, 0);

        this.passengerAircraft3 = new PassengerAircraft("UPS119",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1, 4000.00, 0);

        this.passengerAircraft4 = new PassengerAircraft("VH-BFK",
                AircraftCharacteristics.ROBINSON_R44,
                taskList1, 40.00, 4);

        this.passengerAircraft5 = new PassengerAircraft("NY-ABC",
                AircraftCharacteristics.BOEING_787,
                taskList2, 40.00, 4);

        this.passengerAircraft6 = new PassengerAircraft("YN-XYZ",
                AircraftCharacteristics.ROBINSON_R44,
                taskList2, 40.00, 4);
    }

    @Test
    public void readGate_BasicTest1() {
        String fileContents = "5:empty";
        try {
            Gate gate = ControlTowerInitialiser.readGate(fileContents, List.of(
                    passengerAircraft1,
                    passengerAircraft2,
                    passengerAircraft3,
                    passengerAircraft4
            ));
            Gate expected = new Gate(5);
            assertEquals("readGate is not correct", expected, gate);
            assertNull("readGate is not correct", expected.getAircraftAtGate());
        } catch (MalformedSaveException exception) {
            fail("readGate should not throw a MalformedSaveException");
        }
    }

    @Test
    public void readGate_BasicTest2() {
        String fileContents = "12:QFA481";
        try {
            Gate gate = ControlTowerInitialiser.readGate(fileContents, List.of(
                    passengerAircraft1,
                    passengerAircraft2,
                    passengerAircraft3,
                    passengerAircraft4
            ));
            Gate expected = new Gate(12);
            expected.parkAircraft(passengerAircraft1);
            assertEquals("readGate is not correct", expected, gate);
            assertEquals("readGate didn't part the aircraft into the gate",
                    passengerAircraft1, expected.getAircraftAtGate());
        } catch (MalformedSaveException exception) {
            fail("readGate should not throw a MalformedSaveException");
        } catch (NoSpaceException ignored) {
            // this would never happened
        }
    }

    @Test
    public void readGate_MoreColonTest() {
        String fileContents = "12:QFA:481";
        try {
            Gate gate = ControlTowerInitialiser.readGate(fileContents, List.of(
                    passengerAircraft1,
                    passengerAircraft2,
                    passengerAircraft3,
                    passengerAircraft4
            ));
            fail("readGate should throw a MalformedSaveException since" +
                    "the number of colons (:) detected was more than expected.");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readGate_lessColonTest() {
        String fileContents = "12";
        try {
            Gate gate = ControlTowerInitialiser.readGate(fileContents, List.of(
                    passengerAircraft1,
                    passengerAircraft2,
                    passengerAircraft3,
                    passengerAircraft4
            ));
            fail("readGate should throw a MalformedSaveException since" +
                    "the number of colons (:) detected was less than expected.");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readGate_IntegerTest() {
        String fileContents = "12.4:empty";
        try {
            Gate gate = ControlTowerInitialiser.readGate(fileContents, List.of(
                    passengerAircraft1,
                    passengerAircraft2,
                    passengerAircraft3,
                    passengerAircraft4
            ));
            fail("readGate should throw a MalformedSaveException since" +
                    "the gate number is not an integer.");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readGate_GateNumberLessThanOneTest() {
        String fileContents = "-8:empty";
        try {
            Gate gate = ControlTowerInitialiser.readGate(fileContents, List.of(
                    passengerAircraft1,
                    passengerAircraft2,
                    passengerAircraft3,
                    passengerAircraft4
            ));
            fail("readGate should throw a MalformedSaveException since" +
                    "the gate number is less than one (1).");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readGate_CallsignTest() {
        String fileContents = "9:Java";
        try {
            Gate gate = ControlTowerInitialiser.readGate(fileContents, List.of(
                    passengerAircraft1,
                    passengerAircraft2,
                    passengerAircraft3,
                    passengerAircraft4
            ));
            fail("readGate should throw a MalformedSaveException since" +
                    "The callsign of the aircraft parked at the gate is not empty and " +
                    "the callsign does not correspond to the callsign of any aircraft " +
                    "contained in the list of aircraft given as a parameter.");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTerminal_BasicTest() {
        String fileContents = String.join(System.lineSeparator(),
                "1:QFA481",
                "2:empty",
                "3:empty",
                "4:empty",
                "5:empty",
                "6:empty");
        try {
            Terminal terminal = ControlTowerInitialiser.readTerminal("AirplaneTerminal:1:false:6",
                    new BufferedReader(new StringReader(fileContents)), List.of(
                            passengerAircraft4,
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3
                    ));
            Terminal expected = new AirplaneTerminal(1);
            Gate gate1 = new Gate(1);
            gate1.parkAircraft(passengerAircraft1);
            Gate gate2 = new Gate(1);
            Gate gate3 = new Gate(1);
            Gate gate4 = new Gate(1);
            Gate gate5 = new Gate(1);
            Gate gate6 = new Gate(1);
            expected.addGate(gate1);
            expected.addGate(gate2);
            expected.addGate(gate3);
            expected.addGate(gate4);
            expected.addGate(gate5);
            expected.addGate(gate6);
            assertEquals("readTerminal is not correct", expected, terminal);
            assertEquals("readTerminal didn't part the aircraft into the gate",
                    passengerAircraft1, expected.getGates().get(0).getAircraftAtGate());
        } catch (MalformedSaveException | IOException exception) {
            fail("readTerminal should not throw a MalformedSaveException or IOException");
        } catch (NoSpaceException ignored) {}
    }

    @Test
    public void readTerminal_MoreColonTest() {
        String fileContents = String.join(System.lineSeparator(),
                "1:QFA481",
                "2:empty",
                "3:empty",
                "4:empty",
                "5:empty",
                "6:emp:ty");
        try {
            ControlTowerInitialiser.readTerminal("AirplaneTerminal:1:false:6",
                    new BufferedReader(new StringReader(fileContents)), List.of(
                            passengerAircraft4,
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3
                    ));
            fail("readTerminal should throw a MalformedSaveException since" +
                    "the number of colons (:) detected on the first line is more than expected.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readTerminal_LessColonTest() {
        String fileContents = String.join(System.lineSeparator(),
                "1:QFA481",
                "2:empty",
                "3:empty",
                "4:empty",
                "5empty",
                "6:empty");
        try {
            ControlTowerInitialiser.readTerminal("AirplaneTerminal:1:false:6",
                    new BufferedReader(new StringReader(fileContents)), List.of(
                            passengerAircraft4,
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3
                    ));
            fail("readTerminal should throw a MalformedSaveException since" +
                    "the number of colons (:) detected on the first line is less than expected.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readTerminal_TerminalTypeTest() {
        String fileContents = String.join(System.lineSeparator(),
                "1:QFA481",
                "2:empty",
                "3:empty",
                "4:empty",
                "5:empty",
                "6:empty");
        try {
            ControlTowerInitialiser.readTerminal("BOEING_787:1:false:6",
                    new BufferedReader(new StringReader(fileContents)), List.of(
                            passengerAircraft4,
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3
                    ));
            fail("readTerminal should throw a MalformedSaveException since" +
                    "the terminal type specified on the first line is neither " +
                    "AirplaneTerminal nor HelicopterTerminal.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readTerminal_IntegerTerminalNumberTest() {
        String fileContents = String.join(System.lineSeparator(),
                "1:QFA481",
                "2:empty",
                "3:empty",
                "4:empty",
                "5:empty",
                "6:empty");
        try {
            ControlTowerInitialiser.readTerminal("AirplaneTerminal:5.5:false:6",
                    new BufferedReader(new StringReader(fileContents)), List.of(
                            passengerAircraft4,
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3
                    ));
            fail("readTerminal should throw a MalformedSaveException since" +
                    "the terminal number is not an integer.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readTerminal_TerminalNumberLessOneTest() {
        String fileContents = String.join(System.lineSeparator(),
                "1:QFA481",
                "2:empty",
                "3:empty",
                "4:empty",
                "5:empty",
                "6:empty");
        try {
            ControlTowerInitialiser.readTerminal("AirplaneTerminal:0:false:6",
                    new BufferedReader(new StringReader(fileContents)), List.of(
                            passengerAircraft4,
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3
                    ));
            fail("readTerminal should throw a MalformedSaveException since" +
                    "The terminal number is less than one (1).");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readTerminal_NumberOfGateIntegerTest() {
        String fileContents = String.join(System.lineSeparator(),
                "1:QFA481",
                "2:empty",
                "3:empty",
                "4:empty",
                "5:empty",
                "6:empty");
        try {
            ControlTowerInitialiser.readTerminal("AirplaneTerminal:0.5:false:6",
                    new BufferedReader(new StringReader(fileContents)), List.of(
                            passengerAircraft4,
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3
                    ));
            fail("readTerminal should throw a MalformedSaveException since" +
                    "The number of gates in the terminal is not an integer.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readTerminal_NumberOfGateLessZeroTest() {
        String fileContents = String.join(System.lineSeparator(),
                "1:QFA481",
                "2:empty",
                "3:empty",
                "4:empty",
                "5:empty",
                "6:empty");
        try {
            ControlTowerInitialiser.readTerminal("AirplaneTerminal:1:false:-4",
                    new BufferedReader(new StringReader(fileContents)), List.of(
                            passengerAircraft4,
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3
                    ));
            fail("readTerminal should throw a MalformedSaveException since" +
                    "The number of gates is less than zero");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readTerminal_NumberOfGateExceedMaximumTest() {
        String fileContents = String.join(System.lineSeparator(),
                "1:QFA481",
                "2:empty",
                "3:empty",
                "4:empty",
                "5:empty",
                "6:empty");
        try {
            ControlTowerInitialiser.readTerminal("AirplaneTerminal:1:false:100",
                    new BufferedReader(new StringReader(fileContents)), List.of(
                            passengerAircraft4,
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3
                    ));
            fail("readTerminal should throw a MalformedSaveException since" +
                    "The number of gates is greater than Terminal.MAX_NUM_GATES.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readTerminal_EOFTest() {
        String fileContents = String.join(System.lineSeparator(),
                "1:QFA481",
                "2:empty",
                "3:empty",
                "4:empty",
                "6:empty");
        try {
            ControlTowerInitialiser.readTerminal("AirplaneTerminal:1:false:6",
                    new BufferedReader(new StringReader(fileContents)), List.of(
                            passengerAircraft4,
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3
                    ));
            fail("readTerminal should throw a MalformedSaveException since" +
                    "A line containing an encoded gate was expected, " +
                    "but EOF (end of file) was received");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readQueue_LandingQueueBasicTest() {
        String fileContents = String.join(System.lineSeparator(),
                "LandingQueue:1",
                "VH-BFK");
        try {
            LandingQueue landingQueue = new LandingQueue();
            ControlTowerInitialiser.readQueue(new BufferedReader(new StringReader(fileContents)),
                    List.of(passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4), landingQueue);
            LandingQueue expected = new LandingQueue();
            expected.addAircraft(passengerAircraft4);
            // assertTrue(expected.equals(landingQueue));
            assertEquals("readQueue is not correct",
                    expected.peekAircraft(), landingQueue.peekAircraft());
            assertEquals("readQueue is not correct",
                    expected.getAircraftInOrder(), landingQueue.getAircraftInOrder());
        } catch (MalformedSaveException | IOException exception) {
            fail("readQueue should not throw a MalformedSaveException or IOException");
        }
    }

    @Test
    public void readQueue_TakeoffQueueBasicTest() {
        String fileContents = String.join(System.lineSeparator(),
                "TakeoffQueue:1",
                "UTD302");
        try {
            TakeoffQueue takeoffQueue = new TakeoffQueue();
            ControlTowerInitialiser.readQueue(new BufferedReader(new StringReader(fileContents)),
                    List.of(passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4), takeoffQueue);
            TakeoffQueue expected = new TakeoffQueue();
            expected.addAircraft(passengerAircraft2);
            // assertTrue(expected.equals(landingQueue));
            assertEquals("readQueue is not correct",
                    expected.peekAircraft(), takeoffQueue.peekAircraft());
            assertEquals("readQueue is not correct",
                    expected.getAircraftInOrder(), takeoffQueue.getAircraftInOrder());
        } catch (MalformedSaveException | IOException exception) {
            fail("readQueue should not throw a MalformedSaveException or IOException");
        }
    }

    @Test
    public void readQueue_TakeoffQueueBasicTest1() {
        String fileContents = String.join(System.lineSeparator(),
                "TakeoffQueue:0");
        try {
            TakeoffQueue takeoffQueue = new TakeoffQueue();
            ControlTowerInitialiser.readQueue(new BufferedReader(new StringReader(fileContents)),
                    List.of(passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4), takeoffQueue);
            // assertTrue(expected.equals(landingQueue));

        } catch (MalformedSaveException | IOException exception) {
            fail("readQueue should not throw a MalformedSaveException or IOException");
        }
    }

    @Test
    public void readQueue_NullLineTest() {
        String fileContents = String.join(System.lineSeparator(),
                "",
                "TakeoffQueue:1",
                "UTD302");
        try {
            ControlTowerInitialiser.readQueue(new BufferedReader(new StringReader(fileContents)),
                    List.of(passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4), new TakeoffQueue());
            fail("readQueue should not throw a MalformedSaveException since " +
                    "the first line read from the reader is null.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readQueue_MoreColonsTest1() {
        String fileContents = String.join(System.lineSeparator(),
                "Takeoff:Queue:1",
                "UTD302");
        try {
            ControlTowerInitialiser.readQueue(new BufferedReader(new StringReader(fileContents)),
                    List.of(passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4), new TakeoffQueue());
            fail("readQueue should not throw a MalformedSaveException since " +
                    "the first line contains more/fewer colons (:) than expected.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readQueue_MoreColonsTest2() {
        String fileContents = String.join(System.lineSeparator(),
                "Tak:eoffQueue:1",
                "UTD302");
        try {
            ControlTowerInitialiser.readQueue(new BufferedReader(new StringReader(fileContents)),
                    List.of(passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4), new TakeoffQueue());
            fail("readQueue should not throw a MalformedSaveException since " +
                    "the first line contains more/fewer colons (:) than expected.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readQueue_QueueTypeTest() {
        String fileContents = String.join(System.lineSeparator(),
                "TakeawayQueue:1",
                "UTD302");
        try {
            ControlTowerInitialiser.readQueue(new BufferedReader(new StringReader(fileContents)),
                    List.of(passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4), new TakeoffQueue());
            fail("readQueue should not throw a MalformedSaveException since " +
                    "The queue type specified in the first line is not equal to the " +
                    "simple class name of the queue provided as a parameter.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readQueue_IntegerAircraftNumberTest() {
        String fileContents = String.join(System.lineSeparator(),
                "Takeoff:Queue:0.8",
                "UTD302");
        try {
            ControlTowerInitialiser.readQueue(new BufferedReader(new StringReader(fileContents)),
                    List.of(passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4), new TakeoffQueue());
            fail("readQueue should not throw a MalformedSaveException since " +
                    "the number of aircraft specified on the first line is not an integer");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readQueue_NullAircraftTest() {
        String fileContents = String.join(System.lineSeparator(),
                "Takeoff:Queue:2",
                "");
        try {
            ControlTowerInitialiser.readQueue(new BufferedReader(new StringReader(fileContents)),
                    List.of(passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4), new TakeoffQueue());
            fail("readQueue should not throw a MalformedSaveException since " +
                    "the number of aircraft specified is greater than zero and the second line read is null.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readQueue_CallsignTest1() {
        String fileContents = String.join(System.lineSeparator(),
                "Takeoff:Queue:1",
                "UTD302");
        try {
            ControlTowerInitialiser.readQueue(new BufferedReader(new StringReader(fileContents)),
                    List.of(passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4), new TakeoffQueue());
            fail("readQueue should not throw a MalformedSaveException since " +
                    "the number of callsigns listed on the second line is not equal to " +
                    "the number of aircraft specified on the first line.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readQueue_CallsignTest2() {
        String fileContents = String.join(System.lineSeparator(),
                "TakeoffQueue:1",
                "UTD302,QFA481");
        try {
            ControlTowerInitialiser.readQueue(new BufferedReader(new StringReader(fileContents)),
                    List.of(passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4), new TakeoffQueue());
            fail("readQueue should not throw a MalformedSaveException since " +
                    "the number of callsigns listed on the second line is not equal to " +
                    "the number of aircraft specified on the first line.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readQueue_CallsignTest3() {
        String fileContents = String.join(System.lineSeparator(),
                "TakeoffQueue:2",
                "ABC007");
        try {
            ControlTowerInitialiser.readQueue(new BufferedReader(new StringReader(fileContents)),
                    List.of(passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4), new TakeoffQueue());
            fail("readQueue should not throw a MalformedSaveException since " +
                    "A callsign listed on the second line does not correspond to the callsign " +
                    "of any aircraft contained in the list of aircraft given as a parameter");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readLoadingAircraft_BasicTest() {
        String fileContents = String.join(System.lineSeparator(),
                "LoadingAircraft:2",
                        "NY-ABC:2,YN-XYZ:1");
        Map<Aircraft, Integer> loadingAircraft = new
                TreeMap<>(Comparator.comparing(Aircraft::getCallsign));
        Map<Aircraft, Integer> expected = new
                TreeMap<>(Comparator.comparing(Aircraft::getCallsign));
        expected.put(passengerAircraft5, 2);
        expected.put(passengerAircraft6, 1);
        try {
            ControlTowerInitialiser.readLoadingAircraft(new BufferedReader(new StringReader(fileContents)),
                    List.of(
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4,
                            passengerAircraft5,
                            passengerAircraft6
                    ), loadingAircraft);
            assertEquals("readLoadingAircraft is not correct", expected, loadingAircraft);
        } catch (MalformedSaveException | IOException exception) {
            fail("readLoadingAircraft should not throw a MalformedSaveException or IOException");
        }
    }

    @Test
    public void readLoadingAircraft_NullFirstLineTest() {
        String fileContents = String.join(System.lineSeparator(),
                "",
                "LoadingAircraft:2",
                "NY-ABC:2,YN-XYZ:1");
        try {
            ControlTowerInitialiser.readLoadingAircraft(new BufferedReader(new StringReader(fileContents)),
                    List.of(
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4,
                            passengerAircraft5,
                            passengerAircraft6
                    ), new TreeMap<>(Comparator.comparing(Aircraft::getCallsign)));
            fail("readLoadingAircraft should throw a MalformedSaveException or IOException since" +
                    "the first line read from the reader is null.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readLoadingAircraft_MoreColonsTest() {
        String fileContents = String.join(System.lineSeparator(),
                "Loading:Aircraft:2",
                "NY-ABC:2,YN-XYZ:1");
        try {
            ControlTowerInitialiser.readLoadingAircraft(new BufferedReader(new StringReader(fileContents)),
                    List.of(
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4,
                            passengerAircraft5,
                            passengerAircraft6
                    ), new TreeMap<>(Comparator.comparing(Aircraft::getCallsign)));
            fail("readLoadingAircraft should throw a MalformedSaveException or IOException since" +
                    "the number of colons (:) detected on the first line is more than expected.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readLoadingAircraft_LessColonsTest() {
        String fileContents = String.join(System.lineSeparator(),
                "LoadingAircraft2",
                "NY-ABC:2,YN-XYZ:1");
        try {
            ControlTowerInitialiser.readLoadingAircraft(new BufferedReader(new StringReader(fileContents)),
                    List.of(
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4,
                            passengerAircraft5,
                            passengerAircraft6
                    ), new TreeMap<>(Comparator.comparing(Aircraft::getCallsign)));
            fail("readLoadingAircraft should throw a MalformedSaveException or IOException since" +
                    "the number of colons (:) detected on the first line is less than expected.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readLoadingAircraft_IntegerAircraftNumberTest() {
        String fileContents = String.join(System.lineSeparator(),
                "LoadingAircraft:2.2",
                "NY-ABC:2,YN-XYZ:1");
        try {
            ControlTowerInitialiser.readLoadingAircraft(new BufferedReader(new StringReader(fileContents)),
                    List.of(
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4,
                            passengerAircraft5,
                            passengerAircraft6
                    ), new TreeMap<>(Comparator.comparing(Aircraft::getCallsign)));
            fail("readLoadingAircraft should throw a MalformedSaveException or IOException since" +
                    "the number of aircraft specified on the first line is not an integer");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readLoadingAircraft_NullEntrySetTest() {
        String fileContents = String.join(System.lineSeparator(),
                "LoadingAircraft:2",
                "");
        try {
            ControlTowerInitialiser.readLoadingAircraft(new BufferedReader(new StringReader(fileContents)),
                    List.of(
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4,
                            passengerAircraft5,
                            passengerAircraft6
                    ), new TreeMap<>(Comparator.comparing(Aircraft::getCallsign)));
            fail("readLoadingAircraft should throw a MalformedSaveException or IOException since" +
                    "the number of aircraft is greater than zero and the second line read from the reader is null.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readLoadingAircraft_AircraftNumberMatchTest() {
        String fileContents = String.join(System.lineSeparator(),
                "LoadingAircraft:1",
                "NY-ABC:2,YN-XYZ:1");
        try {
            ControlTowerInitialiser.readLoadingAircraft(new BufferedReader(new StringReader(fileContents)),
                    List.of(
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4,
                            passengerAircraft5,
                            passengerAircraft6
                    ), new TreeMap<>(Comparator.comparing(Aircraft::getCallsign)));
            fail("readLoadingAircraft should throw a MalformedSaveException or IOException since" +
                    "the number of aircraft specified on the first line is not equal to the " +
                    "number of callsigns read on the second line.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readLoadingAircraft_EntrySetColonTest() {
        String fileContents = String.join(System.lineSeparator(),
                "LoadingAircraft:2",
                "NY-ABC:2:5,YN-XYZ:1");
        try {
            ControlTowerInitialiser.readLoadingAircraft(new BufferedReader(new StringReader(fileContents)),
                    List.of(
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4,
                            passengerAircraft5,
                            passengerAircraft6
                    ), new TreeMap<>(Comparator.comparing(Aircraft::getCallsign)));
            fail("readLoadingAircraft should throw a MalformedSaveException or IOException since" +
                    "For any callsign/loading time pair on the second line, the number of " +
                    "colons detected is not equal to one");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readLoadingAircraft_CallsignTest() {
        String fileContents = String.join(System.lineSeparator(),
                "LoadingAircraft:2",
                "NYN-BC:2,YN-XYZ:1");
        try {
            ControlTowerInitialiser.readLoadingAircraft(new BufferedReader(new StringReader(fileContents)),
                    List.of(
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4,
                            passengerAircraft5,
                            passengerAircraft6
                    ), new TreeMap<>(Comparator.comparing(Aircraft::getCallsign)));
            fail("readLoadingAircraft should throw a MalformedSaveException or IOException since" +
                    "a callsign listed on the second line does not correspond to the callsign " +
                    "of any aircraft contained in the list of aircraft given as a parameter.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readLoadingAircraft_TickTest1() {
        String fileContents = String.join(System.lineSeparator(),
                "LoadingAircraft:2",
                "NY-ABC:2.5,YN-XYZ:1");
        try {
            ControlTowerInitialiser.readLoadingAircraft(new BufferedReader(new StringReader(fileContents)),
                    List.of(
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4,
                            passengerAircraft5,
                            passengerAircraft6
                    ), new TreeMap<>(Comparator.comparing(Aircraft::getCallsign)));
            fail("readLoadingAircraft should throw a MalformedSaveException or IOException since" +
                    "any ticksRemaining value on the second line is not an integer");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readLoadingAircraft_TickTest2() {
        String fileContents = String.join(System.lineSeparator(),
                "LoadingAircraft:2",
                "NY-ABC:2,YN-XYZ:0");
        try {
            ControlTowerInitialiser.readLoadingAircraft(new BufferedReader(new StringReader(fileContents)),
                    List.of(
                            passengerAircraft1,
                            passengerAircraft2,
                            passengerAircraft3,
                            passengerAircraft4,
                            passengerAircraft5,
                            passengerAircraft6
                    ), new TreeMap<>(Comparator.comparing(Aircraft::getCallsign)));
            fail("readLoadingAircraft should throw a MalformedSaveException or IOException since" +
                    "Any ticksRemaining value on the second line is less than one (1).");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void loadTick_BasicTest() {
        String fileContents = "5";
        try {
            long tick = ControlTowerInitialiser.loadTick(new StringReader(fileContents));
            long expected = 5;
            assertEquals("loadTick is not correct", tick, expected);
        } catch (MalformedSaveException | IOException exception) {
            fail("loadTick should not throw a MalformedSaveException or IOException");
        }
    }

    @Test
    public void loadTick_IntegerTest() {
        String fileContents = "5.27";
        try {
            ControlTowerInitialiser.loadTick(new StringReader(fileContents));
            fail("loadTick should throw a MalformedSaveException or IOException since " +
                    "The number of ticks elapsed is not an integer.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void loadTick_LessZeroTest() {
        String fileContents = "-3";
        try {
            ControlTowerInitialiser.loadTick(new StringReader(fileContents));
            fail("loadTick should throw a MalformedSaveException or IOException since " +
                    "The number of ticks elapsed is less than zero.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void loadQueues_BasicTest() {
        String fileContents = String.join(System.lineSeparator(),
                "TakeoffQueue:1",
                "QFA481",
                "LandingQueue:1",
                "VH-BFK",
                "LoadingAircraft:2",
                "UTD302:2,UPS119:3");
        TakeoffQueue takeoffQueue = new TakeoffQueue();
        LandingQueue landingQueue = new LandingQueue();
        Map<Aircraft, Integer> loadingAircraft = new TreeMap<>(Comparator.comparing(Aircraft::getCallsign));

        TakeoffQueue expectedTakeoffQueue = new TakeoffQueue();
        expectedTakeoffQueue.addAircraft(passengerAircraft1);
        LandingQueue expectedLandingQueue = new LandingQueue();
        expectedLandingQueue.addAircraft(passengerAircraft4);
        Map<Aircraft, Integer> expectedLoadingAircraft = new TreeMap<>(Comparator.comparing(Aircraft::getCallsign));
        expectedLoadingAircraft.put(passengerAircraft2, 2);
        expectedLoadingAircraft.put(passengerAircraft3, 3);
        try {
            ControlTowerInitialiser.loadQueues(new StringReader(fileContents), List.of(
                    passengerAircraft1,
                    passengerAircraft2,
                    passengerAircraft3,
                    passengerAircraft4
            ), takeoffQueue, landingQueue, loadingAircraft);
            assertEquals("loadQueue is not correct for takeoffQueue",
                    expectedTakeoffQueue.getAircraftInOrder(), takeoffQueue.getAircraftInOrder());
            assertEquals("loadQueue is not correct for landingQueue",
                    expectedLandingQueue.getAircraftInOrder(), landingQueue.getAircraftInOrder());
            assertEquals("loadQueue is not correct for loadingAircraft",
                    expectedLoadingAircraft, loadingAircraft);
        } catch (MalformedSaveException | IOException exception) {
            fail("loadQueue should not throw MalformedSaveException or IOException");
        }
    }

    @Test
    public void loadTerminalsWithGates_BasicTest() throws NoSpaceException {
        String fileContents = String.join(System.lineSeparator(),
                "5",
                "AirplaneTerminal:1:false:6",
                "1:UTD302", // passengerAircraft2
                "2:empty",
                "3:empty",
                "4:empty",
                "5:empty",
                "6:empty",
                "HelicopterTerminal:2:false:5",
                "7:empty",
                "8:empty",
                "9:empty",
                "10:empty",
                "11:empty",
                "AirplaneTerminal:3:false:2",
                "12:empty",
                "13:UPS119",  // passengerAircraft3
                "HelicopterTerminal:4:true:0",
                "HelicopterTerminal:5:false:0");
        List<Terminal> expected = new ArrayList<>();
        Terminal terminal1 = new AirplaneTerminal(1);
        Terminal terminal2 = new HelicopterTerminal(2);
        Terminal terminal3 = new AirplaneTerminal(3);
        Terminal terminal4 = new HelicopterTerminal(4);
        Terminal terminal5 = new HelicopterTerminal(5);
        Gate gate1 = new Gate(1);
        Gate gate2 = new Gate(2);
        Gate gate3 = new Gate(3);
        Gate gate4 = new Gate(4);
        Gate gate5 = new Gate(5);
        Gate gate6 = new Gate(6);
        Gate gate7 = new Gate(7);
        Gate gate8 = new Gate(8);
        Gate gate9 = new Gate(9);
        Gate gate10 = new Gate(10);
        Gate gate11 = new Gate(11);
        Gate gate12 = new Gate(12);
        Gate gate13 = new Gate(13);
        gate1.parkAircraft(passengerAircraft2);
        gate13.parkAircraft(passengerAircraft3);
        terminal1.addGate(gate1);
        terminal1.addGate(gate2);
        terminal1.addGate(gate3);
        terminal1.addGate(gate4);
        terminal1.addGate(gate5);
        terminal1.addGate(gate6);
        terminal2.addGate(gate7);
        terminal2.addGate(gate8);
        terminal2.addGate(gate9);
        terminal2.addGate(gate10);
        terminal2.addGate(gate11);
        terminal3.addGate(gate12);
        terminal3.addGate(gate13);
        terminal4.declareEmergency();
        expected.add(terminal1);
        expected.add(terminal2);
        expected.add(terminal3);
        expected.add(terminal4);
        expected.add(terminal5);
        try {
            List<Terminal> terminals = ControlTowerInitialiser.loadTerminalsWithGates(new
                    StringReader(fileContents), List.of(
                    passengerAircraft1,
                    passengerAircraft2,
                    passengerAircraft3,
                    passengerAircraft4));
            for (int i = 0; i < 5; i++) {
                assertEquals("terminals are not correct after run loadTerminalsWithGates",
                        expected.get(i), terminals.get(i));
                assertEquals("gates in terminal are not correct after run loadTerminalsWithGates",
                        expected.get(i).getGates(), terminals.get(i).getGates());
            }
            assertEquals("the aircraft parking in gate in terminal is not correct",
                    expected.get(0).getGates().get(0).getAircraftAtGate(),
                    terminals.get(0).getGates().get(0).getAircraftAtGate());
            assertEquals("the aircraft parking in gate in terminal is not correct",
                    expected.get(2).getGates().get(1).getAircraftAtGate(),
                    terminals.get(2).getGates().get(1).getAircraftAtGate());
        } catch (MalformedSaveException | IOException exception) {
            fail("loadTerminalsWithGates should not throw a MalformedSaveException or IOException");
        }
    }
}
