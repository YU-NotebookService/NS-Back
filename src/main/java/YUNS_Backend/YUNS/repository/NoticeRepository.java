package YUNS_Backend.YUNS.repository;

import YUNS_Backend.YUNS.entity.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // Lazy 로딩 문제를 방지하기 위해 images를 함께 로드
    @Query("SELECT n FROM Notice n LEFT JOIN FETCH n.images WHERE n.noticeId = :id")
    Optional<Notice> findNoticeWithImages(@Param("id") Long id);

    @Query("SELECT n FROM Notice n LEFT JOIN FETCH n.images")
    List<Notice> findAllWithImages(Pageable pageable);
}
