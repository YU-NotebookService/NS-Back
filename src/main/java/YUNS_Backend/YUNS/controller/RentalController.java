package YUNS_Backend.YUNS.controller;

import YUNS_Backend.YUNS.dto.RentalDto;
import YUNS_Backend.YUNS.dto.RentalDto.RentalRequest;
import YUNS_Backend.YUNS.dto.RentalDto.RentalResponse;
import YUNS_Backend.YUNS.dto.ReservationDto;
import YUNS_Backend.YUNS.entity.Rental;
import YUNS_Backend.YUNS.entity.Reservation;
import YUNS_Backend.YUNS.repository.RentalRepository;
import YUNS_Backend.YUNS.repository.ReservationRepository;
import YUNS_Backend.YUNS.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class RentalController {

    private final ReservationRepository reservationRepository;
    private final RentalService rentalService;
    private final RentalRepository rentalRepository;

    @GetMapping("/api/admin/request/rental")
    public ReservationDto.ReservationResponse getRentalRequests() {
        return rentalService.getAllRentalRequests(); // 서비스 계층으로 위임
    }

    @PostMapping("/api/admin/approve/{reservationId}")
    public ResponseEntity<Object> approveRentalRequest(@PathVariable Long reservationId, @RequestBody RentalDto.RentalApprovalRequest rentalApprovalRequest) {
        rentalService.approveRentalRequest(reservationId, rentalApprovalRequest.getType());

        Map<String, String> response = new HashMap<>();
        response.put("message", "대여 요청 승인이 완료되었습니다.");
        return ResponseEntity.ok(response);
    }

    // 대여현황 조회
    @GetMapping("/api/admin/rentals")
    public ResponseEntity<List<RentalDto.RentalResponse>> getRentalList(@RequestParam(required = false) Long userId) {
        List<RentalDto.RentalResponse> rentalList = rentalService.getRentalList(userId);
        return ResponseEntity.ok(rentalList);
    }

    // 대여현황 수정
    @PutMapping("/api/admin/rentals/{rentalId}/update")
    public ResponseEntity<Object> updateRental(
            @PathVariable Long rentalId,
            @RequestBody RentalDto.RentalRequest rentalRequest
    ) {
        rentalService.updateRental(rentalId, rentalRequest);
        Map<String, String> response = new HashMap<>();
        response.put("message", "대여 현황이 성공적으로 수정되었습니다.");
        return ResponseEntity.ok(response);
    }

    // 대여현황 삭제
    @DeleteMapping("/api/admin/rentals/{rentalId}/delete")
    public ResponseEntity<Object> deleteRental(@PathVariable Long rentalId) {
        rentalService.deleteRental(rentalId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "대여 기록이 성공적으로 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    // 현재 사용자 대여 중인 노트북 조회
    @GetMapping("/api/my/rentals")
    public ResponseEntity<List<RentalDto.CurrentRentalDto>> getCurrentUserRentals(Authentication authentication) {
        String studentNumber = authentication.getName();  // 현재 인증된 사용자의 studentNumber 가져오기
        List<RentalDto.CurrentRentalDto> rentals = rentalService.getCurrentUserRentals(studentNumber);
        return ResponseEntity.ok(rentals);
    }

}
