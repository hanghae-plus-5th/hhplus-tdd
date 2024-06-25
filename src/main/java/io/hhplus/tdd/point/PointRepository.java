package io.hhplus.tdd.point;

import java.util.List;

public interface PointRepository {

  UserPoint selectById(long id);

  List<PointHistory> selectAllByUserId(long userId);

  UserPoint insertOrUpdate(long userId, long l);

  void insert(long userId, long amount, TransactionType transactionType, long l);

}
