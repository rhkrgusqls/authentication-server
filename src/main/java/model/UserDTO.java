package model;


public class UserDTO {
    private String userID;
    private String userPassword;
    private String userAddress;

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public UserDTO(String userID, String userPassword, String userAddress) {
        this.userID = userID;
        this.userPassword = userPassword;
        this.userAddress = userAddress;
    }
}