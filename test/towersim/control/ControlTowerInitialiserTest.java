package towersim.control;

import org.junit.Before;
import org.junit.Test;
import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.PassengerAircraft;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import towersim.util.MalformedSaveException;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class ControlTowerInitialiserTest {
    private Aircraft passengerAircraft1;
    private Aircraft passengerAircraft2;
    private Aircraft passengerAircraft3;
    private Aircraft passengerAircraft4;

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
    }

    @Test
    public void loadAircraft_BasicTest() {
        String fileContents = String.join(System.lineSeparator(),
                "4",
                "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY:10000.00:false:132",
                "UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0",
                "UPS119:BOEING_747_8F:WAIT,LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND:4000.00:false:0",
                "VH-BFK:ROBINSON_R44:LAND,WAIT,LOAD@75,TAKEOFF,AWAY,AWAY:40.00:false:4"
        );
        try {
            List<Aircraft> aircraft = ControlTowerInitialiser.loadAircraft(new
                    StringReader(fileContents));
            List<Aircraft> expected = List.of(
                    passengerAircraft1,
                    passengerAircraft2,
                    passengerAircraft3,
                    passengerAircraft4
            );
            assertEquals("loadAircraft is not correct", expected, aircraft);
        } catch (MalformedSaveException | IOException expected) {
            fail("readAircraft should not throw MalformedSaveException or IOException");
        }
    }

    @Test
    public void loadAircraft_NotIntegerTest() {
        String fileContents = String.join(System.lineSeparator(),
                "5.5",
                "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY:10000.00:false:132",
                "UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0",
                "UPS119:BOEING_747_8F:WAIT,LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND:4000.00:false:0",
                "VH-BFK:ROBINSON_R44:LAND,WAIT,LOAD@75,TAKEOFF,AWAY,AWAY:40.00:false:4"
        );
        try {
            ControlTowerInitialiser.loadAircraft(new StringReader(fileContents));
            fail("loadAircraft should throw a MalformedSaveException since" +
                    "The number of aircraft specified on the first line of the reader " +
                    "is not an integer");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void loadAircraft_IntegerNotMatchTest1() {
        String fileContents = String.join(System.lineSeparator(),
                "3",
                "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY:10000.00:false:132",
                "UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0",
                "UPS119:BOEING_747_8F:WAIT,LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND:4000.00:false:0",
                "VH-BFK:ROBINSON_R44:LAND,WAIT,LOAD@75,TAKEOFF,AWAY,AWAY:40.00:false:4"
        );
        try {
            ControlTowerInitialiser.loadAircraft(new StringReader(fileContents));
            fail("loadAircraft should throw a MalformedSaveException since" +
                    "the number of aircraft specified on the first line is not equal to " +
                    "the number of aircraft actually read from the reader.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void loadAircraft_IntegerNotMatchTest2() {
        String fileContents = String.join(System.lineSeparator(),
                "5",
                "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY:10000.00:false:132",
                "UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0",
                "UPS119:BOEING_747_8F:WAIT,LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND:4000.00:false:0",
                "VH-BFK:ROBINSON_R44:LAND,WAIT,LOAD@75,TAKEOFF,AWAY,AWAY:40.00:false:4"
        );
        try {
            ControlTowerInitialiser.loadAircraft(new StringReader(fileContents));
            fail("loadAircraft should throw a MalformedSaveException since" +
                    "the number of aircraft specified on the first line is not equal to " +
                    "the number of aircraft actually read from the reader.");
        } catch (MalformedSaveException | IOException ignored) {}
    }

    @Test
    public void readTaskList_BasicTest() {
        String fileContents = "AWAY,AWAY,LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY";
        List<Task> expected = List.of(
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 60),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY));
        try {
            TaskList tasks = ControlTowerInitialiser.readTaskList(fileContents);
            for (Task task : expected) {
                assertEquals("the order of task after readTaskList is not correct",
                        task, tasks.getCurrentTask());
                tasks.moveToNextTask();
            }
            assertEquals("readTaskList is not correct",
                    new TaskList(expected).encode(), tasks.encode());
        } catch (MalformedSaveException e) {
            fail("readTaskList should not throw MalformedSaveException");
        }
    }

    @Test
    public void readTaskList_InvalidTaskTypeTest() {
        String fileContents = "AWAY,AWY,LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since the task list's TaskType is not valid (AWY)");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_LoadPercentTest1() {
        String fileContents = "AWAY,AWAY,LAND,WAIT,WAIT,LOAD@0.2,TAKEOFF,AWAY";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since load percent is not an integer (double)");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_LoadPercentTest2() {
        String fileContents = "AWAY,AWAY,LAND,WAIT,WAIT,LOAD@20@@@,TAKEOFF,AWAY";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since load percent is not an integer (double)");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_LoadPercentTest3() {
        String fileContents = "AWAY,AWAY,LAND,WAIT,WAIT,LOAD@cc,TAKEOFF,AWAY";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since load percent is not an integer (double)");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_LoadPercentTest4() {
        String fileContents = "AWAY,AWAY,LAND,WAIT,WAIT,LOAD@-20,TAKEOFF,AWAY";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since load percent is less than zero");
        } catch (MalformedSaveException ignored) {}
    }

    // this test is not necessary, just read from discussion board,
    // tutor would not test for this condition
    @Test
    public void readTaskList_LoadPercentTest5() {
        String fileContents = "AWAY,AWAY,LAND,WAIT@10,WAIT,LOAD@20,TAKEOFF,AWAY";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
        } catch (MalformedSaveException ignored) {
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since load percent is less than zero");
        }
    }

    @Test
    public void readTaskList_LoadPercentTest6() {
        String fileContents = "AWAY,AWAY,LAND,WAIT,WAIT,LOAD@20@@,TAKEOFF,AWAY";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since load percent is less than zero");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_SymbolTest1() {
        String fileContents = "AWAY,AWAY,LAND,WAIT,WAIT,LOAD@20@10,TAKEOFF,AWAY";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since a @ symbol occurred with WAIT task");
        } catch (MalformedSaveException ignored) {}

        fileContents = "AWAY,AWAY,LAND,WAIT,WAIT,LOAD@20@89,TAKEOFF,AWAY";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since more @ symbol occurred with load task");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_SymbolTest2() {
        String fileContents = "AWAY,AWAY,LAND,WAIT,WAIT,LOAD@a,TAKEOFF,AWAY";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since a @ symbol occurred with WAIT task");
        } catch (MalformedSaveException ignored) {}

        fileContents = "AWAY,AWAY,LAND,WAIT,WAIT,LOAD@20@89,TAKEOFF,AWAY";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since more @ symbol occurred with load task");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_AWAYInvalidRulesTest1() {
        String fileContents = "AWAY,TAKEOFF";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since TAKEOFF cannot come after AWAY");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_AWAYInvalidRulesTest2() {
        String fileContents = "AWAY,LOAD@15";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since LOAD cannot come after AWAY");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_AWAYInvalidRulesTest3() {
        String fileContents = "AWAY,WAIT";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since WAIT cannot come after AWAY");
        } catch (MalformedSaveException ignored) {}
    }



    @Test
    public void readTaskList_LANDInvalidRulesTest1() {
        String fileContents = "LAND,TAKEOFF";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since TAKEOFF cannot come after LAND");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_LANDInvalidRulesTest2() {
        String fileContents = "LAND,LAND";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since LAND cannot come after LAND");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_LANDInvalidRulesTest3() {
        String fileContents = "LAND,AWAY";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since AWAY cannot come after LAND");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_WAITInvalidRulesTest1() {
        String fileContents = "WAIT,TAKEOFF";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since TAKEOFF cannot come after WAIT");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_WAITInvalidRulesTest2() {
        String fileContents = "WAIT,LAND";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since LAND cannot come after WAIT");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_WAITInvalidRulesTest3() {
        String fileContents = "WAIT,AWAY";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since AWAY cannot come after WAIT");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_LOADInvalidRulesTest1() {
        String fileContents = "LOAD@15,LOAD@15";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since LOAD cannot come after LOAD");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_LOADInvalidRulesTest2() {
        String fileContents = "LOAD@15,AWAY";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since AWAY cannot come after LOAD");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_LOADInvalidRulesTest3() {
        String fileContents = "LOAD@15,LAND";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since LAND cannot come after LOAD");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_LOADInvalidRulesTest4() {
        String fileContents = "LOAD@15,WAIT";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since WAIT cannot come after LOAD");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_TAKEOFFInvalidRulesTest1() {
        String fileContents = "TAKEOFF,TAKEOFF";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since TAKEOFF cannot come after TAKEOFF");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_TAKEOFFInvalidRulesTest2() {
        String fileContents = "TAKEOFF,LOAD@15";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since LOAD cannot come after TAKEOFF");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_TAKEOFFInvalidRulesTest3() {
        String fileContents = "TAKEOFF,WAIT";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since WAIT cannot come after TAKEOFF");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readTaskList_TAKEOFFInvalidRulesTest4() {
        String fileContents = "TAKEOFF,LAND";
        try {
            ControlTowerInitialiser.readTaskList(fileContents);
            fail("the readTaskList should throw a MalformedSaveException exception" +
                    "since LAND cannot come after TAKEOFF");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readAircraft_BasicTest() {
        String fileContents = "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT," +
                "LOAD@60,TAKEOFF,AWAY:10000.00:false:132";
        try {
            assertEquals("the aircraft after readAircraft is not correct",
                    passengerAircraft1, ControlTowerInitialiser.readAircraft(fileContents));
        } catch (MalformedSaveException e) {
            fail("readAircraft should not throw and MalformedSaveException exception");
        }
    }

    @Test
    public void readAircraft_MoreColonTest() {
        String fileContents = "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT:WAIT," +
                "LOAD@60,TAKEOFF,AWAY:10000.00:false:132";
        try {
            ControlTowerInitialiser.readAircraft(fileContents);
            fail("readAircraft should throw and MalformedSaveException exception" +
                    "since there are more colons expected in file");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readAircraft_EmergencyTest1() {
        String fileContents = "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT," +
                "LOAD@60,TAKEOFF,AWAY:10000.00:fale:132";
        try {
            ControlTowerInitialiser.readAircraft(fileContents).hasEmergency();

        } catch (MalformedSaveException ignored) {
            fail("readAircraft should throw and MalformedSaveException exception" +
                    "since there are more colons expected in file");
        }
    }

    @Test
    public void readAircraft_LessColonTest1() {
        String fileContents = "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT," +
                "LOAD@60,TAKEOFF,AWAY:10000.00:132";
        try {
            ControlTowerInitialiser.readAircraft(fileContents);
            fail("readAircraft should throw and MalformedSaveException exception" +
                    "since there are less colons expected in file");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readAircraft_LessColonTest2() {
        String fileContents = "QFA481AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT," +
                "LOAD@60,TAKEOFF,AWAY:10000.00:false:132";
        try {
            ControlTowerInitialiser.readAircraft(fileContents);
            fail("readAircraft should throw and MalformedSaveException exception" +
                    "since there are less colons expected in file");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readAircraft_InvalidAircraftCharacteristicsTest() {
        String fileContents = "QFA481:AIS_A320:AWAY,AWAY,LAND,WAIT,WAIT," +
                "LOAD@60,TAKEOFF,AWAY:10000.00:false:132";
        try {
            ControlTowerInitialiser.readAircraft(fileContents);
            fail("readAircraft should throw and MalformedSaveException exception" +
                    "since the AircraftCharacteristics is invalid");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readAircraft_FuelAmountTest1() {
        String fileContents = "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT," +
                "LOAD@60,TAKEOFF,AWAY:200.0:false:132";
        try {
            ControlTowerInitialiser.readAircraft(fileContents);
            fail("readAircraft should throw and MalformedSaveException exception" +
                    "since the fuel amount is not a double (integer)");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readAircraft_FuelAmountTest2() {
        String fileContents = "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT," +
                "LOAD@60,TAKEOFF,AWAY:-200:false:132";
        try {
            ControlTowerInitialiser.readAircraft(fileContents);
            fail("readAircraft should throw and MalformedSaveException exception" +
                    "since the fuel amount is less than zero");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readAircraft_FuelAmountTest3() {
        String fileContents = "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT," +
                "LOAD@60,TAKEOFF,AWAY:3000000:false:132";
        try {
            ControlTowerInitialiser.readAircraft(fileContents);
            fail("readAircraft should throw and MalformedSaveException exception" +
                    "since the fuel amount is greater than the aircraft's maximum fuel capacity");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readAircraft_FuelAmountTest4() {
        String fileContents = "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT," +
                "LOAD@60,TAKEOFF,AWAY:abc:false:132";
        try {
            ControlTowerInitialiser.readAircraft(fileContents);
            fail("readAircraft should throw and MalformedSaveException exception" +
                    "since the fuel amount is greater than the aircraft's maximum fuel capacity");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readAircraft_CargoTest1() {
        String fileContents = "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT," +
                "LOAD@60,TAKEOFF,AWAY:10000.00:false:100.00";
        try {
            ControlTowerInitialiser.readAircraft(fileContents);
            fail("readAircraft should throw and MalformedSaveException exception" +
                    "since the cargo amount is not an integer (double)");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readAircraft_CargoTest2() {
        String fileContents = "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT," +
                "LOAD@60,TAKEOFF,AWAY:10000.00:false:-10";
        try {
            ControlTowerInitialiser.readAircraft(fileContents);
            fail("readAircraft should throw and MalformedSaveException exception" +
                    "since the cargo amount is less than zero");
        } catch (MalformedSaveException ignored) {}
    }

    @Test
    public void readAircraft_CargoTest4() {
        String fileContents = "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT," +
                "LOAD@60,TAKEOFF,AWAY:10000.00:false:500";
        try {
            ControlTowerInitialiser.readAircraft(fileContents);
            fail("readAircraft should throw and MalformedSaveException exception" +
                    "since the cargo amount is less than zero");
        } catch (MalformedSaveException | IllegalArgumentException ignored) {}
    }
}
