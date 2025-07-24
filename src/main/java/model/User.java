package model;


//final
public class User {

    private int userIndex;
    private String userID;
    private String userPassword;
    private String userName;
  

	private boolean userAutority;
    private String userAddress;
    private String cert;

    public int getUserIndex() {
        return userIndex;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public boolean getUserAutority() {
        return userAutority;
    }

    public String getCert() {
        return cert;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public String getUserName() {
  		return userName;
  	}

  	public void setUserName(String userName) {
  		this.userName = userName;
  	}
    
    public void setUserIndex(int userIndex) {
        this.userIndex = userIndex;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setUserAutority(boolean userAutority) {
        this.userAutority = userAutority;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public User(int userIndex, String userID, String userPassword,  boolean userAutority, String userAddress, String cert) {
        this.userIndex = userIndex;
        this.userID = userID;
        this.userPassword = userPassword;
        this.userAutority = userAutority;
        this.userAddress = userAddress;
        this.cert = cert;
    }
    
    public User(String userID, String userPassword, String userAddress) {
        this.userID = userID;
        this.userPassword = userPassword;
        this.userAddress = userAddress;
    }
    
}
