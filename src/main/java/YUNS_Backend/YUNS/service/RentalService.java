package YUNS_Backend.YUNS.service;

import YUNS_Backend.YUNS.dto.RentalDto;
import YUNS_Backend.YUNS.entity.Rental;
import YUNS_Backend.YUNS.entity.Reservation;
import YUNS_Backend.YUNS.exception.CustomException;
import YUNS_Backend.YUNS.exception.ErrorCode;
import YUNS_Backend.YUNS.repository.RentalRepository;
import YUNS_Backend.YUNS.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final ReservationRepository reservationRepository;

    // 대여 요청 승인
    @Transactional
    public void approveRentalRequest(Long reservationId, String type) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));

        if (type.equals("RENTAL")) {
            Rental rental = Rental.builder()
                    .user(reservation.getUser())
                    .notebook(reservation.getNotebook())
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusMonths(1)) // 반납예정 1개월
                    .build();

            rentalRepository.save(rental);
        } else if (type.equalsIgnoreCase("EXTEND")) {
            // 연장 요청 처리
            Rental rental = rentalRepository.findByStudentNumber(reservation.getUser().getStudentNumber())
                    .orElseThrow(() -> new RuntimeException("Rental not found for the user with student number: "
                            + reservation.getUser().getStudentNumber()));

            Rental updatedRental = Rental.builder()
                    .rentalId(rental.getRentalId())
                    .notebook(rental.getNotebook())
                    .user(rental.getUser())
                    .startDate(rental.getStartDate())
                    .endDate(rental.getEndDate().plusMonths(1))
                    .build();

            rentalRepository.save(updatedRental);
        } else {
            throw new RuntimeException("Invalid request type: " + type);
        }

        reservationRepository.delete(reservation);
    }

    public List<RentalDto.RentalResponse> getRentalList(Long userId) {
        List<Rental> rentals;
        if (userId == null) {
            rentals = rentalRepository.findAll(); // 모든 대여 기록
        } else {
            rentals = rentalRepository.findByUser_UserId(userId); // 특정 사용자 대여 기록
        }

        return rentals.stream()
                .map(rental -> RentalDto.RentalResponse.builder()
                        .rentalId(rental.getRentalId())
                        .startDate(rental.getStartDate().toString())
                        .endDate(rental.getEndDate().toString())
                        .userId(rental.getUser().getUserId())
                        .notebookId(rental.getNotebook().getNotebookId())
                        .rentalStatus(rental.getRentalStatus().name()) // RentalStatus 추가
                        .build())
                .collect(Collectors.toList());
    }

    // 대여 현황 수정
    public void updateRental(Long rentalId, RentalDto.RentalRequest rentalRequest) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new CustomException(ErrorCode.RENTAL_NOT_FOUND));

        // DTO에서 변환된 데이터를 사용
        rental.updateRental(
                rentalRequest.getParsedStartDate(),
                rentalRequest.getParsedEndDate(),
                rentalRequest.getParsedRentalStatus()
        );

        rentalRepository.save(rental); // 업데이트된 데이터 저장
    }


    // 대여현황 삭제
    public void deleteRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new CustomException(ErrorCode.RENTAL_NOT_FOUND));

        rentalRepository.delete(rental);  // 대여 기록 삭제
    }


    @Transactional(readOnly = true)
    public List<RentalDto.CurrentRentalDto> getCurrentUserRentals(String studentNumber) {
        List<Rental> rentals = rentalRepository.findByUser_StudentNumber(studentNumber);

        // Rental 엔티티를 CurrentRentalDto로 변환
        return rentals.stream()
                .map(rental -> RentalDto.CurrentRentalDto.builder()
                        .rentalId(rental.getRentalId())
                        .notebookId(rental.getNotebook().getNotebookId())
                        .startDate(rental.getStartDate().toString())
                        .endDate(rental.getEndDate().toString())
                        .rentalStatus("대여 중")  // 필요에 따라 동적으로 설정 가능
                        .build())
                .collect(Collectors.toList());
    }
}
