import java.net.*;
import java.io.*;

public class natBoxThread extends Thread {
    private final Socket clientSocket;
    private boolean isActive = false;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String clientIP;

    /*
     * Initializes the natBoxThread with client's socket and sets thread as active
     */
    public natBoxThread(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        setStreams();
        isActive = true;

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
    public void sendPaquet(Paquet p) throws IOException {

        out.writeObject(p);
        out.flush();

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
        boolean internal = isInternal(ip);
        String sIP = p.getDestIP();
        Paquet newP = null;
        if (internal) {
            String publicIP = NatBox.pop();
            if (publicIP != null) {
                NatBox.addEntry(ip, publicIP);
                newP = new Paquet(sIP, ip, Type.DHCP_REPLY, publicIP);

            }
            NatBox.addClient(ip, this);
        }
        newP = new Paquet(sIP, ip, Type.ERROR, "No ip available");
        try {
            NatBox.send(newP);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * IP address released into the pool of available IP when an internal client
     * disconnects.
     * 
     * @throws IOException
     */
    public void disconnect() throws IOException {
        isActive = false;
        NatBox.removeClient(clientIP);
        in.close();
        out.close();
    }

    /*
     * Listens for new Paquet and handles different types of paquest
     */
    @Override
    public void run() {
        while (isActive) {
            Type pType = null;
            Paquet p = null;
            if (!clientSocket.isClosed()) {
                try {
                    p = getPaquet();
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                pType = p.getType();
                clientIP = p.getSourceIP();
            }
            // assumes message is never null
            // how to handle different type of messages
            switch (pType) {

                case DHCP_REQUEST:
                    dhcp(p);
                    break;
                case DHCP_RELEASE:
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

                default:
                    break;

            }
        }

    }

    private void arp(Paquet p) {
        String ip = p.getSourceIP();
        p.setSourceIP(p.getDestIP());
        p.setDestIP(ip);
        p.setType(Type.ARP_REPLY);
        try {
            sendPaquet(p);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Paquet getPaquet() throws ClassNotFoundException {
         //get Paquet from client
         try {
            return (Paquet) in.readObject();
        }catch (IOException e){
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
        if (sInternal) {
            if (dInternal) {
                // internal > internal
                try {
                    NatBox.send(p);
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
                    rPaquet.setType(Type.DHCP_REQUEST);
                    try {
                        sendPaquet(rPaquet);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Paquet inPaquet = getPaquet();
                    if (inPaquet.getType() == Type.DHCP_REPLY) {
                        p.setSourceIP(inPaquet.getPayload());
                        try {
                            NatBox.send(p);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("\n *Paquet dropped due to no available public adress*");
                        p.setType(Type.ERROR);
                        System.out.println(p.toString());
                    }

                } else {
                    try {
                        NatBox.send(p);
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
                if (dPrivate == null) {
                    // drop Paquet ip not assigned
                    System.out.println("\n *Paquet dropped due to public address not being assigned*");
                    
                    System.out.println(p.toString());
                } else {
                    p.setDestIP(dPrivate);
                    p.setType(Type.ECHO_REPLY);
                    NatBox.send(p);
                }
            } else {
                // drop Paquet
                System.out.println("\n *Paquet dropped External-> External*");
                System.out.println(p.toString());
            }

        }
    }

    // NAT-box status reporting 2
    // Packets replied to from clients 2
    // Internal/External 4
    // Connections/disconnections
}
