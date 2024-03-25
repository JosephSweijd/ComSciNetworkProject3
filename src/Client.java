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

    // Main
    public static void main(String[] args) {
        // Var dec
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String msg;
        Paquet p;
        // Initialise
        String mode = args[0];
        MAC = generateRandomMacAddress();
        if (mode == "external") {
            IP = generateRandomIpAddress(false);
            try {
                socket = new Socket(args[1], 5002);
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (mode == "internal") {
            IP = generateRandomIpAddress(true);
            try {
                // Open Conneciton
                socket = new Socket("localhost", 5002);
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                p = new Paquet(Type.DCHP_REQUEST, null, null, null, null, null, null, (Boolean) null);
                out.writeObject(p);
                p = receive();
                IP = p.getContent();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println("Incorrect usage: args 1 must be either \"internal\" or \"external\"");
            return;
        }
        while (true) {
            try {
                msg = reader.readLine();
                if (msg.startsWith("/a")) {
                    p = new Paquet(Type.ARP_REQUEST, IP, null, msg, null, null, null, (Boolean) null);
                    out.writeObject(p);
                }
                if (msg.startsWith("/e")) {
                    p = new Paquet(Type.ECHO_REQUEST, IP, null, msg, null, null, null, (Boolean) null);
                    out.writeObject(p);
                }
                System.out.println(msg);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    // Paquet Reciver
    private static Paquet receive() {
        Paquet p;
        try {
            p = (Paquet) in.readObject();
            if (p.getType() == Type.DCHP_REQUEST) {
                out.writeObject(new Paquet(Type.DHCP_REPLY, null, null, null, null, null, null, (Boolean) null));
            }
            if (p.getType() == Type.ECHO_REQUEST) {
                out.writeObject(new Paquet(Type.ECHO_REPLY, null, null, null, null, null, null, (Boolean) null));
            }
            if (p.getType() == Type.ARP_REQUEST) {
                out.writeObject(new Paquet(Type.ARP_REPLY, null, null, null, null, null, null, (Boolean) null));
            }
            return p;
        } catch (ClassNotFoundException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
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

    static Socket socket;
    static ObjectInputStream in;
    static ObjectOutputStream out;
    static Paquet p;

    public InnerClient(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        while (true) {
            try {
                p = (Paquet) in.readObject();
            } catch (ClassNotFoundException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}