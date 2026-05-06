package kr.spartaclub.coffeeproject.domain.menu.entity;

import jakarta.persistence.*;
import kr.spartaclub.coffeeproject.common.entity.SoftDeleteEntity;
import kr.spartaclub.coffeeproject.common.enums.MenuStatus;
import kr.spartaclub.coffeeproject.common.enums.MenuType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "menus")
public class Menu extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 메뉴명
    @Column(nullable = false, length = 100)
    private String name;

    // 메뉴 가격
    @Column(nullable = false)
    private Long price;

    // 메뉴 카테고리
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MenuType type;

    // 판매 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MenuStatus status;

    // 이미지 URL
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // ==========================
    // 생성자
    // ==========================

    public Menu(String name, Long price, MenuType type, MenuStatus status, String imageUrl) {
        this.name = name;
        this.price = price;
        this.type = type;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    // ==========================
    // 비즈니스 메서드
    // ==========================

    // 메뉴 정보 수정
    public void update(String name, Long price, MenuType type, String imageUrl) {
        this.name = name;
        this.price = price;
        this.type = type;
        this.imageUrl = imageUrl;
    }

    // 판매 상태 변경
    public void changeStatus(MenuStatus status) {
        this.status = status;
    }

    // 주문 가능 여부 판단
    public boolean isOrderable() {
        return this.status == MenuStatus.AVAILABLE;
    }

}
