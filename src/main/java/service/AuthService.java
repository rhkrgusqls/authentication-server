package service;

public interface AuthService {
    boolean authenticate(String input);

    default boolean authenticate(java.util.Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> sb.append(k).append("%").append(v).append("%"));
        return authenticate(sb.toString());
    }
} 