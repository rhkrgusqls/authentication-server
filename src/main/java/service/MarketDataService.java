package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MarketDataService {
    private final service.TokenService tokenService;

    @Autowired
    public MarketDataService(service.TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public String getAccessToken(Map<String, String> params) {
        System.out.println("Executing getAccessToken with params: " + params);
        String userId = params.get("userId");
        if(userId == null){
            return "Error:'userId' data is missing";
        }

        // TODO: 이 부분에서 DB를 조회하여 userId가 실제로 존재하는 사용자인지 확인하는 로직이 필요합니다.
        // 사용자 존재 확인 후 토큰 발급
        String accessToken = tokenService.generateAccessToken(userId);
        System.out.println("Generated new access token for user " + userId);
        return "OK:" + accessToken; // 성공 시 "OK:" 접두사와 함께 토큰 반환
    }
}
