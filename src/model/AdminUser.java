package model;

public class AdminUser {
    public String id, name, secretKey;
    
    public AdminUser(String id, String name, String secretKey) {
        this.id = id;
        this.name = name;
        this.secretKey = secretKey;
    }
}
