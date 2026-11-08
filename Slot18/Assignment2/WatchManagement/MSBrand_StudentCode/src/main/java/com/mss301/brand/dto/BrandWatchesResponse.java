package com.mss301.brand.dto;

import java.util.List;

/** Danh sách watchId của một brand, dùng để kiểm chứng Saga đã đồng bộ hai DB. */
// [TODO-5.5]
public record BrandWatchesResponse(Integer brandId, String brandName, int watchCount, List<Integer> watchIds) {
}
