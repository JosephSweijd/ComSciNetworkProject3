import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * Makes use of NAT to communicate clients within a private network or across the n
 */
public class NatBox {
    private static ServerSocket serverSocket = null;
    static HashMap<String, natBoxThread> clients = new HashMap<String, natBoxThread>();
    static String natMac;
    static String natIp;
    static ArrayList<String> publicPool = new ArrayList<>();
    static ArrayList<String> privatePool = new ArrayList<>();
    static HashMap<String, String> natTable = new HashMap<>();

    /** Accepts connections and creates pool of both private and public ip addresses
     * @param port - nat server port
     * @throws IOException
     */
    public NatBox(int port) throws IOException {
        serverSocket = new ServerSocket(port);// start server
        System.out.println("Nat Server started");
        natIp = "25.31.79.190";
        System.out.println("NAT IP Address: " + natIp);
        String randomMacAddress = generateRandomMacAddress();
        System.out.println("NAT Mac Address: " + randomMacAddress);
       
        createPool();
        createPrivatePool();
        Thread refresh = new ntRefresh(clients, 600);
        refresh.start();
        while (true) {
            try {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                } catch (SocketException e) {
                    break;
                }
                Thread t = new natBoxThread(clientSocket,100);
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
        privatePool.add(ip);
        publicPool.add(natTable.get(ip));
        natTable.remove(ip);
    }

    /** communicates natbox mac with clients
     * @return - natbox mac
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

    }

    /** Maps private ip to public ip in the nat table
     * @param ip - private ip
     * @param publicIP - public ip
     */
    public static void addEntry(String ip, String publicIP) {
        natTable.put(ip, publicIP);
    }

    /** removes clients from connected clients
     * @param clientIP - ip to be removed
     * @param internal - tag of client
     */
    public static synchronized void removeClient(String clientIP, boolean internal) {
        push(clientIP);// case disconnecte while connected
        clients.remove(clientIP);
    }

    /** add new client to connected clients
     * @param clientIP
     * @param thread
     */
    public static synchronized void addClient(String clientIP, natBoxThread thread) {
        clients.put(clientIP, thread);
    }

    /** sends paquet
     * @param p - paquet
     * @return - success on sent
     * @throws IOException
     */
    public static boolean send(Paquet p) throws IOException {
        String ip = p.getDestIP();
        natBoxThread link = clients.get(ip);
        if (link != null) {
            boolean connected = link.isConnected();
            if (connected) {
                return link.sendPaquet(p);
            }
        }
        return false;
    }

    /** translate public ip to private ip
     * @param publicIp - public ip
     * @return
     */
    public static String getPrivateIP(String publicIp) {
        for (Entry<String, String> entry : natTable.entrySet()) {

            // if give value is equal to value from entry
            // print the corresponding key
            if (entry.getValue().equals(publicIp)) {
                return entry.getKey();

            }
        }
        return null;

    }

    /** translate private to public
     * @param privateIp - private ip
     * @return
     */
    public static String getPublicIp(String privateIp) {
        return natTable.get(privateIp);

    }

}

class ntRefresh extends Thread {
    static HashMap<String, natBoxThread> clients = null;
    int timeout;

    public ntRefresh(HashMap<String, natBoxThread> clients, int timeout) {
        this.clients = clients;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(timeout);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            ;
            for (Entry<String, natBoxThread> entry : clients.entrySet()) {
                if (Duration.between(entry.getValue().getLastUsed(), LocalTime.now()).toSeconds() > timeout) {
                    try {
                        entry.getValue().disconnect();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}