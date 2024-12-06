package YUNS_Backend.YUNS.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeDto {
    private Long noticeId;
    private String title;
    private String content;
    private LocalDateTime date;
    private List<String> imageUrl;
}
