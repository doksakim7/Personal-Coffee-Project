package kr.spartaclub.coffeeproject.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    // RedissonClient를 스프링 빈으로 등록한다.
    // 이후 서비스 계층에서 이 빈을 주입받아 Redis 분산락을 획득하는 데 사용한다.
    @Bean
    public RedissonClient redissonClient(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port
    ) {
        // Redisson 전용 설정 객체를 생성한다.
        Config config = new Config();

        // 단일 Redis 서버(local Redis)에 연결하도록 설정한다.
        // redis://host:port 형식의 주소를 사용하며,
        // 현재 프로젝트에서는 로컬 또는 단일 인스턴스 Redis 환경을 기준으로 한다.
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port);

        // 설정 정보를 바탕으로 RedissonClient를 생성하여 반환한다.
        // 이 객체를 통해 lock 획득, 해제, Redis 자료구조 접근 등을 수행할 수 있다.
        return Redisson.create(config);
    }

}
