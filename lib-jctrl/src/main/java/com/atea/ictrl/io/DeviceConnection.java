package com.atea.ictrl.io;

/**
 *
 * @author Martin Arnsrud (martin.arnsrud@gmail.com)
 */
import com.atea.ictrl.io.debugger.ICtrlDebugger;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import org.w3c.dom.Element;

/**
 * <h1>DeviceConnection</h1>
 * DeviceConnection handles I/O-operations to and from different connections,
 * TCP/IP sockets, Serial e.g. I also has built in functions for detecting
 * broken connections and will attempt to repair these. Will also handle logging
 * and debugging messages when using ICtrlDebugger.
 *
 * @author MAARN
 *
 */
public abstract class DeviceConnection {

    // Lowlevel i/o-streams
    /**
     * Raw inputstream
     */
    public InputStream rawRx;

    /**
     * Raw outputstream
     */
    public OutputStream rawTx;
    // High-level i/o-streams

    private final LinkedBlockingQueue<Command> txQueue
            = new LinkedBlockingQueue<Command>(); // Commandqueue
    private long sleepTime; // Time between flushing commands

    /**
     * When latest tx occured
     */
    public long txTime = 0; // W

    /**
     * When latest rx occured
     */
    public long rxTime = 0; // 

    private final byte[] byteBuffer = new byte[60000];

    /**
     *
     */
    protected ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();

    /**
     *
     */
    

    /**
     *
     */
    protected boolean lookingForLineFeed = false;

    private final ArrayList<Integer> rawDebugBytes = new ArrayList<>();

    /**
     *
     */
    protected ArrayList<DeviceConnectionListener> listeners
            = new ArrayList<DeviceConnectionListener>();
    private Timer rawDebugTimer;

    /**
     *
     */
    public String deviceName = "";

    /**
     *
     */
    public boolean runThreads;

    /**
     *
     */
    public boolean connected; // Connection established.
    private boolean connect; // Attempt to connect while true.

    /**
     * 
     */
    public boolean debugMode = false;

    /**
     *
     */
    public boolean logMode = false;

    /**
     *
     */
    public boolean rawMode = false;  // Use raw I/O-streams.

    /**
     *
     */
    public boolean readError; // Read-thread has been terminated

    /**
     *
     */
    public boolean writeError; // Write-thread has been terminated
    private boolean writerRunning = false;

    /**
     *
     */
    public Timer connectionScheduler;
    private final Timer timeoutScheduler;
    private final XmlParseHelper xmlParseHelper = new XmlParseHelper();

    /**
     *
     */
    public DeviceConnection() {
        connectionScheduler = new Timer();
        timeoutScheduler = new Timer();
        this.addDeviceConnectionListener(new DeviceConnectionListener() {

            @Override
            public void parseBytes(byte[] bytes) throws Exception {}

            @Override
            public void parseByte(byte b) throws Exception {
                DeviceConnection.this.parseByte(b);
            }

            @Override
            public void parseLine(String line) throws Exception {
                DeviceConnection.this.parseLine(line);
            }


            @Override
            public void onConnect() throws Exception {
                DeviceConnection.this.initialize();
            }

            @Override
            public void onDisconnect() throws Exception {}
        });
    }

    // WatchDog monitors the connection and atemps to repair it if needed.
    class WatchDog extends TimerTask {

        @Override
        public void run() {
            if (connect != connected) { // Change connection state
                if (connect) // Try to connect to device
                {
                    doConnect();
                } else // Try to disconnect from device
                {
                    doDisconnect();
                }
            } else if (!runThreads && connect) { // Repair conn.
                debugMessage("Attempting to repair broken connection...");
                doDisconnect();
                doConnect();
            }
        }
    }

    // WatchDog monitors the connection and atemps to repair it if needed.

    class RxWatchDog extends TimerTask {

        @Override
        public void run() {
            if (txTime - rxTime > 10000) {
                //todo
            }
        }
    }

    /**
     *
     */
    public class Command { // Defines command structure

        private byte[] command;
        private Integer executionTime;

        /**
         *
         * @param command
         * @param executionTime
         */
        public Command(byte[] command, int executionTime) {
            this.command = command;
            this.executionTime = executionTime;
        }

        /**
         *
         * @return
         */
        public byte[] getCommand() {
            return command;
        }

        /**
         *
         * @return
         */
        public int getExecutionTime() {
            return executionTime;
        }
    }

    /**
     * Pops next command from command queue
     * @return
     */
    public Command popNextCommand() {
        try {
            return (Command) txQueue.take();
        } catch (InterruptedException e) {
            return null;
            //throw new RuntimeException(e);
        }
    }

    private void sendCommand(Command command) {
        txTime = System.currentTimeMillis();
        if(!rawMode) {
            rawMode = !isReadable(command.getCommand());
        }
        try {
            txQueue.put(command);
        } catch (InterruptedException e) {
            // IOexception something has failed during writing to the buffer
        }
    }

    /**
     * Puts a command to the command queue. It will be sent as soon as possible
     * 
     * @param command in byte array format
     * @param executionTime time in milliseconds before next command in queue will be sent.
     */
    public void sendCommand(byte[] command, int executionTime) {
        Command cmd = new Command(command, executionTime);
        sendCommand(cmd);
    }

    /**
     * Puts a command to the command queue. It will be sent as soon as possible
     * @param hexString formatted as hex string e g "0a bc 13 27 0d 0a"
     * @param executionTime time in milliseconds before next command in queue will be sent.
     */
    public void sendHexCommand(String hexString, int executionTime) {
        Command cmd = new Command(HexStringToByteArray(hexString),
                executionTime);
        sendCommand(cmd);
    }

    /**
     * Puts a command to the command queue. It will be sent as soon as possible
     * @param command command in Ascii format.
     * @param executionTime time in milliseconds before next command in queue will be sent.
     */
    public void sendCommand(String command, int executionTime) {
        Command cmd = new Command(command.getBytes(), executionTime);
        sendCommand(cmd);
    }

    /**
     * Will immediately send command to device and override the queue. However, the queue will not be cleared.
     * @param command
     */
    public void sendCommandNow(String command) {
        txTime = System.currentTimeMillis();
        try {
            rawTx.write(command.getBytes());
            rawTx.flush();
            txTime = System.currentTimeMillis();
            debugMessage(deviceName + "> TX: " + command);
        } catch (IOException e) {
            writeError = true;
            debugMessage("I/O write error: " + e);
            stopReadAndWrite();
        } catch (Exception ex) {
        }
    }

    /**
     * Will immediately send command to device and override the queue. However, the queue will not be cleared.
     * @param command
     */
    public void sendCommandNow(byte[] command) {
        txTime = System.currentTimeMillis();
        if(!rawMode) {
            rawMode = !isReadable(command);
        }
        try {
            rawTx.write(command);
            rawTx.flush();
            txTime = System.currentTimeMillis();
            debugMessage("TX: ", command);
        } catch (IOException e) {
            writeError = true;
            debugMessage("I/O write error: " + e);
            stopReadAndWrite();
        } catch (Exception ex) {
        }
    }

    /**
     * Will convert a hexadecimal string representation to a byte array, e g "0a 0b 0c 55 b7"
     * @param hexString
     * @return
     */
    public static byte[] HexStringToByteArray(String hexString) {
        hexString = hexString.replaceAll(" ", "");
        byte data[] = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            data[i / 2] = (Integer.decode("0x" + hexString.charAt(i)
                    + hexString.charAt(i + 1))).byteValue();
        }
        return data;
    }

    /**
     * Converts a byte array to a hexadecimal String representation. 
     * E.g. "0a 1b ff cc"
     * @param bytes
     * @return
     */
    public static String ByteArrayToHexString(byte[] bytes) {
        StringBuilder string = new StringBuilder("");
        for (int i = 0; i < bytes.length; i++) {
            string.append(Integer.toString((bytes[i] & 0xff)
                    + 0x100, 16).substring(1).
                    toUpperCase());
            string.append(" ");
        }
        return string.toString().trim();
    }

    /**
     * Returns true if byte array contains no non-ASCII data
     * @param bytes
     * @return
     */
    public static boolean isReadable(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if ((bytes[i] < 30 || bytes[i] > 175)
                    && bytes[i] != 10 && bytes[i] != 13) {
                return false;
            }
        }

        return true;
    }

    private void doConnect() { // The connect procedure
        try {
            debugMessage("Connecting...");
            connectImpl();
            connected = true;
            if (!writerRunning) {
                initCommandWriter();
            }
            initCommandReader();
            debugMessage("Connected");
            Thread.sleep(3000);
            try {
                onConnect();
                for (DeviceConnectionListener dcl : listeners) {
                    dcl.onConnect();
                }
            } catch (Exception e) {
                debugMessage("Error while running onConnect-event: " + e);
            }
        } catch (Exception e) {
            connected = false;
            debugMessage("Couldn't connect: " + e);
            connectionScheduler.schedule(new WatchDog(), 15000);
        }
    }

    private void doDisconnect() { // The disconnect procedure
        try {
            debugMessage("Disconnecting...");
            disconnectImpl();
            connected = false;
            for (DeviceConnectionListener dcl : listeners) {
                dcl.onDisconnect();
            }
            debugMessage("Disconnected");
        } catch (Exception ex) {
            debugMessage("Exception occured while disconnecting: " + ex);
        }
    }

    
    /**
     * Stop I/O-threads
     */
    public void stopReadAndWrite() {
        setRunThreads(false);
        connected = false;
        try {
            //To unlock the write thread
            sendCommandNow("");
            txQueue.clear();
            onDisconnect();
            for (DeviceConnectionListener dcl : listeners) {
                dcl.onDisconnect();
            }
        } catch (IOException ex) {

        } catch (Exception e) {

        }
        connectionScheduler.schedule(new WatchDog(), 0);
    }

    /**
     * Sets the the state of I/O threads
     * @param runThreads true to let them run
     */
    public void setRunThreads(boolean runThreads) {
        this.runThreads = runThreads;
    }

    /**
     * Message will be passed to ICtrlDebugger with line feed appended.
     * @param message
     */
    public void debugMessage(String message) {
        if (debugMode) {
            ICtrlDebugger.out.println(deviceName + "> " + message);
        }
    }

    /**
     * Message will be passed to ICtrlDebugger with line feed appended.
     * @param prefix pre-message before byte[] message
     * @param message
     */
    public void debugMessage(String prefix, byte[] message) {
        if (debugMode) {
            StringBuilder temp = new StringBuilder();
            if(rawMode) {
                for (int i = 0; i < message.length; i++) {
                    temp.append(Integer.toString((message[i] & 0xff)
                            + 0x100, 16).substring(1).
                            toUpperCase());
                    temp.append(" ");
                }
            } else {
                temp.append(new String(message));
            }
            ICtrlDebugger.out.println(deviceName + "> " + prefix + temp);
        }
    }

    // Gets
    /**
     * Gets the state of debugMode
     * @return
     */
    public boolean getDebugMode() {
        return debugMode;
    }



    /**
     * Returns number of messages waiting to be written to connected device.
     * @return
     */
    public int getTxQueueSize() {
        if (txQueue != null) {
            return txQueue.size();
        } else {
            return 0;
        }
    }

    /**
     * Clears all pending messages waiting the be written to connected device.
     */
    public void clearTxQueue() {
        txQueue.clear();
    }

    /**
     * Returns time when latest write occurred.
     * @return System.currentTimeMillis()
     */
    public long getTxTime() {
        return txTime;
    }

    /**
     * Returns time when latest read occurred.
     * @return System.currentTimeMillis()     
     */
    public long getRxTime() {
        return rxTime;
    }

    /**
     * Name of the device for debugging purposes. E.g. "Projector left".
     * @return
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Returns true if the write thread has been terminated. 
     * Flag will be reset on method call.
     * @return
     */
    public boolean getWriteError() {
        if (writeError == true) {
            writeError = false;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if the read thread has been terminated. 
     * Flag will be reset on method call.
     * @return
     */
    public boolean getReadError() {
        if (readError == true) {
            readError = false;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds a DeviceConnectionListener that will receive I/O events.
     * @param dcl
     */
    public void addDeviceConnectionListener(DeviceConnectionListener dcl) {
        listeners.add(dcl);
    }

    /**
     * Removes a specific DeviceConnectionListener.
     * @param dcl
     */
    public void removeDeviceConnectionListener(DeviceConnectionListener dcl) {
        listeners.remove(dcl);
    }



    /**
     * Use ICtrlDebugger.addDevice(DeviceConnection dc) instead
     *
     * @param modeOn
     * @deprecated
     */
    @Deprecated
    public void setDebugMode(boolean modeOn) {
        debugMode = modeOn;
    }



    /**
     * DeviceConnection is now able to handle both ascii and non-ascii messages
     * at the same time.
     * @param on
     * @deprecated
     */
    public void setRawMode(boolean on) {
        rawMode = on;
    }

    /**
     * Use ICtrlDebugger.addDevice(DeviceConnection dc) instead
     *
     * @param name
     * @deprecated
     */
    @Deprecated
    public void setDeviceName(String name) {
        this.deviceName = name;
    }

    /**
     * Attempts to connect to the device until disconnect() is called
     */
    public void connect() {
        connect = true;
        connectionScheduler.schedule(new WatchDog(), 0);
    }

    /**
     * Attempts to disconnect the device until connect() is called
     */
    public void disconnect() {
        connect = false;
        connectionScheduler.schedule(new WatchDog(), 0);
    }

    /**
     * Handles when read thread has detected an CF/LF.
     */
    protected void lineDetected() {
        String line;
        try {
            line = new String(lineBuffer.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            line = new String(lineBuffer.toByteArray());
        }

       
        // Parse line
        try {
            for (DeviceConnectionListener dcl : listeners) {                
                dcl.parseLine(line);
            }
        } catch (Exception e) {
            debugMessage("Line parsing error: " + e.getMessage());
            e.printStackTrace();
        }
        // send line to debuggers
        debugMessage("RX: ", line.getBytes());
        lineBuffer.reset(); // line parsed. Reset.
    }

    /**
     * Please use addDeviceConnectionListener(new DeviceConnectionListener()) 
     * for parsing.
     * 
     * @param data
     * @throws Exception
     * @deprecated
     */
    @Deprecated
    public void parseByte(byte data) throws Exception {
    }

    ;

    /**
     * Please use addDeviceConnectionListener(new DeviceConnectionListener()) 
     * for parsing.
     * @param line
     * @throws Exception
     * @deprecated
     */
    @Deprecated
    public void parseLine(String line) throws Exception {
    }

    ;

    /**
     * Please use addDeviceConnectionListener(new DeviceConnectionListener()) 
     * for parsing.
     * @param element
     * @throws Exception
     * @deprecated
     */
    @Deprecated
    public void parseXml(Element element) throws Exception {
    }

    /**
     * Here is where rawRx and rawTx should be created.
     * Every repair attempt will call first disconnectImpl() and then connectImpl() 
     * @throws Exception
     */
    public abstract void connectImpl() throws Exception;

    /**
     * Here is where rawRx and rawTx should be destroyed and nulled.
     * Every repair attempt will call first disconnectImpl() and then connectImpl() 
     * @throws Exception
     */
    public abstract void disconnectImpl() throws Exception;

    /**
     * Please use addDeviceConnectionListener(new DeviceConnectionListener()) 
     * onConnect event 
     * @throws Exception
     * @deprecated
     */
    @Deprecated
    public void initialize() throws Exception {
    }

    ;

    /**
     * Please use addDeviceConnectionListener(new DeviceConnectionListener()) 
     * onConnect event 
     * @throws Exception
     * @deprecated
     */
    @Deprecated
    public void onConnect() throws Exception {
    }

    /**
     * Please use addDeviceConnectionListener(new DeviceConnectionListener()) 
     * onDisconnect event 
     * @throws Exception
     * @deprecated
     */
    @Deprecated
    public void onDisconnect() throws Exception {
    }

    /**     
     * Will start the write thread. Don't use externally unless you know what
     * you're doing.
     */
    public synchronized void initCommandWriter() {
        writerRunning = true;
        setRunThreads(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (runThreads) {
                    try {
                        Command cmd = popNextCommand();
                        sleepTime = cmd.getExecutionTime();
                        debugMessage("TX: ", cmd.getCommand());
                        rawTx.write(cmd.getCommand());
                        rawTx.flush();
                        txTime = System.currentTimeMillis();
                    } catch (IOException e) {
                        writeError = true;
                        debugMessage("An error occured while writing to device: " + e);
                        stopReadAndWrite();
                        break;
                    }
                    try {
                        Thread.sleep(sleepTime);
                        if (!runThreads) {
                            break;
                        }
                    } catch (Exception e) {
                        writeError = true;
                        stopReadAndWrite();
                        break;
                    }
                }
                writerRunning = false;
            }
        }, "writeThread").start();

    }

    /**
     * Will start the read thread. Don't use externally unless you know what
     * you're doing.
     */
    public synchronized void initCommandReader() {
        setRunThreads(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (runThreads) {
                    try {
                        int bytesRead = rawRx.read(byteBuffer);
                        if(bytesRead == -1) {
                            throw new IOException("Reading -1");
                        }
                        byte[] bytesToParse = Arrays.copyOf(byteBuffer, bytesRead);
                        rxTime = System.currentTimeMillis();
                        if(rawMode) {
                            debugMessage("RX: ", bytesToParse);                        
                        }
                        if (lineBuffer.size() > 1024) {
                            lineBuffer.reset(); // this is probably not a line based protocol ?
                        }

                        for (DeviceConnectionListener dcl : listeners) {
                            dcl.parseBytes(bytesToParse);
                        }
                        for (int i = 0; i < bytesRead; i++) {
                            for (DeviceConnectionListener dcl : listeners) {
                                dcl.parseByte(bytesToParse[i]);
                            }
                            char c = (char) bytesToParse[i];
                            if (c == '\n') {
                                if (lookingForLineFeed) {
                                    lookingForLineFeed = false;
                                    continue;
                                } else {
                                    lineDetected();
                                }
                            } else if (c == '\r') {
                                lookingForLineFeed = true;
                                lineDetected();
                            } else {
                                lookingForLineFeed = false;
                                lineBuffer.write(bytesToParse[i]);
                            }
                        }
                    } catch (IOException e) {
                        debugMessage("I/O error while reading: " + e);
                        stopReadAndWrite();
                        readError = true;
                        break;
                    } catch (Exception e) {
                        debugMessage("Byte parsing error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }, "readThread").start();
    }
    
}
