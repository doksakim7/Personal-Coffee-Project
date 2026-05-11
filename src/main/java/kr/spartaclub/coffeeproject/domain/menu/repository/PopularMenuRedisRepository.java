package kr.spartaclub.coffeeproject.domain.menu.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class PopularMenuRedisRepository {

    private static final String POPULAR_MENUS_KEY_PREFIX = "popular:menus:";

    private final StringRedisTemplate stringRedisTemplate;

    // 날짜별 key를 생성한다.
    private String dailyKey(LocalDate date) {
        return POPULAR_MENUS_KEY_PREFIX + date;
    }

    // 특정 날짜의 메뉴 주문 수를 quantity만큼 증가시킨다.
    public void incrementScore(LocalDate date, Long menuId, int quantity) {
        stringRedisTemplate.opsForZSet()
                .incrementScore(dailyKey(date), String.valueOf(menuId), quantity);
    }

    // 특정 날짜의 메뉴 주문 수를 quantity만큼 감소시킨다.
    public void decrementScore(LocalDate date, Long menuId, int quantity) {
        stringRedisTemplate.opsForZSet()
                .incrementScore(dailyKey(date), String.valueOf(menuId), -quantity);
    }

    // 최근 7일간의 일별 ZSet을 합산해 상위 인기 메뉴를 조회한다.
    public Map<Long, Long> getTopMenusLast7Days(int limit) {
        LocalDate today = LocalDate.now();
        String tempKey = "popular:menus:tmp:" + UUID.randomUUID();

        try {
            List<String> keys = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                keys.add(dailyKey(today.minusDays(i)));
            }

            stringRedisTemplate.opsForZSet()
                    .unionAndStore(keys.get(0), keys.subList(1, keys.size()), tempKey);

            Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
                    .reverseRangeWithScores(tempKey, 0, limit - 1);

            if (tuples == null || tuples.isEmpty()) {
                return Collections.emptyMap();
            }

            Map<Long, Long> result = new LinkedHashMap<>();
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                if (tuple.getValue() == null || tuple.getScore() == null) {
                    continue;
                }
                result.put(Long.valueOf(tuple.getValue()), tuple.getScore().longValue());
            }

            return result;
        } finally {
            stringRedisTemplate.delete(tempKey);
        }
    }

}
