
public class Paquet {
    private String sIP;

    private Type type;
    private String dIP;
    private byte[] payload;

    public byte[] getPayload() {
        return payload;
    }

    public Paquet(String sIP, String dIP, Type type, String data) {
        this.sIP = sIP;
        this.dIP = dIP;
        this.type = type;
        payload = data.getBytes();
    }

    public void changeSource(byte[] sIP) {
        this.sIP = sIP.toString();
    }

    public Type getType() {
        return type;
    }

    public String getsIP() {
        return sIP;
    }

    public String getdIP() {
        return dIP;
    }

}
