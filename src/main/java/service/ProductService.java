package service;

import domain.Product;
import repository.ProductRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;


@Service
public class ProductService {

    private final TokenService tokenService;
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(TokenService tokenService, ProductRepository productRepository) {
        this.tokenService = tokenService;
        this.productRepository = productRepository;
    }
    @Transactional(readOnly = true)
    public String getProductDetails(Map<String, String> params, String token){
        System.out.println("getProductDetails() 호출됨 (DB연동 버전)");
        try{
            if(token == null || token.isBlank()){
                return "Error : Missing token";
            }
            Claims claims = tokenService.validateAndGetClaims(token);
            System.out.println("Token validation successful for user : " + claims.getSubject());
        } catch (IllegalArgumentException e){
            return "Error : " + e.getMessage();
        }
        // 2. 파라미터에서 상품 ID(productIndex) 추출
        String productIndexStr = params.get("productIndex");
        if (productIndexStr == null || productIndexStr.isBlank()) {
            return "Error:productIndex 데이터가 없습니다.";
        }
        // 3. DB에서 특정 ID의 상품 조회
        try {
            Long productId = Long.parseLong(productIndexStr);

            // JpaRepository의 findById 기능을 사용하여 상품을 찾음.
            // Optional<Product>는 상품이 있을 수도, 없을 수도 있다는 의미.
            return productRepository.findById(productId)
                    .map(product -> {
                        // 상품을 찾았을 경우, 이름과 이미지 URL을 조합하여 반환
                        System.out.println("상품 조회 성공: ID " + productId);
                        String responseData = String.join(",", product.getName(), product.getImageUrl());
                        return "OK:" + responseData;
                    })
                    .orElseGet(() -> {
                        // 상품을 찾지 못했을 경우, 에러 메시지 반환
                        System.out.println("상품 조회 실패: ID " + productId + " 없음");
                        return "Error:해당 ID의 상품을 찾을 수 없습니다: " + productId;
                    });

        } catch (NumberFormatException e) {
            return "Error:productIndex는 숫자여야 합니다.";
        }
    }
        @Transactional(readOnly = true) // 데이터를 읽기만 하는 메소드이므로 readOnly=true로 성능 최적화
    public String searchProducts(Map<String, String> params, String token) {
        System.out.println("SearchProducts() 호출 됨 (DB 연동 버전)");
        System.out.println("params : " + params);

        // 1. 토큰 검증 로직 (기존과 동일)
        try {
            if (token == null || token.isBlank()) {
                return "Error:Missing token";
            }
            Claims claims = tokenService.validateAndGetClaims(token);
            System.out.println("Token validation successful for user: " + claims.getSubject());
        } catch (IllegalArgumentException e) {
            return "Error:" + e.getMessage();
        }

        // 2. 파라미터 파싱 및 페이지 정보 설정
        String keyword = params.get("name");
        String pageStr = params.get("page");
        int page = (pageStr != null) ? Integer.parseInt(pageStr) - 1 : 0; // 페이지는 0부터 시작
        int pageSize = 8; // 한 페이지에 보여줄 상품 개수
        Pageable pageable = PageRequest.of(page, pageSize);

        // 3. DB에서 데이터 조회
        List<Product> productList;
        if (keyword == null || keyword.isBlank()) {
            // 검색어가 없으면 최신 상품 8개 조회
            System.out.println("최신 상품 목록 " + (page + 1) + "페이지를 조회합니다.");
            productList = productRepository.findAllByOrderByIdDesc(pageable);
        } else {
            // 검색어가 있으면 키워드 포함 상품 8개 조회
            System.out.println("'" + keyword + "' 키워드로 " + (page + 1) + "페이지를 검색합니다.");
            productList = productRepository.findByNameContainingOrderByIdDesc(keyword, pageable);
        }

        // 4. 조회된 데이터를 클라이언트가 원하는 형식의 문자열로 변환
        if (productList.isEmpty()) {
            System.out.println("검색 결과가 없습니다.");
            return "OK:"; // 데이터가 없을 경우 "OK:"만 반환
        }

        String responseData = productList.stream()
                .map(p -> String.join(",",
                        String.valueOf(p.getId()),
                        p.getName(),
                        p.getImageUrl(),
                        String.valueOf(p.getPrice())))
                .collect(Collectors.joining("$"));

        return "OK:" + responseData;
    }

    public String saveData(Map<String, String> params, String token) {
        // TODO: 실제 저장 로직 구현
        return "saveResult%success";
    }
}
