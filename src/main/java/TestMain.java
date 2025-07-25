import java.util.HashMap;
import java.util.Map;
import model.identityServerConnector;

public class TestMain {
    public static void main(String[] args) {
        // 객체 생성
        identityServerConnector connector = new identityServerConnector();

        // 테스트용 파라미터 맵 생성
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("clientId", "testClient");
        paramMap.put("clientSecret", "testSecret");

        try {
            // 토큰 요청
            Map<String, String> response = connector.requestTokensFromExternalServer(paramMap);

            // 응답 출력
            System.out.println("=== Response From Server ===");
            for (Map.Entry<String, String> entry : response.entrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }

        } catch (Exception e) {
            System.err.println("[Error] Failed to get token from external server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
