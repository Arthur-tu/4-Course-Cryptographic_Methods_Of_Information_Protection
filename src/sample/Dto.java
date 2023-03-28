package sample;

public class Dto {
    private int ulen;
    private int blob_len;
    private String uname;
    private  byte[] openkey;
    private byte[] sign;

    public Dto(int ulen, int blob_len, String uname, byte[] openkey, byte[] sign) {
        this.ulen = ulen;
        this.blob_len = blob_len;
        this.uname = uname;
        this.openkey = openkey;
        this.sign = sign;
    }

    public int getUlen() {
        return ulen;
    }

    public int getBlob_len() {
        return blob_len;
    }

    public String getUname() {
        return uname;
    }

    public byte[] getOpenkey() {
        return openkey;
    }

    public byte[] getSign() {
        return sign;
    }
}
