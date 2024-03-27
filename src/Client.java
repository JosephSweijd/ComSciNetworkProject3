//1. Either be designated as an internal or external client, as discussed above.
// 2. IP addresses should reflect clients’ status as either internal or external clients.
// 3. Internal clients should use a minimal DHCP implementation to request an IP address.
// 4. Every client must have a unique (simulated) MAC address.
// 5. No GUI is required, only CLI (command-line interface) is necessary.
// 6. The client must provide a reply for every paquet received (This is not the case with ICMP error
//paquets). This may take the form of an ACK or a similar type of packet.

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

public class Client {
    // Var Dec
    private static String MAC;
    private static String IP;
    static Socket socket;
    static ObjectInputStream in;
    static ObjectOutputStream out;
    static boolean connected;

    // Main
    public static void main(String[] args) {
        // Var dec
        if (args.length < 1) {
            System.out.println("Incorrect usage: args 1 must be either \"internal\" or \"external\"");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input, msg, destIP;
        Paquet p;
        // Initialise
        String mode = args[0];
        System.out.println("Flag 4");
        MAC = generateRandomMacAddress();
        if (mode.equals("external")) {
            IP = generateRandomIpAddress(false);
            System.out.println("IP adress:" + IP);
            try {
                socket = new Socket("localhost", 1234);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                p = new Paquet(IP, null, Type.DHCP_REQUEST, null);
                send(p);
                p = receive();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (mode.equals("internal")) {
            IP = generateRandomIpAddress(true);
            try {
                // Open Conneciton
                socket = new Socket("localhost", 1234);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                p = new Paquet(IP, null, Type.DHCP_REQUEST, null);
                send(p);
                p = receive();
                IP = p.getPayload();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (IP.equals("No ip available")) {
                System.out.println(IP);
            } else {
                System.out.println("IP adress set to: " + IP);
            }
        } else {
            System.out.println("Incorrect usage: args 1 must be either \"internal\" or \"external\"");
            return;
        }
        // Listen for paquets
        InnerClient iClient = new InnerClient();
        Thread t = new Thread(iClient, "iClient");
        t.start();
        connected = true;
        // listen for inputs
        while (connected) {
            try {
                input = reader.readLine();
                if (connected) {
                    if (input.equals("disconnect")) {
                        p = new Paquet(IP, null, Type.DISCONNECT, input);
                        send(p);
                        break;
                    } else if (input.startsWith("echo")) {
                        destIP = input.substring(5);
                        p = new Paquet(IP, destIP, Type.ECHO_REQUEST, null);
                        send(p);
                    } else {
                        destIP = input.substring(0, input.indexOf(" "));
                        msg = input.substring(input.indexOf(" "));
                        p = new Paquet(IP, destIP, Type.TCP, msg);
                        send(p);
                    }
                    System.out.println(input);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("Disconnected");
    }

    public static void setConnected(boolean var) {
        connected = var;
    }

    // Paquet Reciver
    public static Paquet receive() {
        try {
            Paquet p = (Paquet) in.readObject();
            Paquet newP = new Paquet(IP, null, Type.ACK, null);
            send(newP);
            if (p.getType() == Type.ECHO_REQUEST) {
                newP = new Paquet(IP, p.getSourceIP(), Type.ECHO_REPLY, null);
                send(newP);
            }
            if (p.getType() == Type.DISCONNECT) {
                connected = false;
            }
            return p;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }

    // Packet sender
    public static void send(Paquet p) {
        System.out.println(p.toString());
        try {
            out.writeObject(p);
            out.reset();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // copied from https://iqcode.com/code/java/random-mac-address-generator-java
    private static String generateRandomMacAddress() {
        Random random = new Random();
        StringBuilder mac = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int n = random.nextInt(256);
            mac.append(String.format("%02x", n));
            if (i < 5) {
                mac.append(":");
            }
        }
        return mac.toString().toUpperCase();
    }

    // copied from internet still need to check if doesnt violate any rules
    private static String generateRandomIpAddress(boolean internal) {
        Random random = new Random();
        if (internal) {
            return "10.1.0." + random.nextInt(256);
        } else {
            return random.nextInt(256) + "." +
                    random.nextInt(256) + "." +
                    random.nextInt(256) + "." +
                    random.nextInt(256);
        }
    }
}

class InnerClient implements Runnable {
    /*
     * Socket socket;
     * ObjectInputStream in;
     * ObjectOutputStream out;
     */

    public InnerClient() {
        /*
         * this.socket = socket;
         * this.in = in;
         * this.out = out;
         */
    }

    @Override
    public void run() {
        Paquet p;
        while (true) {
            p = Client.receive();

            System.out.println(p.toString());
            if (p.getType() == Type.DISCONNECT) {
                break;
            }
        }
        System.out.println("Disconnected");

    }

}