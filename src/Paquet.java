import java.io.Serializable;

public class Paquet implements Serializable{
    private Type type;
    private String sourceIP;
    private String destIP;
    private String Payload;
    // ethernet
    private String sourceMac;
    private String destMac;

    public Paquet(String sourceIP,String destIP, Type type, String Payload) {
        this.type = type;
        this.sourceIP = sourceIP;
        this.destIP = destIP;
        this.Payload = Payload;
    }

    public Type getType() {
        return type;
    }

    public String getSourceIP() {
        return sourceIP;
     
    }

    public String getDestIP() {
        return destIP;
  
    }

    public String getPayload() {
        return Payload;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setSourceIP(String sourceIP) {
        this.sourceIP = sourceIP;
    }

    public void setDestIP(String destIP) {
        this.destIP = destIP;
    }

    public void setPayload(String payload) {
        Payload = payload;
    }

    public void setSourceMac(String sourceMac) {
        this.sourceMac = sourceMac;
    }

    public void setDestMac(String destMac) {
        this.destMac = destMac;
    }

    @Override
    public String toString() {
        return "Paquet [type=" + type + ", sourceIP=" + sourceIP + ", destIP=" + destIP + ", Payload=" + Payload
                + ", sourceMac=" + sourceMac + ", destMac=" + destMac + "]";
    }

    public String getSourceMac() {
        return sourceMac;
        
    }
    
}
