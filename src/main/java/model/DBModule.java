package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;
public class DBModule {

    private final String DATA_BASE_IP = "127.0.0.1";
    private final int DATA_BASE_PORT = 3306;
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "QWER1234!";

    public DBModule() {}

    public Connection getConnection(String schemaName) throws Exception {
        String url = "jdbc:mysql://" + DATA_BASE_IP + ":" + DATA_BASE_PORT + "/" + schemaName +
                "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul";
        return DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
    }
}