
public class Paquet {

    // Vars
    Type type;
    String sourceIP;
    String DestIP;
    String Content;
    String Receiver;
    String Header;
    String Payload;
    Boolean Data;

    public Paquet(Type type, String sourceIP, String DestIP, String Content, String Receiver, String Header,
            String Payload, boolean Data) {
                this.type = type;
                this.sourceIP = sourceIP;
                this.DestIP = DestIP;
                this.Content = Content;
                this.Receiver = Receiver;
                this.Payload = Payload;
                this.Data = Data;
    }

    public Type getType() {
        // TODO Auto-generated method stub
        return type;
        //throw new UnsupportedOperationException("Unimplemented method 'getType'");
    }

    public String getSourceIP() {
        return sourceIP;
        // TODO Auto-generated method stub
       // throw new UnsupportedOperationException("Unimplemented method 'getSourceIP'");
    }

    public String getDestIP() {
        return DestIP;
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'getDestIP'");
    }

    public String getContent() {
        return Content;
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'getContent'");
    }

    public String getReceiver() {
        return Receiver;
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'getReceiver'");
    }

    public String getHeader() {
        return Header;
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'getReceiver'");
    }

    public String getPayload() {
        // 576 bytes
        // variable length
        // TODO Auto-generated method stub
        return Payload;
        //throw new UnsupportedOperationException("Unimplemented method 'getReceiver'");
    }

    public boolean getData() {
        return Data;
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'getData'");
    }

}
