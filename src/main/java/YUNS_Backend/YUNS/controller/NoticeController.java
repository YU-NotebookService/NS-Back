package YUNS_Backend.YUNS.controller;

import YUNS_Backend.YUNS.dto.NoticeDto;
import YUNS_Backend.YUNS.entity.Notice;
import YUNS_Backend.YUNS.entity.User;
import YUNS_Backend.YUNS.service.NoticeService;
import YUNS_Backend.YUNS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private UserService userService;

    @PostMapping("/api/admin/notices/create")
    public ResponseEntity<NoticeDto> createNotice(@RequestBody NoticeDto noticeDto, @AuthenticationPrincipal UserDetails currentUser) {
      User user = userService.findUserByStudentNumber(currentUser.getUsername());

        Notice newNotice = noticeService.convertToEntity(noticeDto);

        // User 설정을 위해 updateUser 메서드 호출
        newNotice = newNotice.updateUser(user);

         NoticeDto createdNoticeDto = noticeService.createNotice(newNotice);
        return ResponseEntity.ok(createdNoticeDto);
    }

}