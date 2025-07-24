package model;

//final
public class serverKey {

    public String getOpenKey() {
        return openKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    private String openKey;
    private String privateKey;

    public serverKey(String openKey, String privateKey) {
        this.openKey = openKey;
        this.privateKey = privateKey;
    }

}