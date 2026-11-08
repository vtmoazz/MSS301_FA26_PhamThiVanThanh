package com.mss301.watch.repository;

import com.mss301.watch.entity.Watch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// [TODO-6.16]
public interface WatchRepository extends JpaRepository<Watch, Integer> {

    /** Watch mới thêm nằm đầu danh sách. */
    List<Watch> findAllByOrderByWatchIdDesc();
}
