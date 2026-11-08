package com.mss301.watch.service;

/** Kết quả nghiệp vụ kèm sagaId để controller trả về header X-Saga-Id. */
// [TODO-6.13]
public record SagaResult<T>(String sagaId, T body) {
}
