
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

/**
 * 
 */
public class Client {
    // Var Dec
    private static String MAC;
    private static String IP;
    static Socket socket;
    static ObjectInputStream in;
    static ObjectOutputStream out;
    static boolean connected;

    // Main
    /**
     * @param args
     */
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

        MAC = generateRandomMacAddress();
        if (mode.equals("external")) {
            IP = generateRandomIpAddress(false);
            System.out.println("IP adress:" + IP);
            try {
                socket = new Socket("localhost", 1234);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                p = new Paquet(IP, null, Type.DHCP_REQUEST, null);
                p.setSourceMac(MAC);
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
                p.setSourceMac(MAC);
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
        InnerClient iClient = new InnerClient(socket,in, out);
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
                        p.setSourceMac(MAC);
                        send(p);
                        break;
                    } else if (input.startsWith("echo")) {
                        destIP = input.substring(5);
                        p = new Paquet(IP, destIP, Type.ECHO_REQUEST, null);
                        p.setSourceMac(MAC);
                        send(p);
                    } else {
                        destIP = input.substring(0, input.indexOf(" "));
                        msg = input.substring(input.indexOf(" "));
                        p = new Paquet(IP, destIP, Type.TCP, msg);
                        p.setSourceMac(MAC);
                        send(p);
                    }
                    System.out.println(input);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.println("Disconnected");
    }

    /**
     * @param var
     */
    public static void setConnected(boolean var) {
        connected = var;
    }

    // Paquet Reciver
    /**
     * @return
     */
    public static Paquet receive() {
        try {
            Paquet p = (Paquet) in.readObject();
            Paquet newP = new Paquet(IP, null, Type.ACK, null);
            newP.setSourceMac(MAC);
            send(newP);
            if (p.getType() == Type.ECHO_REQUEST) {
                newP = new Paquet(IP, p.getSourceIP(), Type.ECHO_REPLY, null);
                newP.setSourceMac(MAC);
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
    /**
     * @param p
     */
    public static void send(Paquet p) {
        try {
            out.writeObject(p);
            out.reset();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // copied from https://iqcode.com/code/java/random-mac-address-generator-java
    /**
     * @return
     */
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
    /**
     * @param internal
     * @return
     */
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

    Socket socket;
    ObjectInputStream in;
    ObjectOutputStream out;

    public InnerClient(Socket socket,
            ObjectInputStream in,
            ObjectOutputStream out) {

        this.socket = socket;
        this.in = in;
        this.out = out;

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

}