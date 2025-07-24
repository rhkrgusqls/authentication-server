package controller;

import model.JWTModel;
import service.AuthService;
import service.ProductService;
import service.TestAuthService;
import service.TokenService;
import service.MarketDataService;
import controller.ParsingController.DataStruct;

import java.util.Map;

public class MainController {

    private final AuthService authService;
    private final JWTModel jwtModel = new JWTModel();
    private final TokenService tokenService;
    private final ProductService productService;
    private final MarketDataService marketDataService;
    private final repository.ProductRepository productRepository;

    public MainController(AuthService authService, TokenService tokenService, repository.ProductRepository productRepository) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.productRepository = productRepository;
        this.productService = new ProductService(tokenService, productRepository);
        this.marketDataService = new MarketDataService(tokenService);
    }

    public String getAccessToken(DataStruct data) {
        return marketDataService.getAccessToken(dataStructToMap(data));
    }

    public String searchProducts(DataStruct data, String token) {
        return productService.searchProducts(dataStructToMap(data), token);
    }

    public String getProductDetails(DataStruct data, String token) {
        return productService.getProductDetails(dataStructToMap(data), token);
    }

    private Map<String, String> dataStructToMap(DataStruct data) {
        Map<String, String> map = new java.util.HashMap<>();
        if (data.id != null && data.id.length > 0) map.put("id", data.id[0]);
        if (data.password != null && data.password.length > 0) map.put("password", data.password[0]);
        // 필요시 name, phoneNum 등도 추가 가능
        return map;
    }

    public String login(DataStruct data) {
        Map<String, String> paramMap = dataStructToMap(data);
        System.out.println("[로그인 시도] 입력 데이터: " + paramMap);
        if (!paramMap.containsKey("id") || !paramMap.containsKey("password")) {
            System.out.println("[로그인 실패] id 또는 password 누락");
            return "loginResult%error%Missing id or password";
        }
        boolean result = authService.authenticate(paramMap);
        if (result) {
            System.out.println("[로그인 성공] id: " + paramMap.get("id"));
            return "loginResult%success";
        } else {
            System.out.println("[로그인 실패] 인증 실패, id: " + paramMap.get("id"));
            return "loginResult%error%Invalid credentials";
        }
    }

    public String saveData(DataStruct data, String token) {
        return productService.saveData(dataStructToMap(data), token);
    }

    public String getPublicKey() {
        return "publicKey%" + jwtModel.getPublicKeyString();
    }

    public String createToken(String userId) {
        String token = jwtModel.generateToken(userId);
        return "token%" + token;
    }

    public String fetchTableData(String tableName) {
        return "item%1%카테고리1%상품A|2%카테고리2%상품B";
    }

    public String signup(DataStruct data) {
        Map<String, String> paramMap = dataStructToMap(data);
        System.out.println("[회원가입 시도] 입력 데이터: " + paramMap);
        boolean result = authService.signup(paramMap);
        return "signupResult%" + (result ? "success" : "fail");
    }
}
