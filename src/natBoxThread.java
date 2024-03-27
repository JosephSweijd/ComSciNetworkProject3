import java.net.*;
import java.time.Duration;
import java.time.LocalTime;
import java.io.*;

public class natBoxThread extends Thread {
    private final Socket clientSocket;
    private boolean isActive = false;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String clientIP;
    private LocalTime lastUsed;
    private boolean internal;
    private boolean ack;
    private boolean toself;
    private int timeout;

    /*
     * Initializes the natBoxThread with client's socket and sets thread as active
     */
    public natBoxThread(Socket clientSocket,int timeout) throws IOException {
        this.clientSocket = clientSocket;
        setStreams();
        isActive = true;
        lastUsed = LocalTime.now();
        this.timeout = timeout;

    }

    /*
     * Sets up input and output streams for communication
     */
    public void setStreams() {
        // the server thread handles communication between each client and server
        try {
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error while creating streams");
        }
    }

    /*
     * Sends paquet to the client
     */
    public boolean sendPaquet(Paquet p) throws IOException {
        ack = false;
        out.writeObject(p);
        out.reset();
        if (toself){
            Paquet newp;
            try {
                newp = getPaquet();
                if (newp.getType() == Type.ACK) {
                    // System.out.println("Acknoleged");
                    return true;
                }
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        LocalTime sent = LocalTime.now();
        while (Duration.between(sent, LocalTime.now()).toSeconds() < timeout) {
            if (ack){
                // System.out.println("Acknoleged");
                return true;
            }
        }
        return false;

    }

    public boolean isConnected() {
        return isActive;
    }

    public void logout() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
    }

    /**
     * Assigns Internal clients unique IP addresses
     * 
     * @param ip - private IP address to be mapped to public
     * @return paquest to notify if an ip was assigned
     */
    public void dhcp(Paquet p) {
        String ip = p.getSourceIP();
        internal = isInternal(ip);
        String sIP = p.getDestIP();
        Paquet newP = null;
        if (internal) {
            String publicIP = NatBox.pop(false);
            String privateIP = NatBox.pop(true);
            if (publicIP != null) {
                NatBox.addEntry(privateIP, publicIP);
                newP = new Paquet(sIP, privateIP, Type.DHCP_REPLY, privateIP);
            } else {
                newP = new Paquet(sIP, ip, Type.ERROR, "No ip available");
            }
            clientIP = privateIP;
            NatBox.addClient(privateIP, this);
        } else {
            clientIP = ip;
            NatBox.addClient(ip, this);
            newP = new Paquet(sIP, ip, Type.DHCP_REPLY, "connected");
        }
        try {
            NatBox.send(newP);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean isInternal() {
        return internal;
    }

    /**
     * IP address released into the pool of available IP when an internal client
     * disconnects.
     * 
     * @throws IOException
     */
    public void disconnect() throws IOException {
        Paquet newP = new Paquet(NatBox.natIp, clientIP, Type.DISCONNECT, "disconnected");
        NatBox.send(newP);
        isActive = false;
        NatBox.removeClient(clientIP, internal);
        in.close();
        out.close();
        clientSocket.close();
    }

    public LocalTime getLastUsed() {
        return lastUsed;
    }

    /*
     * Listens for new Paquet and handles different types of paquest
     */
    @Override
    public void run() {
        while (isActive) {
            toself = false;
            Type pType = null;
            Paquet p = null;
            if (!clientSocket.isClosed()) {
                try {
                    p = getPaquet();
                } catch (ClassNotFoundException e) {
                    // AD more later
                    break;
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }
                if (p != null) {

                    pType = p.getType();
                    clientIP = p.getSourceIP();
                    lastUsed = LocalTime.now();
                    // how to handle different type of paquets
                    switch (pType) {

                        case DHCP_REQUEST:
                        toself = true;
                            dhcp(p);
                            break;
                        case DHCP_RELEASE:
                            // update later
                        toself = true;
                        NatBox.push(clientIP);
                            break;

                        case ARP_REQUEST:
                            arp(p);
                            break;

                        case ECHO_REQUEST:
                            try {
                                packetForwarding(p);
                            } catch (ClassNotFoundException | IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            break;

                        case ECHO_REPLY:
                            try {
                                packetForwarding(p);
                            } catch (ClassNotFoundException | IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            break;

                        case TCP:
                            try {
                                packetForwarding(p);
                            } catch (ClassNotFoundException | IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            break;

                        case DISCONNECT:
                            try {
                        toself = true;
                        disconnect();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            break;

                        case ACK:
                            ack =true;
                            break;

                        default:
                            break;

                    }
                }
            }
        }

    }

    private void arp(Paquet p) {
        String ip = p.getSourceIP();
        p.setSourceIP(p.getDestIP());
        p.setDestMac(p.getSourceMac());
        p.setDestIP(ip);
        p.setType(Type.ARP_REPLY);
        p.setSourceMac(NatBox.natMac);
        try {
            NatBox.send(p);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Paquet getPaquet() throws ClassNotFoundException {
        // get Paquet from client
        try {
            Paquet p = (Paquet) in.readObject();
            return p;
        } catch (IOException e) {
          e.printStackTrace();
            return null;
        }
    }

    private boolean isInternal(String ip) {
        // private access list is 10.1.0.x
        if (ip.startsWith("10.1.0.")) {
            return true;
        }
        return false;
    }

    private void packetForwarding(Paquet p) throws ClassNotFoundException, IOException {
        boolean sInternal = isInternal(p.getSourceIP());
        boolean dInternal = isInternal(p.getDestIP());
        boolean sent = false;
        if (sInternal) {
            if (dInternal) {
                // internal > internal
                try {
                   sent =  NatBox.send(p);
                } catch (IOException e) {
                    // error while sending a paquet
                    e.printStackTrace();
                }
            } else {
                // internal to external
                String publicIp = NatBox.getPublicIp(p.getSourceIP());
                if (publicIp == null) {
                    Paquet rPaquet = p;
                    rPaquet.setDestIP(NatBox.natIp);
                    rPaquet.setDestMac(NatBox.natMac);
                    rPaquet.setType(Type.DHCP_REQUEST);
                    try {
                        sent =  NatBox.send(rPaquet);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Paquet inPaquet = getPaquet();
                    if (inPaquet.getType() == Type.DHCP_REPLY) {
                        p.setSourceIP(inPaquet.getPayload());
                        try {
                            sent = NatBox.send(p);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("\n *Paquet dropped due to no available public adress* \n");
                        p.setType(Type.ERROR);
                        System.out.println(p.toString());
                    }

                } else {
                    p.setSourceIP(publicIp);
                    try {
                        sent = NatBox.send(p);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        } else {
            String dPublic = p.getDestIP();
            if (dPublic.startsWith("192.168.1.")) {
                String dPrivate = NatBox.getPrivateIP(dPublic);
                // System.out.println(NatBox.natTable);
                if (dPrivate == null) {
                    // drop Paquet ip not assigned
                    System.out.println("\n *Paquet dropped due to public address not being assigned* \n");

                    System.out.println(p.toString());
                } else {
                    p.setDestIP(dPrivate);
                    p.setType(Type.ECHO_REPLY);
                    sent = NatBox.send(p);
                }
            } else {
                // drop Paquet
                System.out.println("\n *Paquet dropped External-> External* \n");
                System.out.println(p.toString());
            }

        }
        if (!sent) {
            System.out.println("\n *Paquet Failed to send, sending ICMP ERROR MSG* \n ");
            Paquet newP = new Paquet(NatBox.natIp, clientIP, Type.ERROR, clientIP);
            NatBox.send(newP);
        }
    }

}
