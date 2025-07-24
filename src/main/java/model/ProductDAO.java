package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ProductDAO {
    private DBModule dbModule = new DBModule();

    // 상품 추가 (DTO를 받아 DB에 저장)
    public void addProduct(ProductDTO productDto) {
        String sql = "INSERT INTO products (productName, productStock) VALUES (?, ?)";
        try (Connection conn = dbModule.getConnection("productdb"); // "productdb" 스키마 사용
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productDto.getProductName());
            pstmt.setInt(2, productDto.getProductStock());
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 모든 상품 조회 (DB 데이터를 product 객체 리스트로 반환)
    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = dbModule.getConnection("productdb");
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Product p = new Product();
                p.setProductID(rs.getInt("productID"));
                p.setProductName(rs.getString("productName"));
                p.setProductStock(rs.getInt("productStock"));
                productList.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return productList;
    }

    public Product getProductById(int productId) {
        String sql = "SELECT * FROM products WHERE productID = ?";
        try (Connection conn = dbModule.getConnection("productdb");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId); // 파라미터로 받은 ID를 쿼리에 설정

            try (ResultSet rs = pstmt.executeQuery()) {
                // 결과가 있다면 Product 객체를 만들어 정보 저장
                if (rs.next()) {
                    Product product = new Product();
                    product.setProductID(rs.getInt("productID"));
                    product.setProductName(rs.getString("productName"));
                    product.setProductStock(rs.getInt("productStock"));
                    return product; // 정보가 담긴 객체 반환
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 상품을 찾지 못했거나 오류가 발생한 경우 null 반환
        return null;
    }

}
