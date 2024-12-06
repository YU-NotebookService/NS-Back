package YUNS_Backend.YUNS.controller;

import YUNS_Backend.YUNS.dto.NoticeDto;
import YUNS_Backend.YUNS.entity.Notice;
import YUNS_Backend.YUNS.entity.User;
import YUNS_Backend.YUNS.service.NoticeService;
import YUNS_Backend.YUNS.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private UserService userService;

    @PostMapping("/api/admin/notices/create")
    public ResponseEntity<NoticeDto> createNotice(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails currentUser) {

        User user = userService.findUserByStudentNumber(currentUser.getUsername());
        if (user == null) {
            return ResponseEntity.badRequest().body(null);
        }

        NoticeDto noticeDto = NoticeDto.builder()
                .title(title)
                .content(content)
                .build();

        NoticeDto createdNotice = noticeService.createNotice(noticeDto, images, user);
        return ResponseEntity.ok(createdNotice);
    }

    @PutMapping("/api/admin/notices/{id}/update")
    public ResponseEntity<NoticeDto> updateNotice(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @RequestParam(value = "oldImageUrls", required = false) List<String> oldImageUrls) {

        NoticeDto noticeDto = NoticeDto.builder()
                .title(title)
                .content(content)
                .build();

        NoticeDto updatedNotice = noticeService.updateNotice(id, noticeDto, newImages, oldImageUrls);
        return updatedNotice != null ? ResponseEntity.ok(updatedNotice) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/api/admin/notices/{noticeId}/delete")
    public ResponseEntity<String> deleteNotice(@PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.ok("공지사항이 삭제되었습니다.");
    }

    @GetMapping("/api/noticeList")
    public ResponseEntity<Page<NoticeDto>> getAllNotices(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(defaultValue = "desc") String sortOrder) {
        Pageable pageable = PageRequest.of(page, size, "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by("noticeId").ascending()
                : Sort.by("noticeId").descending());
        Page<NoticeDto> noticeList = noticeService.getAllNotices(pageable);
        return ResponseEntity.ok(noticeList);
    }

    @GetMapping("/api/noticeList/{noticeId}")
    public ResponseEntity<NoticeDto> getNoticeById(@PathVariable Long noticeId) {
        Optional<NoticeDto> noticeDto = noticeService.getNoticeById(noticeId);
        return noticeDto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

