package kr.spartaclub.coffeeproject.domain.menu.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PopularMenuRedisRepository {

    private static final String POPULAR_MENUS_KEY = "popular:menus";

    private final StringRedisTemplate stringRedisTemplate;

    // menuId의 score를 quantity만큼 증가시킨다.
    public void incrementScore(Long menuId, int quantity) {
        stringRedisTemplate.opsForZSet()
                .incrementScore(POPULAR_MENUS_KEY, String.valueOf(menuId), quantity);
    }

    // menuId의 score를 quantity만큼 감소시킨다.
    public void decrementScore(Long menuId, int quantity) {
        stringRedisTemplate.opsForZSet()
                .incrementScore(POPULAR_MENUS_KEY, String.valueOf(menuId), -quantity);
    }

    // 상위 limit개의 인기 메뉴(menuId, score)를 score 내림차순으로 조회한다.
    public Map<Long, Long> getTopMenus(int limit) {
        Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(POPULAR_MENUS_KEY, 0, limit - 1);

        if (tuples == null || tuples.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Long> result = new LinkedHashMap<>();
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple.getValue() == null || tuple.getScore() == null) {
                continue;
            }

            Long menuId = Long.valueOf(tuple.getValue());
            Long orderCount = tuple.getScore().longValue();
            result.put(menuId, orderCount);
        }

        return result;
    }

}
