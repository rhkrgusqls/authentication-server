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
        public String[] refreshToken;  // ← 새로 추가
    }


    public static String controllerHandle(String input) {
        String opcode = ParsingModule.extractOpcode(input);
        System.out.println("[DEBUG] 추출된 opcode: '" + opcode + "'");
        DataStruct data = ParsingModule.extractData(input);
        String senderId = ParsingModule.extractSenderUserId(input);

        controller.MainController controller = new controller.MainController();

        switch (opcode) {
            case "LOGIN":
                if (data.refreshToken != null && data.refreshToken.length > 0) {
                    return controller.login(data.refreshToken[0]);
                } else {
                    return controller.login(data.id[0], data.password[0]);
                }
            case "SIGNUP":
                return controller.signup(input);
            case "REQUESTKEY":
                return controller.getAccessTokenPublicKey();
            default:
                return "error%UnknownOpcode";
        }
    }
}
