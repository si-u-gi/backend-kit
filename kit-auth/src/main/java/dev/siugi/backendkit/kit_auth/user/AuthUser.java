package dev.siugi.backendkit.kit_auth.user;

public record AuthUser(
        String id,
        String email,
        String passwordHash
) {
}

//public record를 사용하면 클래스를 간결하게 만들 수 있음,
//public class AuthUser {
//
//    private final String id;
//    private final String email;
//    private final String passwordHash;
//
//    public AuthUser(
//            String id,
//            String email,
//            String passwordHash
//    ) {
//        this.id = id;
//        this.email = email;
//        this.passwordHash = passwordHash;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public String getPasswordHash() {
//        return passwordHash;
//    }
//}