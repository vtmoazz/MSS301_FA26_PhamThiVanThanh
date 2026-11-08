package com.mss301.brand.dto;

/**
 * Kết quả một bước Saga trên MSBrand.
 * previousBrandId giúp MSWatch biết brand cũ để bù trừ khi cần.
 */
// [TODO-5.2, TODO-5.3]
public record AssignmentResponse(Integer watchId, Integer brandId, Integer previousBrandId, String status) {
}
