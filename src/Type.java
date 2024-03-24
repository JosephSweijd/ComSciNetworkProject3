
public enum Type {
    
    DHCP_REPLY,
    DESTINATION_UNREACHABLE,//packet could not be delivered
    ECHO_REPLY,//is active
    ECHO_REQUEST,//is activw
    REDIRECT,//send to router in the case it didnt manage
    DHCP_REQUEST,//
    DHCP_RELEASE,//NO LONGER NEEDS IP
    PARAMETER_PROBLEM,//invalid header field
    ARP_REPLY,
    ARP_REQUEST, ERROR
    
}
