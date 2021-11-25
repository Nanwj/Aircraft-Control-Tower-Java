package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
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

import java.io.*;

import java.util.*;

/**
 * Utility class that contains static methods for
 * loading a control tower and associated entities from files.
 */
public class ControlTowerInitialiser {

    /* Try to read a clean string with given reader */
    private static String readCleanLine(BufferedReader reader)
            throws MalformedSaveException, IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new MalformedSaveException();
        }
        return line;
    }

    /* Reads a integer from its encoded representation in the given string. */
    private static int readIntegers(String line) throws MalformedSaveException {
        try {
            int number = Integer.parseInt(line);
            if (number < 0) {
                // all of integer less than 0 read from file is invalid
                throw new MalformedSaveException();
            }
            return number;
        } catch (NumberFormatException exception) {
            // the string cannot convert to an integer
            throw new MalformedSaveException();
        }
    }

    /* Reads a two decimals double from its encoded representation in the given string. */
    private static double readDouble(String line) throws MalformedSaveException {
        if (!line.contains(".")) {
            // this is not a double (without dot)
            throw new MalformedSaveException();
        }
        String[] integerAndDecimal = line.split("\\.");
        if (integerAndDecimal[1].length() != 2) {
            // this is not a two decimals double
            throw new MalformedSaveException();
        }
        try {
            double number = Double.parseDouble(line);
            if (number < 0) {
                // double less than 0 is invalid.
                throw new MalformedSaveException();
            }
            return number;
        } catch (NumberFormatException e) {
            // the string is not a presentation of double
            throw new MalformedSaveException();
        }
    }

    /* Check whether the number of string separated by ":" of "@" is correct,
    in other word, check whether the number of symbol is correct. */
    private static void checkLengthOfArray(String[] strings, int numberOfStrings)
            throws MalformedSaveException {
        if (strings.length != numberOfStrings) {
            // the symbol is less than expected
            throw new MalformedSaveException();
        }
    }

    /**
     * Loads the number of ticks elapsed from the given reader instance.
     *
     * @param reader reader from which to load the number of ticks elapsed
     * @return number of ticks elapsed
     * @throws MalformedSaveException if the format of the text read from the reader
     *                                is invalid according to the rules above
     * @throws IOException if an IOException is encountered when reading from the reader
     */
    public static long loadTick(Reader reader) throws MalformedSaveException, IOException {
        BufferedReader readLoadTick = new BufferedReader(reader);
        // the tick number transferred from string string to long
        try {
            long tickNumber = Long.parseLong(readCleanLine(readLoadTick));
            if (tickNumber >= 0) {
                readLoadTick.close();
                return tickNumber;
            } else {
                // the tick is less than zero
                throw new MalformedSaveException();
            }
        } catch (NumberFormatException exception) {
            // tick read from reader is not a long
            throw new MalformedSaveException();
        }
    }

    /**
     * Loads the list of all aircraft managed by the control tower
     * from the given reader instance.
     *
     * @param reader reader from which to load the list of aircraft
     * @return list of aircraft read from the reader
     * @throws IOException if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     */
    public static List<Aircraft> loadAircraft(Reader reader)
            throws IOException, MalformedSaveException {
        BufferedReader readAircraft = new BufferedReader(reader);
        List<Aircraft> aircraft = new ArrayList<>();
        int numberOfAircraft = readIntegers(readCleanLine(readAircraft));
        for (int indexOfAircraft = 0; indexOfAircraft < numberOfAircraft; indexOfAircraft++) {
            // read aircraft with line read from reader
            // if the aircraft is invalid readAircraft method would throw exception
            aircraft.add(readAircraft(readCleanLine(readAircraft)));
        }
        if (checkRedundantInformation(readAircraft)) {
            // The number of aircraft specified on the first line is less than
            // the number of aircraft actually read from the reader.
            throw new MalformedSaveException();
        }
        readAircraft.close();
        return aircraft;
    }

    /* In order to check whether there is any extra information, try to read more line.
    return true is there is extra information, false otherwise. */
    private static boolean checkRedundantInformation(BufferedReader reader) {
        try {
            // try to read more string.
            readCleanLine(reader);
            return true;
        } catch (MalformedSaveException | IOException e) {
            // there is no extra string read from reader
            return false;
        }
    }

    /**
     * Loads the takeoff queue, landing queue and map of
     * loading aircraft from the given reader instance.
     *
     * @param reader reader from which to load the queues and loading map
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @param takeoffQueue empty takeoff queue that aircraft will be added to
     * @param landingQueue empty landing queue that aircraft will be added to
     * @param loadingAircraft empty map that aircraft and loading times will be added to
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     * @throws IOException if an IOException is encountered when reading from the reader
     */
    public static void loadQueues(Reader reader,
                                  List<Aircraft> aircraft,
                                  TakeoffQueue takeoffQueue,
                                  LandingQueue landingQueue,
                                  Map<Aircraft, Integer> loadingAircraft)
            throws MalformedSaveException, IOException {
        BufferedReader queueReader = new BufferedReader(reader);
        readQueue(queueReader, aircraft, takeoffQueue);
        readQueue(queueReader, aircraft, landingQueue);
        readLoadingAircraft(queueReader, aircraft, loadingAircraft);
        queueReader.close();
    }

    /**
     * Loads the list of terminals and their gates from the given reader instance.
     *
     * @param reader reader from which to load the list of terminals and their gates
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return list of terminals (with their gates) read from the reader
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     * @throws IOException if an IOException is encountered when reading from the reader
     */
    public static List<Terminal> loadTerminalsWithGates(Reader reader, List<Aircraft> aircraft)
            throws MalformedSaveException, IOException {
        BufferedReader readTerminal = new BufferedReader(reader);
        List<Terminal> terminals = new ArrayList<>();
        int numberOfTerminals = readIntegers(readCleanLine(readTerminal));
        for (int indexOfTerminal = 0; indexOfTerminal < numberOfTerminals; indexOfTerminal++) {
            // read terminal with line read from reader
            // if the terminal is invalid readTerminal method would throw exception
            terminals.add(readTerminal(readCleanLine(readTerminal), readTerminal, aircraft));
        }
        if (checkRedundantInformation(readTerminal)) {
            // The number of terminal specified on the first line
            // is less than the number of terminal actually read from the reader.
            throw new MalformedSaveException();
        }
        readTerminal.close();
        return terminals;
    }

    /**
     * Creates a control tower instance by reading
     * various airport entities from the given readers.
     *
     * @param tick reader from which to load the number of ticks elapsed
     * @param aircraft reader from which to load the list of aircraft
     * @param queues reader from which to load the aircraft queues and map of loading aircraft
     * @param terminalsWithGates reader from which to load the terminals and their gates
     * @return control tower created by reading from the given readers
     * @throws MalformedSaveException if reading from any of the given readers
     * results in a MalformedSaveException, indicating the contents of that reader are invalid
     * @throws IOException if an IOException is encountered when reading from any of the readers
     */
    public static ControlTower createControlTower(Reader tick,
                                                  Reader aircraft,
                                                  Reader queues,
                                                  Reader terminalsWithGates)
            throws MalformedSaveException, IOException {
        // list of aircraft controlled by control tower
        List<Aircraft> aircraftInControlTower = loadAircraft(new BufferedReader(aircraft));
        // takeoff queue in control tower
        TakeoffQueue takeoffQueue = new TakeoffQueue();
        // landing queue in control tower
        LandingQueue landingQueue = new LandingQueue();
        // map of loading aircraft in control tower
        Map<Aircraft, Integer> loadingAircraft = new
                TreeMap<>(Comparator.comparing(Aircraft::getCallsign));
        loadQueues(new BufferedReader(queues), aircraftInControlTower, takeoffQueue,
                landingQueue, loadingAircraft);

        // initializer control tower
        ControlTower controlTower = new ControlTower(loadTick(new BufferedReader(tick)),
                aircraftInControlTower, landingQueue, takeoffQueue, loadingAircraft);

        // list of terminals controlled by control tower
        List<Terminal> terminals = loadTerminalsWithGates(new
                BufferedReader(terminalsWithGates), aircraftInControlTower);
        for (Terminal terminal : terminals) {
            controlTower.addTerminal(terminal);
        }
        return controlTower;
    }

    /**
     * Reads an aircraft from its encoded representation in the given string.
     *
     * @param line public static Aircraft readAircraft(String line)
     *             throws MalformedSaveException
     * @return decoded aircraft instance
     * @throws MalformedSaveException if the format of the given string is
     *                                invalid according to the rules above
     */
    public static Aircraft readAircraft(String line) throws MalformedSaveException {
        Aircraft aircraft;
        // aircraft consist of a callsign, followed by a characteristic,
        // task list, fuel amount, emergency state and cargo amount
        String[] aircraftInformation = line.split(":", 6);
        checkLengthOfArray(aircraftInformation, 6);
        try {
            // create an aircraft with aircraft information read from reader
            aircraft = createAircraft(aircraftInformation[0],
                    getCharacteristic(aircraftInformation[1]),
                    readTaskList(aircraftInformation[2]),
                    readDouble(aircraftInformation[3]),
                    readIntegers(aircraftInformation[5]));
        } catch (IllegalArgumentException exception) {
            // the characteristic of aircraft or the order of task list is invalid.
            throw new MalformedSaveException();
        }
        if (Boolean.parseBoolean(aircraftInformation[4])) {
            // declare emergency if the encode of emergency state is true
            aircraft.declareEmergency();
        }
        return aircraft;
    }

    /* Return an FreightAircraft or PassengerAircraft with given information of aircraft */
    private static Aircraft createAircraft(String callsign,
                                                 AircraftCharacteristics characteristic,
                                                 TaskList tasks,
                                                 double fuelAmount,
                                                 int cargoAmount) {
        if (characteristic.passengerCapacity == 0) {
            // create a freight aircraft
            return new FreightAircraft(callsign, characteristic,
                    tasks, fuelAmount, cargoAmount);
        } else {
            // create a passenger aircraft
            return new PassengerAircraft(callsign, characteristic,
                    tasks, fuelAmount, cargoAmount);
        }
    }

    /* Reads a AircraftCharacteristic from its encoded representation in the given string. */
    private static AircraftCharacteristics getCharacteristic(String line)
            throws MalformedSaveException {
        try {
            return AircraftCharacteristics.valueOf(line);
        } catch (IllegalArgumentException exception) {
            // the characteristic is invalid
            throw new MalformedSaveException();
        }
    }

    /**
     * Reads a task list from its encoded representation in the given string.
     *
     * @param taskListPart string containing the encoded task list
     * @return decoded task list instance
     * @throws MalformedSaveException if the format of the given string is
     *                                invalid according to the rules above
     */
    public static TaskList readTaskList(String taskListPart)
            throws MalformedSaveException {
        List<Task> tasks = new ArrayList<>();
        String[] allTask = taskListPart.split(",");
        try {
            for (String task : allTask) {
                // readTask method would throw exception
                // if the encode of task is invalid
                tasks.add(readTask(task));
            }
            return new TaskList(tasks);
        } catch (IllegalArgumentException exception) {
            // the order of task list is invalid
            throw new MalformedSaveException();
        }
    }

    /* Reads a task from its encoded representation in the given string. */
    private static Task readTask(String line) throws MalformedSaveException {
        String[] taskInformation = line.split("@", 2);
        // task information consists of task type or task type and load percent
        if (taskInformation.length == 1) {
            // this is a task without loading percent
            return new Task(getTaskType(line));
        } else if (taskInformation.length == 2) {
            // Load task consists of task type and load percent
            int taskPercent = readIntegers(taskInformation[1]);
            return new Task(getTaskType(taskInformation[0]), taskPercent);
        } else {
            // the task encode is invalid
            throw new MalformedSaveException();
        }
    }

    /* Reads a TaskType from its encoded representation in the given string. */
    private static TaskType getTaskType(String line) throws MalformedSaveException {
        try {
            return TaskType.valueOf(line);
        } catch (IllegalArgumentException exception) {
            // task type is invalid
            throw new MalformedSaveException();
        }
    }

    /**
     * Reads an aircraft queue from the given reader instance.
     *
     * @param reader reader from which to load the aircraft queue
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @param queue empty queue that aircraft will be added to
     * @throws IOException if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     */
    public static void readQueue(BufferedReader reader,
                                  List<Aircraft> aircraft, AircraftQueue queue)
            throws IOException, MalformedSaveException {
        String[] queueInformation = readCleanLine(reader).split(":", 2);
        // queue information consists of queue type and the number of aircraft in the queue
        checkLengthOfArray(queueInformation, 2);
        if (!queue.getClass().getSimpleName().equals(queueInformation[0])) {
            // The queue type specified in the first line is not equal to
            // the simple class name of the queue provided as a parameter.
            throw new MalformedSaveException();
        }
        int numOfAircraft = readIntegers(queueInformation[1]);
        if (numOfAircraft > 0) {
            String[] aircraftCallsigns = readCleanLine(reader).split(",");
            // The number of callsigns listed on the second line is not equal
            // to the number of aircraft specified on the first line.
            checkLengthOfArray(aircraftCallsigns, numOfAircraft);
            for (int indexOfAircraft = 0; indexOfAircraft < numOfAircraft; indexOfAircraft++) {
                queue.addAircraft(findAircraftInList(aircraft,
                        aircraftCallsigns[indexOfAircraft]));
            }
        }
    }

    /* Try to find aircraft in a list with given acllsign. */
    private static Aircraft findAircraftInList(List<Aircraft> allAircraft, String callsign)
            throws MalformedSaveException {
        for (Aircraft aircraft : allAircraft) {
            if (aircraft.getCallsign().equals(callsign)) {
                return aircraft;
            }
        }
        throw new MalformedSaveException();
    }

    /**
     * Reads the map of currently loading aircraft from the given reader instance.
     *
     * @param reader reader from which to load the map of loading aircraft
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @param loadingAircraft empty map that aircraft and their loading times will be added to
     * @throws IOException if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     */
    public static void readLoadingAircraft(BufferedReader reader, List<Aircraft> aircraft,
                                           Map<Aircraft, Integer> loadingAircraft)
            throws IOException, MalformedSaveException {
        String[] loadingAircraftInfo = readCleanLine(reader).split(":", 2);
        // loading aircraft map consists of introduction of map type
        // and the number of aircraft in the map
        checkLengthOfArray(loadingAircraftInfo, 2);
        int callsignNum = readIntegers(loadingAircraftInfo[1]);
        if (callsignNum > 0) {
            String[] mapInformation = readCleanLine(reader).split(",");
            // The number of aircraft specified on the first line is not equal
            // to the number of callsigns read on the second line.
            checkLengthOfArray(mapInformation, callsignNum);
            for (String string : mapInformation) {
                putInformationIntoMap(loadingAircraft, string, aircraft);
            }
        }
    }

    /* Reads the callsign and loading time pair from
    the given reader string and push them into given map */
    private static void putInformationIntoMap(Map<Aircraft, Integer> loadingAircraft,
                                              String line, List<Aircraft> aircraftUnderCheck)
            throws MalformedSaveException {
        String[] entrySetInformation = line.split(":", 2);
        // loadingAircraft consists callsign and loading time of an aircraft
        checkLengthOfArray(entrySetInformation, 2);
        int ticksRemaining = readIntegers(entrySetInformation[1]);
        if (ticksRemaining < 1) {
            throw new MalformedSaveException();
        }
        loadingAircraft.put(findAircraftInList(aircraftUnderCheck,
                entrySetInformation[0]), ticksRemaining);
    }

    /**
     * Reads a terminal from the given string and reads
     * its gates from the given reader instance.
     *
     * @param line string containing the first line of the encoded terminal
     * @param reader reader from which to load the gates of the terminal (subsequent lines)
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return decoded terminal with its gates added
     * @throws IOException if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the given string
     * or the text read from the reader is invalid according to the rules above
     */
    public static Terminal readTerminal(String line, BufferedReader reader,
                                        List<Aircraft> aircraft)
            throws IOException, MalformedSaveException {
        Terminal terminal;
        String[] terminalInformation = line.split(":", 4);
        // terminal consists of terminal type, terminal number, emergency state
        // and number of gate controlled by terminal
        checkLengthOfArray(terminalInformation, 4);
        terminal = separateTerminalType(readIntegers(terminalInformation[1]),
                terminalInformation[0], terminalInformation[2]);
        int numberOfGate = readIntegers(terminalInformation[3]);
        if (numberOfGate > Terminal.MAX_NUM_GATES) {
            // The number of gates is less than zero or is greater than Terminal.MAX_NUM_GATES
            throw new MalformedSaveException();
        }
        addGateIntoTerminal(reader, aircraft, numberOfGate, terminal);
        return terminal;
    }

    /* Reads a gate from its encoded representation
    in the given reader and add it into given terminal */
    private static void addGateIntoTerminal(BufferedReader reader,
                                            List<Aircraft> aircraft,
                                            int numberOfGate,
                                            Terminal terminal)
            throws IOException, MalformedSaveException {
        try {
            for (int indexOfGate = 0; indexOfGate < numberOfGate; indexOfGate++) {
                terminal.addGate(readGate(readCleanLine(reader), aircraft));
            }
        } catch (NoSpaceException exception) {
            // do nothing
        }
    }

    /* try to create a terminal with correct type */
    private static Terminal separateTerminalType(int terminalNumber,
                                                 String terminalType,
                                                 String emergency)
            throws MalformedSaveException {
        Terminal terminal;
        if (terminalNumber < 1) {
            // the terminal number is less than one
            throw new MalformedSaveException();
        }
        if (terminalType.equals("AirplaneTerminal")) {
            terminal = new AirplaneTerminal(terminalNumber);
        } else if (terminalType.equals("HelicopterTerminal")) {
            terminal = new HelicopterTerminal(terminalNumber);
        } else {
            // The terminal type specified on the first line is neither
            // AirplaneTerminal nor HelicopterTerminal.
            throw new MalformedSaveException();
        }
        if (Boolean.parseBoolean(emergency)) {
            // declare emergency if the encode of emergency state is true
            terminal.declareEmergency();
        }
        return terminal;
    }

    /**
     * Reads a gate from its encoded representation in the given string.
     *
     * @param line string containing the encoded gate
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return decoded gate instance
     * @throws MalformedSaveException if the format of the given string is
     *                                invalid according to the rules above
     */
    public static Gate readGate(String line, List<Aircraft> aircraft)
            throws MalformedSaveException {
        Gate gate;
        String[] gateInformation = line.split(":", 2);
        // Gate is consist of gate number and callsign of aircraft
        // parked inside or "empty" represents empty gate
        checkLengthOfArray(gateInformation, 2);
        int gateNumber = readIntegers(gateInformation[0]);
        if (gateNumber < 1) {
            // gate number is less than one
            throw new MalformedSaveException();
        }
        gate = new Gate(gateNumber);
        if (!gateInformation[1].equals("empty")) {
            // callsign of the aircraft parked at the gate is not "empty"
            try {
                gate.parkAircraft(findAircraftInList(aircraft, gateInformation[1]));
            } catch (NoSpaceException exception) {
                // do nothing
            }
        }
        return gate;
    }
}
