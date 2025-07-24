package domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity // 해당 클래스가 DB 테이블과 매핑되는 엔티티 클래스임을 나타냄
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // 'name' 컬럼은 null 값을 허용하지 않음.
    private String name;

    private String imageUrl;

    private int price;

    //TODO : 생성일, 수정일 등 필요한 컬럼을 추가할 것
}
