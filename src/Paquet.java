
public class Paquet {
    // Vars
    Type type;
    String sourceIP;
    String DestIP;
    String Header;
    String Payload;

    public Paquet(Type type, String sourceIP, String DestIP, String Header,
            String Payload, boolean Data) {
        this.type = type;
        this.sourceIP = sourceIP;
        this.DestIP = DestIP;
        this.Payload = Payload;

    }

    public Type getType() {
        // TODO Auto-generated method stub
        return type;
        // throw new UnsupportedOperationException("Unimplemented method 'getType'");
    }

    public String getSourceIP() {
        return sourceIP;
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method
        // 'getSourceIP'");
    }

    public String getDestIP() {
        return DestIP;
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'getDestIP'");
    }

    public String getHeader() {
        return Header;
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method
        // 'getReceiver'");
    }

    public String getPayload() {
        // 576 bytes
        // variable length
        // TODO Auto-generated method stub
        return Payload;
        // throw new UnsupportedOperationException("Unimplemented method
        // 'getReceiver'");
    }

}
