package controller;

import util.ParsingModule;

import java.util.Map;

public class ParsingController {

    // 파싱 유틸에서 뽑아온 데이터 구조
    public static class DataStruct {
        // TODO: 데이터세트 지정
        public String[] id;
        public String[] password;
        public String[] name;
        public String[] profileDir;
        public String[] phoneNum;
        public String[] chatRoomNum;
        public String[] chatData;
    }

    // controllerHandle 메서드: opcode별 분기하되 내용은 비워둠
    public static String controllerHandle(String input) {
        String opcode = ParsingModule.extractOpcode(input);
        DataStruct data = ParsingModule.extractData(input);
        String senderId = ParsingModule.extractSenderUserId(input);

        switch (opcode) {
            case "Login":
                // TODO: 로그인 처리 로직 작성 예정
                break;
            default:
                // TODO: 알 수 없는 opcode 처리
                break;
        }
        return null; // 실행 내용 없으므로 기본 null 반환
    }
}
