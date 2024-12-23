package YUNS_Backend.YUNS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
public class NotebookRegistRequestDto {

    @NotBlank
    private String model;

    private String content;

    @NotBlank
    private String manufactureDate;

    @NotBlank
    private String os;

    private List<MultipartFile> images = new ArrayList<>();;

    @NotNull
    private Integer size;
}
