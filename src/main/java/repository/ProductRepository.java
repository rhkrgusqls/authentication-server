package repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<domain.Product, Long> {

    // 키워드(name)가 포함된 상품을 최신순(ID 역순)으로 검색하는 메소드
    // Spring Data JPA가 메소드 이름을 분석해서 자동으로 쿼리를 생성
    List<domain.Product> findByNameContainingOrderByIdDesc(String keyword, Pageable pageable);

    // 모든 상품을 최신순(ID 역순)으로 검색하는 메소드
    List<domain.Product> findAllByOrderByIdDesc(Pageable pageable);
}
