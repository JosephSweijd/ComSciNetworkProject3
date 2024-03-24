import java.net.*;
import java.io.*;

public class natBoxThread extends Thread {
    private final Socket clientSocket;
    private boolean isActive = false;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean internalIP;
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
     * Sends paquest to the (network)destination IP
     */
    public void sendMessage(Paquet p) throws IOException {

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
    public Paquet dhcp(String ip) {
        if (internalIP) {
            String publicIP = NatBox.pop();
            if (publicIP != null) {
                NatBox.addEntry(ip, publicIP);
            } else {
                // no public address available
            }

        }
        return new Paquet();
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
            Type pType = Type.ECHO_REPLY;
            Paquet p = null;
            if (!clientSocket.isClosed()) {
                p = getPaquet();
                pType = p.getType();
            }
            // assumes message is never null
            // how to handle different type of messages
            switch (pType) {
                case ARP_REPLY:

                    break;

                case DCHP_REQUEST:
                    internalIP = p.getData();
                    dhcp(p.getSourceIP());
                    break;
                case TIME_EXCEEDED:
                    NatBox.returnToPool(p.getSourceIP());
                    break;
                case REDIRECT:

                    break;
                case PARAMETER_PROBLEM:

                    break;
                case ARP_REQUEST: 
                    break;
                case ECHO_REQUEST: 
                    break;
                case DESTINATION_UNREACHABLE: 
                    break;
                case DHCP_REPLY:
                    break;
                case ECHO_REPLY:

                    break;
                default:
                    break;

            }
        }

    }

    private Paquet getPaquet() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPaquet'");
    }

 
}
