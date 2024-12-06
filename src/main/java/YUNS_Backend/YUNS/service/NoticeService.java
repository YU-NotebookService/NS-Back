package YUNS_Backend.YUNS.service;

import YUNS_Backend.YUNS.dto.NoticeDto;
import YUNS_Backend.YUNS.entity.Notice;
import YUNS_Backend.YUNS.entity.NoticeImage;
import YUNS_Backend.YUNS.entity.User;
import YUNS_Backend.YUNS.repository.NoticeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NoticeService {

    @Autowired
    private NoticeRepository noticeRepository;

    private final S3Service s3Service;

    @Autowired
    public NoticeService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public Page<NoticeDto> getAllNotices(Pageable pageable) {
        Page<Notice> notices = noticeRepository.findAll(pageable);

        // NoticeDto로 변환
        return notices.map(this::convertToDto);
    }

    public NoticeDto createNotice(NoticeDto noticeDto, List<MultipartFile> images, User user) {
        List<String> imageUrls = uploadImagesToS3(images);

        // Notice 엔티티 생성
        Notice notice = convertToEntity(noticeDto);
        notice.setUser(user);

        // 이미지 추가
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                NoticeImage noticeImage = new NoticeImage(notice, imageUrl);
                notice.addImage(noticeImage);
            }
        }

        // Notice 저장
        Notice savedNotice = noticeRepository.save(notice);

        return convertToDto(savedNotice);
    }

    @Transactional
    public NoticeDto updateNotice(Long id, NoticeDto noticeDto, List<MultipartFile> newImages, List<String> oldImageUrls) {
        Optional<Notice> optionalNotice = noticeRepository.findById(id);
        if (optionalNotice.isEmpty()) {
            throw new EntityNotFoundException("Notice not found with id: " + id);
        }

        Notice notice = optionalNotice.get();

        // 기존 이미지 삭제
        if (oldImageUrls != null && !oldImageUrls.isEmpty()) {
            for (String oldImageUrl : oldImageUrls) {
                s3Service.deleteFile(oldImageUrl);
                notice.getImages().removeIf(image -> image.getImageUrl().equals(oldImageUrl));
            }
        }

        // 새 이미지 추가
        List<String> newImageUrls = uploadImagesToS3(newImages);
        List<NoticeImage> newNoticeImages = newImageUrls.stream()
                .map(url -> new NoticeImage(notice, url))
                .toList();
        notice.updateImages(newNoticeImages);

        // Notice 내용 업데이트
        notice.updateNotice(noticeDto.getTitle(), noticeDto.getContent());

        // 저장 후 DTO 반환
        Notice savedNotice = noticeRepository.save(notice);
        return convertToDto(savedNotice);
    }

    public void deleteNotice(Long id) {
        Optional<Notice> optionalNotice = noticeRepository.findById(id);
        if (optionalNotice.isPresent()) {
            Notice notice = optionalNotice.get();

            // S3에서 이미지 삭제
            notice.getImages().forEach(image -> s3Service.deleteFile(image.getImageUrl()));

            noticeRepository.deleteById(id);
        }
    }

    @Transactional
    public Optional<NoticeDto> getNoticeById(Long id) {
        Optional<Notice> optionalNotice = noticeRepository.findById(id);
        if (optionalNotice.isPresent()) {
            Notice notice = optionalNotice.get();

            // Lazy 로딩 초기화
            notice.getImages().size();

            return Optional.of(convertToDto(notice));
        }
        return Optional.empty();
    }

    private List<String> uploadImagesToS3(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            imageUrls.add(s3Service.uploadFile(image));
        }
        return imageUrls;
    }

    private NoticeDto convertToDto(Notice notice) {
        return NoticeDto.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .date(notice.getDate())
                .imageUrls(notice.getImages().stream()
                        .map(NoticeImage::getImageUrl)
                        .toList())
                .build();
    }

    private Notice convertToEntity(NoticeDto noticeDto) {
        return Notice.builder()
                .noticeId(noticeDto.getNoticeId())
                .title(noticeDto.getTitle())
                .content(noticeDto.getContent())
                .date(noticeDto.getDate() != null ? noticeDto.getDate() : LocalDateTime.now())
                .build();
    }
}
