package kr.spartaclub.coffeeproject.common.lock;

import kr.spartaclub.coffeeproject.common.exception.CustomException;
import kr.spartaclub.coffeeproject.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class RedissonLockManager implements DistributedLockManager {

    private final RedissonClient redissonClient;

    @Override
    public <T> T executeWithLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> task) {
        // 전달받은 key를 기준으로 Redis 분산락 객체를 조회한다.
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;

        try {
            // waitTime 동안 락 획득을 시도하고,
            // 락 획득에 성공하면 leaseTime 이후 자동 해제되도록 설정한다.
            locked = lock.tryLock(waitTime, leaseTime, timeUnit);

            // 지정한 시간 내에 락을 얻지 못하면 동시 요청 과다로 판단하고 예외를 발생시킨다.
            if (!locked) {
                throw new CustomException(ErrorCode.LOCK_ACQUIRE_FAILED);
            }

            // 락 획득에 성공한 경우에만 실제 비즈니스 로직을 실행한다.
            return task.get();

        } catch (InterruptedException e) {
            // 대기 중 인터럽트가 발생하면 현재 스레드의 인터럽트 상태를 복구하고 예외로 변환한다.
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.LOCK_ACQUIRE_FAILED, e);
        } finally {
            // 현재 스레드가 락을 보유한 경우에만 안전하게 락을 해제한다.
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public void executeWithLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit, Runnable task) {
        // 반환값이 없는 작업도 Supplier 형태로 감싸서 동일한 락 처리 로직을 재사용한다.
        executeWithLock(key, waitTime, leaseTime, timeUnit, () -> {
            task.run();
            return null;
        });
    }

}
