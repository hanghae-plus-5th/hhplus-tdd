package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PointControllerIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private PointRepository postsRepository;

  @Test
  public void 동시성테스트_포인트_적립() {
    // given
    long point1 = 1000L;
    long point2 = 2000L;
    long point3 = 3000L;
    long accountId = 1L;
    String url = "/point/" + accountId + "/charge";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    //when
    CompletableFuture.allOf(
        CompletableFuture.runAsync(() -> {
          HttpEntity<Long> request = new HttpEntity<>(point1, headers);
          restTemplate.exchange(
              url,
              HttpMethod.PATCH,
              request,
              Void.class
          );
        }),

        CompletableFuture.runAsync(() -> {
          HttpEntity<Long> request = new HttpEntity<>(point2, headers);
          restTemplate.exchange(
              url,
              HttpMethod.PATCH,
              request,
              Void.class
          );
        }),

        CompletableFuture.runAsync(() -> {
          HttpEntity<Long> request = new HttpEntity<>(point3, headers);
          restTemplate.exchange(
              url,
              HttpMethod.PATCH,
              request,
              Void.class
          );
        })
    ).join();

    //then
    UserPoint userPoint = postsRepository.selectById(accountId);
    assertEquals(userPoint.id(), accountId);
    assertEquals(userPoint.point(), point1 + point2 + point3);
  }

  @Test
  public void 동시성테스트_포인트_사용() {
    // given
    long point1 = 1000L;
    long point2 = 2000L;
    long point3 = 3000L;
    long chargePoint = 10000L;
    long accountId = 2L;
    String url = "/point/" + accountId + "/charge";

    //when
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

    //금액 충전
    HttpEntity<Long> chargeRequest = new HttpEntity<>(chargePoint, headers);
    restTemplate.exchange(
        url,
        HttpMethod.PATCH,
        chargeRequest,
        Void.class
    );

    //금액사용
    CompletableFuture.allOf(
        CompletableFuture.runAsync(() -> {
          HttpEntity<Long> request = new HttpEntity<>(point1, headers);
          restTemplate.exchange(
              "/point/" + accountId + "/use",
              HttpMethod.PATCH,
              request,
              Void.class
          );
        }),

        CompletableFuture.runAsync(() -> {
          HttpEntity<Long> request = new HttpEntity<>(point2, headers);
          restTemplate.exchange(
              "/point/" + accountId + "/use",
              HttpMethod.PATCH,
              request,
              Void.class
          );
        }),

        CompletableFuture.runAsync(() -> {
          HttpEntity<Long> request = new HttpEntity<>(point3, headers);
          restTemplate.exchange(
              "/point/" + accountId + "/use",
              HttpMethod.PATCH,
              request,
              Void.class
          );
        })
    ).join();

    //then
    UserPoint userPoint = postsRepository.selectById(accountId);
    assertEquals(userPoint.id(), accountId);
    assertEquals(userPoint.point(), chargePoint - (point1 + point2 + point3));
    System.out.printf("test");
  }
}
