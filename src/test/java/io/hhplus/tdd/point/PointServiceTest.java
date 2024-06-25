package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PointServiceTest {

  @Mock
  private PointRepository pointRepository;

  @InjectMocks
  private PointService pointService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * 특정 유저의 포인트를 조회하는 기능 성공 케이스 테스트
   */
  @Test
  public void givenId_whenPoint_thenReturnCurrentUserPoint() {
    //given
    long id = -1L;
    long amount = 100L;

    //when
    UserPoint userPoint = new UserPoint(id, amount, System.currentTimeMillis());
    when(pointRepository.selectById(id)).thenReturn(userPoint);

    UserPoint thenUserPoint = pointService.point(id);

    //then
    assertEquals(thenUserPoint.id(), userPoint.id());
    assertEquals(thenUserPoint.point(), userPoint.point());
    assertEquals(thenUserPoint.updateMillis(), userPoint.updateMillis());

    verify(pointRepository).selectById(id);
  }

  /**
   * 특정 유저의 포인트 충전/이용 내역을 조회하는 기능 성공 테스트
   */
  @Test
  public void givenUserId_whenHistory_thenReturnCorrectHistory() {
    //given
    long userId = 1L;
    long id1 = 1L;
    long id2 = 2L;

    //when
    List<PointHistory> pointHistories = List.of(
        new PointHistory(id1, userId, 1000, TransactionType.CHARGE, System.currentTimeMillis()),
        new PointHistory(id2, userId, 1000, TransactionType.USE, System.currentTimeMillis())
    );

    when(pointRepository.selectAllByUserId(userId)).thenReturn(pointHistories);

    List<PointHistory> whenPointHistories = pointService.history(userId);

    //then
    assertEquals(whenPointHistories.size(), pointHistories.size());

    for (int i = 0; i < 2; i++) {
      assertEquals(whenPointHistories.get(i).id(), pointHistories.get(i).id());
      assertEquals(whenPointHistories.get(i).userId(), pointHistories.get(i).userId());
      assertEquals(whenPointHistories.get(i).amount(), pointHistories.get(i).amount());
      assertEquals(whenPointHistories.get(i).type(), pointHistories.get(i).type());
    }

    verify(pointRepository).selectAllByUserId(userId);
  }

  /**
   * 유저의 포인트를 충전시 음수를 입력하면 예외가 발생하는지 확인하는 테스트
   */
  @Test
  public void givenNegativeAmount_whenChargeById_thenThrowsException() {
    //given
    long id = 1L;
    long chargePoint = -1000L;

    //when
    String errorMessage = null;
    Exception exception = null;

    try {
      pointService.charge(id, chargePoint);
    } catch (IllegalArgumentException e) {
      exception = e;
      errorMessage = e.getMessage();
    }

    //then
    assertThat(exception).isNotNull();
    assertThat(errorMessage).isNotNull();
    assertEquals(errorMessage, "충전금액은 0보다 커야합니다.");
  }

  /**
   * 유저의 포인트 충전시 기존에 있던 금액에서 합산이 되었는지 확인하는 테스트
   */
  @Test
  public void giveIdAndChargePoint_whenCharge_thenSumUserPoint() {
    //given
    long id = 1L;
    long pointBeforeCharge = 1000L;
    long chargePoint = 300L;
    long pointAfterCharge = pointBeforeCharge + chargePoint;

    //when
    UserPoint userPointBeforeCharge = new UserPoint(id, pointBeforeCharge,
        System.currentTimeMillis());
    when(pointRepository.selectById(id)).thenReturn(userPointBeforeCharge);

    UserPoint userPointAfterCharge = new UserPoint(id, pointAfterCharge,
        System.currentTimeMillis());
    when(pointRepository.insertOrUpdate(id, pointAfterCharge)).thenReturn(userPointAfterCharge);

    UserPoint thenUserPoint = pointService.charge(id, chargePoint);

    //then
    assertEquals(thenUserPoint.point(), userPointAfterCharge.point());
    assertEquals(thenUserPoint.id(), userPointAfterCharge.id());

    verify(pointRepository).selectById(id);
    verify(pointRepository).insertOrUpdate(id, pointAfterCharge);
  }

  /**
   * 유저의 포인트 사용시 기존에 가지고 있던 포인트보다 많이 사용하면 예외가 발생하는지 확인하는 테스트
   */
  @Test
  public void givenId_whenUse_thenThrowsException() {
    //given
    long id = 1L;
    long pointBeforeUse = 1000L;
    long usePoint = 1200L;

    //when
    UserPoint userPointBeforeCharge = new UserPoint(id, pointBeforeUse, System.currentTimeMillis());
    when(pointRepository.selectById(id)).thenReturn(userPointBeforeCharge);

    String errorMessage = null;
    Exception exception = null;

    try {
      pointService.use(id, usePoint);
    } catch (IllegalArgumentException e) {
      exception = e;
      errorMessage = e.getMessage();
    }

    //then
    assertThat(exception).isNotNull();
    assertThat(errorMessage).isNotNull();
    assertEquals(errorMessage, "보유포인트 보다 사용포인트가 큽니다.");
  }

  /**
   * 유저의 포인트 사용시 기존에 있던 금액에서 차감이 되었는지 확인하는 테스트
   */
  @Test
  public void givenIdAndUsePoint_whenUse_thenDifferenceUserPoint() {
    //given
    long id = 1L;
    long pointBeforeUse = 1000L;
    long usePoint = 300L;
    long pointAfterUse = pointBeforeUse - usePoint;

    //when
    UserPoint userPointBeforeUse = new UserPoint(id, pointBeforeUse, System.currentTimeMillis());
    when(pointRepository.selectById(id)).thenReturn(userPointBeforeUse);

    UserPoint userPointAfterUse = new UserPoint(id, pointAfterUse, System.currentTimeMillis());
    when(pointRepository.insertOrUpdate(id, pointAfterUse)).thenReturn(userPointAfterUse);

    UserPoint thenUserPoint = pointService.use(id, usePoint);

    //then
    assertEquals(thenUserPoint.id(), id);
    assertEquals(thenUserPoint.point(), pointAfterUse);

    verify(pointRepository).selectById(id);
    verify(pointRepository).insertOrUpdate(id, pointAfterUse);
  }
}
