package kr.spartaclub.coffeeproject.domain.menu.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.spartaclub.coffeeproject.common.enums.MenuStatus;
import kr.spartaclub.coffeeproject.common.enums.MenuType;
import kr.spartaclub.coffeeproject.domain.menu.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static kr.spartaclub.coffeeproject.domain.menu.entity.QMenu.menu;

@RequiredArgsConstructor
public class MenuCustomRepositoryImpl implements MenuCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Menu> searchMenus(MenuType type, Pageable pageable) {

        // 동적 조건 구성을 위한 BooleanBuilder 생성
        BooleanBuilder builder = new BooleanBuilder();

        // 삭제되지 않았고, 조회 가능한 상태의 메뉴만 조회
        builder.and(menu.deletedAt.isNull());
        builder.and(menu.status.in(MenuStatus.AVAILABLE, MenuStatus.SOLD_OUT));

        // type 값이 있으면 해당 카테고리 조건 추가
        if (type != null) {
            builder.and(menu.type.eq(type));
        }

        // 조건에 맞는 메뉴 목록을 페이지 단위로 조회
        List<Menu> content = queryFactory
                .selectFrom(menu)
                .where(builder)
                .orderBy(menu.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 페이지 수 계산을 위한 총 개수 조회
        Long total = queryFactory
                .select(menu.count())
                .from(menu)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

}
