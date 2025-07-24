package controller;

import model.JWTModel;
import service.AuthService;
import service.ProductService;
import service.TestAuthService;
import service.TokenService;
import service.MarketDataService;
import controller.ParsingController.DataStruct;

public class MainController {

    private final AuthService authService = new TestAuthService();
    private final JWTModel jwtModel = new JWTModel();
    private final TokenService tokenService = new TokenService();
    private final ProductService productService = new ProductService();
    private final MarketDataService marketDataService = new MarketDataService();

    public String getAccessToken(DataStruct data) {
        // 임시 응답 처리 (예시)
        return marketDataService.getAccessToken(data);
    }

    public String searchProducts(DataStruct data, String token) {
        return productService.searchProducts(data, token);
    }

    public String getProductDetails(DataStruct data, String token) {
        return productService.getProductDetails(data, token);
    }

    public String login(DataStruct data) {
        boolean result = authService.authenticate(data);
        return "loginResult%" + (result ? "success" : "fail");
    }

    public String saveData(DataStruct data, String token) {
        return productService.saveData(data, token);
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
}
