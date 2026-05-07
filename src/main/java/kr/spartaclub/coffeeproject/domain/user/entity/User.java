package kr.spartaclub.coffeeproject.domain.user.entity;

import jakarta.persistence.*;
import kr.spartaclub.coffeeproject.common.entity.SoftDeleteEntity;
import kr.spartaclub.coffeeproject.common.exception.CustomException;
import kr.spartaclub.coffeeproject.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이메일 (로그인 ID)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 암호화된 비밀번호
    @Column(nullable = false)
    private String password;

    // 현재 포인트 잔액 (기준 값)
    @Column(nullable = false)
    private Long point;

    // ==========================
    // 생성자
    // ==========================

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.point = 0L;
    }

    // ==========================
    // 비즈니스 메서드
    // ==========================

    // 포인트 증가
    public void addPoint(Long amount) {
        if (amount == null || amount <= 0) {
            throw new CustomException(ErrorCode.POINT_INVALID_ADD_AMOUNT);
        }
        this.point += amount;
    }

    // 포인트 차감
    public void subtractPoint(Long amount) {
        if (amount == null || amount <= 0) {
            throw new CustomException(ErrorCode.POINT_INVALID_SUBTRACT_AMOUNT);
        }
        if (this.point < amount) {
            throw new CustomException(ErrorCode.INSUFFICIENT_POINT);
        }
        this.point -= amount;
    }

    // 내 정보 수정(지금은 비밀번호만 가능하고 필요시 추가 구현 예정)
    public void updatePassword(String password) {
        this.password = password;
    }

}
