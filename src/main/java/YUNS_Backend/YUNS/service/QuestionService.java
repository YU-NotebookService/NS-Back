package YUNS_Backend.YUNS.service;

import YUNS_Backend.YUNS.dto.QuestionDto;
import YUNS_Backend.YUNS.entity.Question;
import YUNS_Backend.YUNS.entity.User;
import YUNS_Backend.YUNS.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;

    public List<QuestionDto> getQuestionsByPage(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize); // 페이지는 0부터 시작하므로 1을 빼줌
        Page<Question> paginatedQuestions = questionRepository.findAll(pageable);

        return paginatedQuestions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 특정 질문을 DTO로 반환
    public Optional<QuestionDto> getQuestionById(Long questionId) {
        return questionRepository.findById(questionId).map(this::convertToDto);
    }

    // 질문 생성 및 DTO 반환
    public QuestionDto createQuestion(QuestionDto dto, User user) {
        Question question = Question.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .imageUrl2(dto.getImageUrl2())
                .imageUrl3(dto.getImageUrl3())
                .state(false)
                .date(LocalDateTime.now())
                .user(user)
                .build();

        Question savedQuestion = questionRepository.save(question);
        return convertToDto(savedQuestion);
    }

    // 질문 수정 및 DTO 반환
    public Optional<QuestionDto> updateQuestion(Long questionId, QuestionDto dto) {
        return questionRepository.findById(questionId).map(existingQuestion -> {

            Question updatedQuestion = Question.builder()
                    .questionId(existingQuestion.getQuestionId())
                    .title(dto.getTitle() != null ? dto.getTitle() : existingQuestion.getTitle())
                    .content(dto.getContent() != null ? dto.getContent() : existingQuestion.getContent())
                    .imageUrl(dto.getImageUrl() != null ? dto.getImageUrl() : existingQuestion.getImageUrl())
                    .imageUrl2(dto.getImageUrl2() != null ? dto.getImageUrl2() : existingQuestion.getImageUrl2())
                    .imageUrl3(dto.getImageUrl3() != null ? dto.getImageUrl3() : existingQuestion.getImageUrl3())
                    .date(existingQuestion.getDate())
                    .state(existingQuestion.isState())
                    .answer(existingQuestion.getAnswer())
                    .user(existingQuestion.getUser())
                    .build();

            Question savedQuestion = questionRepository.save(updatedQuestion);
            return convertToDto(savedQuestion);
        });
    }


    // 질문 삭제
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    public Page<QuestionDto> getQuestions(Pageable pageable) {
        return questionRepository.findAll(pageable)
                .map(this::convertToDto); // 엔티티를 DTO로 변환
    }
    // 엔티티를 DTO로 변환
    private QuestionDto convertToDto(Question question) {
        return QuestionDto.builder()
                .questionId(question.getQuestionId())
                .title(question.getTitle())
                .content(question.getContent())
                .imageUrl(question.getImageUrl())
                .imageUrl2(question.getImageUrl2())
                .imageUrl3(question.getImageUrl3())
                .date(question.getDate())
                .state(question.isState())
                .answer(question.getAnswer())
                .userStudentNumber(question.getUser().getStudentNumber())
                .build();
    }

    public Optional<QuestionDto> answerQuestion(Long questionId, String answer) {
        return questionRepository.findById(questionId).map(existingQuestion -> {

            Question updatedQuestion = Question.builder()
                    .questionId(existingQuestion.getQuestionId())
                    .title(existingQuestion.getTitle())
                    .content(existingQuestion.getContent())
                    .date(existingQuestion.getDate())
                    .state(true)
                    .answer(answer)
                    .imageUrl(existingQuestion.getImageUrl())
                    .user(existingQuestion.getUser())
                    .build();

            Question savedQuestion = questionRepository.save(updatedQuestion);
            return convertToDto(savedQuestion);
        });
    }


    public List<QuestionDto> getQuestionsByStudentNumber(String studentNumber) {
        return questionRepository.findByUser_StudentNumber(studentNumber)
                .stream()
                .map(Question.QuestionMapper::toDto)
                .collect(Collectors.toList());
    }

}
