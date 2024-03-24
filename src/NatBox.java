import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class NatBox {
    private static ServerSocket serverSocket = null;
    static HashMap<String, natBoxThread> clients = new HashMap<String, natBoxThread>();
    String natMac;// 2A:85:1A:89:C3:E1(12-bit)
    String natIp;// 5.126.129.96
    static ArrayList<String> publicPool = new ArrayList<>();
    static HashMap<String, String> natTable = new HashMap<String, String>();

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

    private void createPool() {
        for (int i = 0; i < 5; i++) {
            publicPool.add("192.168.1." + i);
        }
    }

    public static String pop() {
        if (publicPool.isEmpty())
            return null;
        String ip = publicPool.get(0);
        publicPool.remove(0);
        return ip;
    }

    public static void push(String ip) {
        publicPool.add(ip);

    }

    public HashMap<String, String> arp() {
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
        natTable.remove(clientIP);// case disconnecte while connected
        clients.remove(clientIP);
    }

    public static void returnToPool(String sourceIP) {
        push(natTable.get(sourceIP));
        natTable.remove(sourceIP);
    }
}

