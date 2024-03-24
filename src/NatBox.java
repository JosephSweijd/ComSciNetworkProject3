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
    static HashMap<String, String> natTable = new HashMap<>();

    /**
     * @param port
     * @throws IOException
     */
    public NatBox(int port) throws IOException {
        serverSocket = new ServerSocket(port);// start server
        System.out.println("Server started");
        String randomIpAddress = generateRandomIpAddress();
        System.out.println("NAT IP Address: " + randomIpAddress);
        String randomMacAddress = generateRandomMacAddress();
        System.out.println("NAT Mac Address: " + randomMacAddress);
        createPool();
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
     * gets public address from pool
     * 
     * @return public ip address
     */
    public static String pop() {
        if (publicPool.isEmpty())
            return null;
        String ip = publicPool.get(0);
        publicPool.remove(0);
        return ip;
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
    public static HashMap<String, String> arp() {
        HashMap<String, String> natHeader = new HashMap<String, String>();
        natHeader.put("Mac", natMac);
        natHeader.put("IP", natIp);
        return natHeader;
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
    private static String generateRandomIpAddress() {
        Random random = new Random();
        return random.nextInt(256) + "." +
                random.nextInt(256) + "." +
                random.nextInt(256) + "." +
                random.nextInt(256);
    }

    public static void main(String[] args) throws IOException {
        try {// starts server locally
            new NatBox(5002);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addEntry(String ip, String publicIP) {
        natTable.put(ip, publicIP);
    }

    public static synchronized void removeClient(String clientIP) {
        push(clientIP);// case disconnecte while connected
        clients.remove(clientIP);
    }

    public static void send(Paquet p) throws IOException {

        String ip = p.getdIP();
        natBoxThread link = clients.get(ip);
        boolean connected = link.isConnected();
        if (connected) {
            link.sendPaquet(p);
        }
    }

    public static String getIpFromTable(String publicIp) {
        for (Entry<String, String> entry : natTable.entrySet()) {

            // if give value is equal to value from entry
            // print the corresponding key
            if (entry.getValue() == publicIp) {
                return entry.getKey();

            }
        }
        return null;

    }

}
