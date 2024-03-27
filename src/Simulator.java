//Your simulator must minimally support the following protocols and paquets:
//        1. ICMP paquet forwarding (error and echo/response paquets).
//        2. TU (TCP and UDP) paquet forwarding.
//        3. A minimal DHCP implementation.

import java.io.IOException;

public class Simulator {
NatBox nBox = null;
    public static void main(String[] args) {
        try {// starts server locally
            new NatBox(1234);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            
        }
    }
}