import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Creates a Server that acts as Internet and makes use of NAT to communicate
 * devices within a private network or across the internet
 */
public class NatBox {
    private static ServerSocket serverSocket = null;
    static HashMap<String, natBoxThread> clients = new HashMap<String, natBoxThread>();
    static String natMac;
    static String natIp;
    static ArrayList<String> publicPool = new ArrayList<>();
    static ArrayList<String> privatePool = new ArrayList<>();
    static HashMap<String, String> natTable = new HashMap<>();

    /**
     * @param port
     * @throws IOException
     */
    public NatBox(int port) throws IOException {
        serverSocket = new ServerSocket(port);// start server
        System.out.println("Server started");
        natIp = "25.31.79.190";
        System.out.println("NAT IP Address: " + natIp);// ??
        String randomMacAddress = generateRandomMacAddress();
        System.out.println("NAT Mac Address: " + randomMacAddress);
        /*
         * Socket natSocket = new Socket(natIp, 5002);
         * System.out.println("Flag 7");
         * natBoxThread natThread = new natBoxThread(natSocket);
         * System.out.println("Flag 8");
         * natThread.start();
         * System.out.println("Flag 9");
         * clients.put(natIp, natThread);
         */

        createPool();
        createPrivatePool();
        while (true) {
            try {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                } catch (SocketException e) {
                    break;
                }
                Thread t = new natBoxThread(clientSocket);
                t.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    /**
     * Creates a pool of public addresses
     */
    private void createPool() {
        for (int i = 0; i < 5; i++) {
            publicPool.add("192.168.1." + i);
        }
    }

    /**
     * Creates a pool of public addresses
     */
    private void createPrivatePool() {
        for (int i = 0; i < 100; i++) {
            privatePool.add("10.1.0." + i);
        }
    }

    /**
     * gets public address from pool
     * 
     * @return public ip address
     */
    public static String pop(boolean priv) {
        if (priv) {
            if (privatePool.isEmpty())
                return null;
            String ip = privatePool.get(0);
            privatePool.remove(0);
            return ip;
        } else {
            if (publicPool.isEmpty())
                return null;
            String ip = publicPool.get(0);
            publicPool.remove(0);
            return ip;
        }
    }

    /**
     * add ip address to the pool
     * 
     * @param ip - public address
     */
    public static void push(String ip) {
        publicPool.add(ip);
        natTable.remove(ip);

    }

    /**
     * @return - natbox header with mac and ip address
     */
    public static String arp() {
        return natMac;
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

    public static void main(String[] args) throws IOException {
        /*
         * MOVING 2 SIMULATOR
         * try {// starts server locally
         * new NatBox(5002);
         * 
         * } catch (IOException e) {
         * throw new RuntimeException(e);
         * }
         */
    }

    public static void addEntry(String ip, String publicIP) {
        natTable.put(ip, publicIP);
    }

    public static synchronized void removeClient(String clientIP) {
        push(clientIP);// case disconnecte while connected
        clients.remove(clientIP);
    }

    public static synchronized void addClient(String clientIP, natBoxThread thread) {
        clients.put(clientIP, thread);
    }

    public static void send(Paquet p) throws IOException {

        String ip = p.getDestIP();
        System.out.println("IP is :" + ip);
        natBoxThread link = clients.get(ip);
        boolean connected = link.isConnected();
        System.out.println("link is connected:" + connected);
        if (connected) {
            link.sendPaquet(p);
        }
    }

    public static String getPrivateIP(String publicIp) {
        for (Entry<String, String> entry : natTable.entrySet()) {
            System.out.println(entry);
            System.out.println(entry.getValue());
            System.out.println(entry.getKey());

            // if give value is equal to value from entry
            // print the corresponding key
            if (entry.getValue().equals(publicIp)) {
                return entry.getKey();

            }
        }
        return null;

    }

    public static String getPublicIp(String privateIp) {
        return natTable.get(privateIp);

    }

}
