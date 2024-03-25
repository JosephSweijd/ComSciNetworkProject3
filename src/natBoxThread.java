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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isConnected'");
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

        }
        newP = new Paquet(sIP, ip, Type.ERROR, "No ip available");
        try {
            sendPaquet(newP);
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

        // broadcast message of disconnection to users?

        // print to console then closes streams and client socket

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
                p = getPaquet();
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
                    packetForwarding(p);
                    break;

                default:
                    break;

            }
        }

    }

    private void arp(Paquet p) {
        String ip = p.getSourceIP();
        String natIp = NatBox.arp().get("IP");
        String natMac = NatBox.arp().get("MAC");
        Paquet newP = new Paquet(natIp, ip, Type.ARP_REPLY, natIp + "|" + natMac);
        try {
            sendPaquet(newP);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Paquet getPaquet() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPaquet'");
    }

    private boolean isInternal(String ip) {
        // private access list is 10.1.0.x
        if (ip.startsWith("10.1.0.")) {
            return true;
        }
        return false;
    }

    private void packetForwarding(Paquet p) {
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
                // internal to external (this assumes clients do dhcp during packet forwading)
                dhcp(p);
                Paquet newP = getPaquet();
                if (newP.getType() == Type.DHCP_REPLY) {
                    String publicIp = newP.getPayload();
                    p.setSourceIP(publicIp);
                    try {
                        sendPaquet(p);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    // packet dropped
                }

            }
        } else {
            String dPublic = p.getDestIP();
            if (dPublic.startsWith("192.168.1.")) {
                String dPrivate = NatBox.getIpFromTable(dPublic);
                if (dPrivate == null) {
                    // drop packet ip not assigned
                }
            } else {
                // drop packet
            }

        }
    }

    // NAT-box status reporting 2
    // Packets replied to from clients 2
    // Internal/External 4
    // receiving messages
    // Connections/disconnections
}
