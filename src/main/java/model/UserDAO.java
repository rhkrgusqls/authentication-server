package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

public class UserDAO {

    private DBModule dbModule = new DBModule();

    /**
     * 회원가입: 사용자 정보를 받아 DB에 저장합니다.
     * @param userDto 회원가입에 필요한 정보가 담긴 DTO
     * @return 성공 시 true, 실패 시 false
     */
    public boolean addUser(UserDTO userDto) {
        String sql = "INSERT INTO user (userid, userPassword, userAddress) VALUES (?, ?, ?)";
        try (Connection conn = dbModule.getConnection("userdb");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userDto.getUserID());
            pstmt.setString(2, userDto.getUserPassword());
            pstmt.setString(3, userDto.getUserAddress());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 로그인: 아이디와 비밀번호를 확인합니다.
     * @param id 사용자 아이디
     * @param password 사용자 비밀번호
     * @return 로그인 성공 시 true, 실패 시 false
     */
    public boolean login(String id, String password) {
        String sql = "SELECT COUNT(*) FROM user WHERE userid = ? AND userPassword = ?";
        try (Connection conn = dbModule.getConnection("userdb");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 회원 정보 조회: 아이디로 특정 회원 정보를 가져옵니다.
     * @param id 조회할 사용자의 아이디
     * @return User 객체 (회원 정보), 없으면 null
     */
    public User getUserById(String id) {
        String sql = "SELECT * FROM user WHERE userid = ?";
        try (Connection conn = dbModule.getConnection("userdb");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(); // 기본 생성자로 객체 생성 후 setter로 값 설정
                    user.setUserIndex(rs.getInt("userIndex"));
                    user.setUserID(rs.getString("userID"));
                    user.setUserPassword(rs.getString("userPassword"));
                    user.setUserName(rs.getString("userName"));
                    user.setUserAutority(rs.getBoolean("userAutority"));
                    user.setUserAddress(rs.getString("userAddress"));
                    user.setCert(rs.getString("cert"));
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 회원 정보 수정: 주로 비밀번호나 주소를 변경할 때 사용합니다.
     * @param userDto 수정할 정보가 담긴 DTO (ID를 기준으로 정보 변경)
     * @return 성공 시 true, 실패 시 false
     */
    public boolean updateUser(UserDTO userDto) {
        String sql = "UPDATE user SET userPassword = ?, userAddress = ? WHERE userid = ?";
        try (Connection conn = dbModule.getConnection("userdb");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userDto.getUserPassword());
            pstmt.setString(2, userDto.getUserAddress());
            pstmt.setString(3, userDto.getUserID());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 회원 탈퇴: 아이디로 특정 회원 정보를 삭제합니다.
     * @param id 삭제할 사용자의 아이디
     * @return 성공 시 true, 실패 시 false
     */
    public boolean deleteUser(String id) {
        String sql = "DELETE FROM user WHERE userid = ?";
        try (Connection conn = dbModule.getConnection("userdb");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
