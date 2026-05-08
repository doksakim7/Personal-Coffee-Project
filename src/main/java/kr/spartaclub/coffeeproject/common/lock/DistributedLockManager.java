package kr.spartaclub.coffeeproject.common.lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface DistributedLockManager {

    // 분산락을 획득한 뒤 값을 반환하는 작업을 실행한다.
    // key       : 락을 구분하기 위한 고유 키 (예: userId 기준 락)
    // waitTime  : 락을 얻기 위해 최대 얼마 동안 대기할지
    // leaseTime : 락을 획득한 뒤 자동으로 해제되기까지의 시간
    // timeUnit  : waitTime, leaseTime에 사용할 시간 단위
    // task      : 락 획득 성공 후 실행할 실제 비즈니스 로직
    <T> T executeWithLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> task);

    // 분산락을 획득한 뒤 반환값이 없는 작업을 실행한다.
    // 내부적으로는 Runnable 작업을 실행하고, 반환값은 없는 형태의 락 처리에 사용한다.
    void executeWithLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit, Runnable task);

}
