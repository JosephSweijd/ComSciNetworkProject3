
public enum Type {
    
    DHCP_REPLY,
    DESTINATION_UNREACHABLE,//packet could not be delivered
    ECHO_REPLY,//is active
    ECHO_REQUEST,//is activw
    DHCP_REQUEST,//
    DHCP_RELEASE,//NO LONGER NEEDS IP
    ARP_REPLY,
    ARP_REQUEST,
    ERROR,
    DISCONNECT,
    ACK,
    TCP
    
}
