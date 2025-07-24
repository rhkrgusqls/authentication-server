package controller;
import util.ParsingModule;
import controller.MainController;

import java.util.Map;

public class ParsingController {

    public static class DataStruct {
        public String[] id;
        public String[] password;
        public String[] name;
        public String[] profileDir;
        public String[] phoneNum;
        public String[] chatRoomNum;
        public String[] chatData;
    }

    public static String controllerHandle(String input) {
        String opcode = ParsingModule.extractOpcode(input);
        System.out.println("[DEBUG] 추출된 opcode: '" + opcode + "'");
        DataStruct data = ParsingModule.extractData(input);
        String senderId = ParsingModule.extractSenderUserId(input);

        // 실제 의존성 객체를 직접 생성 (임시)
        service.AuthService authService = new service.TestAuthService();
        service.TokenService tokenService = new service.TokenService();
        repository.ProductRepository productRepository = null; // 실제 구현체로 교체 필요
        controller.MainController controller = new controller.MainController(authService, tokenService, productRepository);

        switch (opcode) {
            case "GET_ACCESS_TOKEN":
                return controller.getAccessToken(data);

            case "PRODUCT_SEARCH":
                return controller.searchProducts(data, senderId);

            case "SearchData":
                return controller.getProductDetails(data, senderId);

            case "LOGIN":
                return controller.login(data);

            case "SIGNUP":
                return controller.signup(data);

            case "SAVE_DATA":
                return controller.saveData(data, senderId);

            case "GET_PUBLIC_KEY":
                return controller.getPublicKey();

            default:
                return "error%UnknownOpcode";
        }
    }
}
