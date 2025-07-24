package controller;

import service.AuthService;
import service.TestAuthService;
import util.ParsingModule;

public class ParsingController {

    // 파싱된 데이터를 담는 구조체
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
        ParsingController.DataStruct data = ParsingModule.extractData(input);
        String senderId = ParsingModule.extractSenderUserId(input);

        MainController controller = new MainController();

        switch (opcode) {
            case "FetchTable":
                return controller.fetchTableData("item"); // or dynamic from input

            case "Login":
                return controller.login("Tokken Or ID+PASSWORD");

            case "CreateToken":
                return controller.createToken(senderId); // userId로 처리

            case "GetPublicKey":
                return controller.getPublicKey();

            default:
                return "error%UnknownOpcode";
        }
    }
}
