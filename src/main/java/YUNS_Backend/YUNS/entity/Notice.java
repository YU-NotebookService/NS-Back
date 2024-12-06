package YUNS_Backend.YUNS.entity;

import YUNS_Backend.YUNS.dto.NoticeDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noticeId", updatable = false, nullable = false)
    private Long noticeId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String content;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_number", referencedColumnName = "student_number", nullable = false)
    private User user;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NoticeImage> images = new ArrayList<>();

    public void setUser(User user) {
        this.user = user;
    }


    // 필드를 업데이트하여 새로운 Notice 객체를 반환하는 메서드
    public void updateNotice(String title, String content) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
    }


    // User 업데이트를 위한 별도의 메서드
    public Notice updateUser(User user) {
        return Notice.builder()
                .noticeId(this.noticeId)
                .title(this.title)
                .content(this.content)
                .date(this.date)
                .user(user != null ? user : this.user)  // user가 null이면 기존 값 유지
                .build();
    }


    public void updateImages(List<NoticeImage> newImages) {
        this.images.clear(); // 기존 이미지를 모두 삭제
        if (newImages != null) {
            this.images.addAll(newImages); // 새 이미지를 추가
        }
    }

    public void addImage(NoticeImage image) {
        images.add(image);
        image.setNotice(this); // 연관 관계 설정
    }


    public NoticeDto convertToDto(Notice notice) {
        // Notice의 NoticeImage 리스트를 URL로 변환
        List<String> imageUrls = notice.getImages().stream()
                .map(NoticeImage::getImageUrl) // NoticeImage 객체에서 URL 가져오기
                .collect(Collectors.toList());

        return NoticeDto.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .date(notice.getDate())
                .imageUrl(imageUrls) // URL 리스트 설정
                .build();
    }
}
