package com.mss301.watch.service.saga;

/** Kết quả một Saga đã chạy xong (COMPLETED). Saga thất bại sẽ ném SagaExecutionException. */
// [TODO-6.8]
public record SagaOutcome(String sagaId, Integer watchId, SagaStatus status) {
}
