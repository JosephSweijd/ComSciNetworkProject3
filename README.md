# Network Address Translation

## Group Members
- Joseph Sweijd - Client - 24366498
- Thina Mbangela - Natbox - 24930741

## Introduction
The project aims to implement a simulated router with NAT functionality, catering to the exchange of packets among internal and external clients. This document provides an overview of the project's features, file descriptions, program flow, issues encountered, design, and experiments conducted to validate the implementation.

## Features
1. **NAT Table Management:** The NatBox class maintains a NAT table using a HashMap, allowing for dynamic addition and removal of NAT entries.
2. **Packet Forwarding:** The `natBoxThread` class facilitates packet forwarding between internal and external clients based on NAT translations.
3. **NAT Entry Management:** It includes methods for assigning internal clients unique IP addresses, mapping private IP addresses to public IP addresses, and releasing IP addresses back into the pool upon client disconnection.
4. **DHCP Support:** Assigns internal clients unique IP addresses from the pool of public addresses and responds to DHCP requests and DHCP releases.
5. **Protocol Handling:** Handles types of packets, including DHCP requests, DHCP releases, ARP requests, and ICMP echo requests.
6. **Error Handling:** It appropriately handles errors such as unavailable public addresses and drops packets when necessary.

### Program Flow
1. **Client Initialization:**
   - Upon instantiation, clients are prompted to select their type (external or internal).
   - All clients connect to the Hamachi network.

2. **Internal Client Connection:**
   - Internal clients send DHCP requests upon connection.
   - NAT box assigns private IP addresses and, if available, maps them to public addresses.

3. **ARP Packet Handling:**
   - NAT box responds to ARP requests to provide its MAC address to clients.

4. **DHCP Packet Handling:**
   - DHCP requests and assignments are managed by the NAT box.
   - Unused public addresses are released back to the pool, and new assignments are made as necessary.

5. **Echo Request/Reply Handling:**
   - Clients communicate directly using ICMP echo requests and replies.
   - NAT box performs IP address translation when necessary for communication between internal and external clients.

## To compile
- `make nsim` to start a router
- `make i` to run an internal client
- `make e` to run an external client
