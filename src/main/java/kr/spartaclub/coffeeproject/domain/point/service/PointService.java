package kr.spartaclub.coffeeproject.domain.point.service;

import kr.spartaclub.coffeeproject.common.enums.PointType;
import kr.spartaclub.coffeeproject.common.exception.CustomException;
import kr.spartaclub.coffeeproject.common.exception.ErrorCode;
import kr.spartaclub.coffeeproject.common.security.AuthUser;
import kr.spartaclub.coffeeproject.domain.point.dto.request.PointAmountRequest;
import kr.spartaclub.coffeeproject.domain.point.dto.response.PointHistoryListResponse;
import kr.spartaclub.coffeeproject.domain.point.dto.response.PointResponse;
import kr.spartaclub.coffeeproject.domain.point.entity.PointHistory;
import kr.spartaclub.coffeeproject.domain.point.repository.PointHistoryRepository;
import kr.spartaclub.coffeeproject.domain.user.entity.User;
import kr.spartaclub.coffeeproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PointService {

    private static final long MIN_CHARGE_AMOUNT = 10_000L;
    private static final long MIN_EXCHANGE_AMOUNT = 5_000L;
    private static final long UNIT_AMOUNT = 5_000L;
    private static final long MAX_POINT = 2_000_000L;

    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    // 포인트를 충전하고 현재 보유 포인트를 반환한다.
    @Transactional
    public PointResponse chargePoint(AuthUser authUser, PointAmountRequest request) {
        User user = getUser(authUser);
        Long amount = request.getAmount();

        validateChargeAmount(amount);

        if (user.getPoint() + amount > MAX_POINT) {
            throw new CustomException(ErrorCode.POINT_LIMIT_EXCEEDED);
        }

        user.addPoint(amount);

        pointHistoryRepository.save(
                new PointHistory(
                        user,
                        null,
                        amount,
                        user.getPoint(),
                        PointType.CHARGE
                )
        );

        return new PointResponse(user.getPoint());
    }

    // 포인트를 환전하고 현재 보유 포인트를 반환한다.
    @Transactional
    public PointResponse exchangePoint(AuthUser authUser, PointAmountRequest request) {
        User user = getUser(authUser);
        Long amount = request.getAmount();

        validateExchangeAmount(amount);

        user.subtractPoint(amount);

        pointHistoryRepository.save(
                new PointHistory(
                        user,
                        null,
                        -amount,
                        user.getPoint(),
                        PointType.EXCHANGE
                )
        );

        return new PointResponse(user.getPoint());
    }

    // 현재 보유 포인트를 조회한다.
    @Transactional(readOnly = true)
    public PointResponse getPoint(AuthUser authUser) {
        User user = getUser(authUser);
        return new PointResponse(user.getPoint());
    }

    // 포인트 내역을 최신순으로 조회한다.
    @Transactional(readOnly = true)
    public PointHistoryListResponse getPointHistories(AuthUser authUser, Pageable pageable) {
        User user = getUser(authUser);

        Page<PointHistory> pageResult = pointHistoryRepository.findAllByUserOrderByCreatedAtDesc(user, pageable);

        List<PointHistoryListResponse.PointHistoryItem> content = pageResult.getContent().stream()
                .map(history -> new PointHistoryListResponse.PointHistoryItem(
                        history.getId(),
                        history.getType().name(),
                        history.getAmount(),
                        history.getBalance(),
                        history.getCreatedAt()
                ))
                .toList();

        return new PointHistoryListResponse(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    private User getUser(AuthUser authUser) {
        return userRepository.findById(authUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateChargeAmount(Long amount) {
        if (amount == null || amount < MIN_CHARGE_AMOUNT || amount % UNIT_AMOUNT != 0) {
            throw new CustomException(ErrorCode.POINT_INVALID_CHARGE_POLICY);
        }
    }

    private void validateExchangeAmount(Long amount) {
        if (amount == null || amount < MIN_EXCHANGE_AMOUNT || amount % UNIT_AMOUNT != 0) {
            throw new CustomException(ErrorCode.POINT_INVALID_EXCHANGE_POLICY);
        }
    }

}
