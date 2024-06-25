package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

  private final PointHistoryTable pointHistoryTable;
  private final UserPointTable userPointTable;

  @Override
  public UserPoint selectById(long id) {
    return userPointTable.selectById(id);
  }

  @Override
  public List<PointHistory> selectAllByUserId(long userId) {
    return pointHistoryTable.selectAllByUserId(userId);
  }

  @Override
  public UserPoint insertOrUpdate(long userId, long amount) {
    return userPointTable.insertOrUpdate(userId, amount);
  }

  @Override
  public void insert(long userId, long amount, TransactionType transactionType, long updateMillis) {
    pointHistoryTable.insert(userId, amount, transactionType, updateMillis);
  }
}
