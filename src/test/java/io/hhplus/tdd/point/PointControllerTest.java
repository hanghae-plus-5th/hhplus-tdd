package io.hhplus.tdd.point;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(PointController.class)
public class PointControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  PointService pointService;

  /**
   * 특정 유저의 포인트를 조회 테스트
   */
  @Test
  public void givenId_whenPoint_thenStatusOkAndExistBody() throws Exception {
    //given
    long id = 1L;

    //when
    UserPoint userPoint = new UserPoint(id, 0, System.currentTimeMillis());
    when(pointService.point(id)).thenReturn(userPoint);

    //then
    mockMvc.perform(get("/point/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.point").exists())
        .andExpect(jsonPath("$.updateMillis").exists())
        .andDo(print());

    verify(pointService).point(id);
  }

  /**
   * 특정 유저의 포인트 충전/이용 내역을 조회 테스트
   */
  @Test
  public void givenUserId_whenHistory_thenStatusOkAndExistBody() throws Exception {
    //given
    long userId = 1L;
    long id1 = 1L;
    long id2 = 2L;

    //when
    List<PointHistory> pointHistories = List.of(
        new PointHistory(id1, userId, 1000, TransactionType.CHARGE, System.currentTimeMillis()),
        new PointHistory(id2, userId, 1000, TransactionType.USE, System.currentTimeMillis())
    );

    when(pointService.history(userId)).thenReturn(pointHistories);

    //then
    mockMvc.perform(get("/point/{id}/histories", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0]").exists())
        .andExpect(jsonPath("$[1]").exists())
        .andExpect(jsonPath("$[0].id").exists())
        .andExpect(jsonPath("$[0].userId").exists())
        .andExpect(jsonPath("$[0].amount").exists())
        .andExpect(jsonPath("$[0].type").exists())
        .andExpect(jsonPath("$[0].updateMillis").exists())
        .andExpect(jsonPath("$[1].id").exists())
        .andExpect(jsonPath("$[1].userId").exists())
        .andExpect(jsonPath("$[1].amount").exists())
        .andExpect(jsonPath("$[1].type").exists())
        .andExpect(jsonPath("$[1].updateMillis").exists())
        .andDo(print());

    verify(pointService).history(userId);
  }

  /**
   * 특정 유저의 포인트를 충전하는 기능 테스트"
   */
  @Test
  public void givenId_whenCharge_thenStatusOkAndExistBody() throws Exception {
    //given
    long id = 1L;
    long chargePoint = 1000L;

    //when
    UserPoint userPoint = new UserPoint(id, chargePoint, System.currentTimeMillis());
    when(pointService.charge(id, chargePoint)).thenReturn(userPoint);

    //then
    mockMvc.perform(patch("/point/{id}/charge", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.valueOf(chargePoint)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.point").exists())
        .andExpect(jsonPath("$.updateMillis").exists())
        .andDo(print());

    verify(pointService).charge(id, chargePoint);
  }

  /**
   * 특정 유저의 포인트를 사용하는 기능 테스트
   */
  @Test
  public void givenIdAndChargePoint_whenUse_thenStatusOkAndExistBodys() throws Exception {
    //given
    long id = 1L;
    long chargePoint = 1000L;

    //when
    UserPoint userPoint = new UserPoint(id, chargePoint, System.currentTimeMillis());
    when(pointService.use(id, chargePoint)).thenReturn(userPoint);

    //then
    mockMvc.perform(patch("/point/{id}/use", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.valueOf(chargePoint)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.point").exists())
        .andExpect(jsonPath("$.updateMillis").exists())
        .andDo(print());

    verify(pointService).use(id, chargePoint);
  }
}
