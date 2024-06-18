package io.hhplus.tdd.point;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

  private final PointRepository pointRepository;

  public UserPoint point(long id) {
    UserPoint userPoint = pointRepository.selectById(id);
    return userPoint;
  }

  public List<PointHistory> history(long userId) {
    List<PointHistory> pointHistories = pointRepository.selectAllByUserId(userId);
    return pointHistories;
  }

  public UserPoint charge(long id, long amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("충전금액은 0보다 커야합니다.");
    }

    UserPoint getPoint = pointRepository.selectById(id);

    UserPoint userPoint = pointRepository.insertOrUpdate(getPoint.id(), getPoint.point() + amount);
    pointRepository.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
    return userPoint;
  }

  public UserPoint use(long userId, long amount) {
    UserPoint getPoint = pointRepository.selectById(userId);

    long totalPoint = getPoint.point() - amount;
    if (totalPoint < 0) {
      throw new IllegalArgumentException("보유포인트 보다 사용포인트가 큽니다.");
    }

    UserPoint userPoint = pointRepository.insertOrUpdate(getPoint.id(), totalPoint);
    pointRepository.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());
    return userPoint;
  }
}
