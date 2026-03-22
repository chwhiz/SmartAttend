package model;

// simple model para sa admin user. kayo na bahala diyan
public class AdminUser {
    public String id, schoolId, name, secretKey;
    
    public AdminUser(String id, String schoolId, String name, String secretKey) {
        this.id = id;
        this.schoolId = schoolId;
        this.name = name;
        this.secretKey = secretKey;
    }
}
