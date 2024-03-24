//1. Either be designated as an internal or external client, as discussed above.
// 2. IP addresses should reflect clients’ status as either internal or external clients.
// 3. Internal clients should use a minimal DHCP implementation to request an IP address.
// 4. Every client must have a unique (simulated) MAC address.
// 5. No GUI is required, only CLI (command-line interface) is necessary.
// 6. The client must provide a reply for every paquet received (This is not the case with ICMP error
//paquets). This may take the form of an ACK or a similar type of packet.