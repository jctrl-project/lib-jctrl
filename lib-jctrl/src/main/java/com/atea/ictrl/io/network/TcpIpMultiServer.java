package com.atea.ictrl.io.network;




import com.atea.ictrl.io.debugger.ICtrlDebugger;
import java.util.*;
import java.io.*;
import java.net.*;
import org.w3c.dom.Element;

/**
 * The Server class handles all of the incomming connections, passing them to new Descriptor objects, which
 * handle the rest. The Server class also initializes all necessary objects during bootup, and handles shutdown
 * procedures.
 */
public abstract class TcpIpMultiServer extends Thread {
    public static final String charsetUTF8 = "UTF-8";
    public static final String charsetISO_8859_1 = "ISO-8859-1";
    private String deviceName = "";
    /** Should we keep the server running (isClosed=false), or shutdown (isClosed=true)? */
    private boolean isClosed = false;	//are we open or closed?
    /** Port to bind to */
    private int port = 5000;
    /** Timeout length, in millis - 10 minutes of inactivity */
    private int idleTime = 600000;
    
    /** The maximum number of players we will allow on at one time. Set this value to 0 if you dont
     *  want to cap the chatters. (You would set it above 0 if you knew you had a certain amount of
     *  resources, and didnt want to exceed them) */
    private int maxConnections = 10;
    /** A list of all our current connections. We put <Descriptor> in to define the Vector as a list of Descriptors.
     * Also, we initialize the vector with an argument of 1 to set the initial size of the vector to 1. From there, it
     * will automatically increase to allocate the new data. */
    private ArrayList<ServerClient> descriptorList = new ArrayList<ServerClient>();
    /** The number of connections we currently have. We increment this as players connect. */
    private int numberOfClients = 0;
    /** Server Connection Object, which binds to the specified port, and listens for new connections. Instantiated in Main */
    private ServerSocket serverSocket;
    
    private boolean debugMode = false;
    private String charset = "UTF-8";
    private Timer poll;
    private boolean rawMode = false;
    /**
     * Initializes a new Server object.
     */
    public TcpIpMultiServer(int port, int maxConnections) {
        this.port = port;
        this.maxConnections = maxConnections;       
    }
    
    public TcpIpMultiServer(int port, int maxConnections, String charset) {
        this.port = port;
        this.maxConnections = maxConnections;   
        this.charset = charset;        
    }
    class RemoveDeadClientsTask extends TimerTask {
        @Override
        public void run() {
            for (ServerClient sc : descriptorList) {  
                try {
                    if (System.currentTimeMillis() - sc.getRxTime() > idleTime) {
                        sc.stopClient();
                        removeClient(sc);
                        ICtrlDebugger.log.print(deviceName + ": " + "Removed 'dead' client " + sc.getIpAddress());
                        break;
                    }
                } catch (Exception ex) {
                    ICtrlDebugger.log.print(deviceName + ": Failed remove 'dead' client " + sc.getIpAddress() + " (or it was already been removed)");
                    ICtrlDebugger.log.print(deviceName + ": " + ex);
                }
            }
        }
    }
    @Override
    public void start() {
        initServer();
        poll = new Timer();
        poll.scheduleAtFixedRate(new RemoveDeadClientsTask(), 1000, 1000);
    }
    public void setRawMode(boolean aFlag) {
        this.rawMode = aFlag;
    }
    public void setDeviceName(String name) {
        this.deviceName = name;
    }
    private void initServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(port);
                    while (isClosed == false) {
                        ICtrlDebugger.log.println(deviceName + ": " + "Listening...");
                        /** Accept the next connection */
                        Socket connection = serverSocket.accept();
                        /** Set some values on that connection to ensure that it doesnt lag wrongly. */
                        connection.setTcpNoDelay(true);
                        connection.setSoLinger(false, 0);
                        /** Find the address of the connection. We can check this against a ban list,
                         *  and we can use this for logs */
                        InetAddress addr = connection.getInetAddress();

                        
                        ICtrlDebugger.log.println(deviceName + ": " + "New connection request from " + addr.getHostName() + "@" + addr.getHostAddress());

                        /** Check if we already have that client but failed to detect its disconnection.
                         * 
                         */
                        for(ServerClient sc:descriptorList) {
                            if(sc.getIpAddress().equals(addr.getHostAddress())) {
                                sc.stopClient();
                                removeClient(sc);  
                                break;
                            }
                        }
                        /** If we only allow a certain number of connections, then heres where we deny
                         *  any exceeding that number */
                        if (maxConnections > 0 && numberOfClients >= maxConnections) {
                            /** Send a message to the player */
                            /** Create our address and output objects, so we can process output and log */
                            PrintStream pout = new PrintStream(connection.getOutputStream());
                            pout.println("Serving maximum number of clients, please try again later.");
                            pout.flush();
                            /** Print a message */
                            ICtrlDebugger.log.print(deviceName + ": " + "User " + addr.getHostName() + "." + addr.getHostAddress() + " denied access.");
                            ICtrlDebugger.log.print(deviceName + ": " + "Already " + numberOfClients + " connections.");
                            /** Close the connection */
                            connection.close();
                            /** Break out of our loop */
                            continue;
                        } /** Otherwise, accept the connection and process the request */
                        else {
                            createClient(connection);
                            continue;
                        }
                    }
                } catch (BindException be) {
                    ICtrlDebugger.log.println(deviceName + ": " + "Bind exception, SERVICE_PORT probably in use");
                    be.printStackTrace();
                    return;
                } catch (SocketException se) {
                    /** If we didnt want to continue, a SocketException is normal here when shutting down.
                     *  Therefore, check if a close was requested. If not, only then do we print a message */
                    if (isClosed != true) {
                        ICtrlDebugger.log.println(deviceName + ": " + "Socket exception caught in Server, possibly closing ServerSocket.");
                        se.printStackTrace();
                    }
                } catch (IOException ioe) {
                    ICtrlDebugger.log.println(deviceName + ": " + "IOException caught in Server, possibly executing new thread in ThreadPool");
                    ioe.printStackTrace();
                } catch (Exception e) {
                    /** Catch any other exeption we didnt prepare for */
                    ICtrlDebugger.log.println(deviceName + ": " + "Exception caught in Server.");
                    e.printStackTrace();
                } finally {
                    /** Then, when were all done, print our final messages, and quit */
                    ICtrlDebugger.log.println(deviceName + ": " + "Server class has been shut down.");
                    /** If it was a valid shutdown, close System, otherwise leave open for debugging */
                }
            }
        }).start();
    }
    private void createClient(Socket connection) {
        ServerClient client = new ServerClient(connection, this, charset);
        client.setDebugMode(debugMode);
        client.setRawMode(rawMode);
        addClient(client);
        /** Continue our loop, and wait for new players */
        Thread handler = new Thread(client);
        handler.start();
        ICtrlDebugger.log.println(deviceName + ": " + "Accepted request from " + client.getClientSocket().getInetAddress());
    }
    public void sendCommandToAll(String line, int executionTime) {
        Iterator it = descriptorList.iterator();
        /** Iterate over each one */
        while (it.hasNext()) {
            /** Pull the descriptor from the iterator */
            ServerClient desc = (ServerClient) it.next();
            /** Send the message */
            desc.sendCommand(line, executionTime);
        }

    }
    public synchronized ArrayList<ServerClient> getClients() {
        return descriptorList;
    }
    public synchronized void setDebugMode(boolean modeOn) {
        this.debugMode = modeOn;
        for(int i=0;i<descriptorList.size();i++) {
            descriptorList.get(i).setDebugMode(modeOn);
        }        
    }
    public abstract void parseLine(String line, ServerClient client) throws Exception;
    public abstract void parseByte(byte data, ServerClient client) throws Exception;
    public abstract void parseXml(Element element, ServerClient client) throws Exception;
    public abstract void onConnect(ServerClient client) throws Exception;
    public abstract void onDisconnect(ServerClient client) throws Exception;
    public boolean isClosed() {
        return isClosed;
    }
    /** Shutdown our Server object */
    public void shutdown() {
        /** Set our variable so we know 1) Not to continue, and 2) It was requested */
        isClosed = true;
        /** Close all connections, and log everything */
        ICtrlDebugger.log.println(deviceName + ": " + "Server shutting down!");
        try {
            serverSocket.close();
        } catch (IOException ioe) {
            ICtrlDebugger.log.println(deviceName + ": " + "IOException caught while closing ServerSocket");
            ioe.printStackTrace();
        } catch (Exception e) {
            ICtrlDebugger.log.println(deviceName + ": " + "Exception caught shutting down");
            e.printStackTrace();
        }
    }

    /** Add the specified descriptor to our list */
    public synchronized void addClient(ServerClient client) {
        /** Check the validity of the descriptor */
        if (client == null) {
            /** Print a message */
            ICtrlDebugger.log.println(deviceName + ": " + "Invalid descriptor passed");
            return;
        }
        descriptorList.add(client);
        /** Increase our connections */
        numberOfClients++;
        ICtrlDebugger.log.println(deviceName + ": " + "Serving " + numberOfClients + " (" + 
                    descriptorList.size() + ") client(s).");
    }

    /** Removes the specified descriptor from our list */
    public synchronized void removeClient(ServerClient client) {
        /** Check the validity of the descriptor */
        if (client == null) {
            /** Print a message */
            ICtrlDebugger.log.println(deviceName + ": " + "Invalid descriptor passed");
            return;
        }        
        if(descriptorList.contains(client)) {
            descriptorList.remove(client);

            /** Decrease our connections */
            numberOfClients--;
            ICtrlDebugger.log.println(deviceName + ": " + "Serving " + numberOfClients + " (" + 
                    descriptorList.size() + ") client(s).");
        } 
        client = null;
    }

}
