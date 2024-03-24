
public enum Type {
    
    DHCP_REPLY,
    DESTINATION_UNREACHABLE,//packet could not be delivered
    ECHO_REPLY,//is active
    ECHO_REQUEST,//is activw
    REDIRECT,//send to router in the case it didnt manage
    DCHP_REQUEST,
    PARAMETER_PROBLEM,//invalid header field
    TIME_EXCEEDED,//time to live field hit 0 discard packet
    ARP_REPLY,
    ARP_REQUEST
    
}
