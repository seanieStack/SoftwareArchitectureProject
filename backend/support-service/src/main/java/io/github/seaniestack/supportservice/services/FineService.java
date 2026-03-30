package io.github.seaniestack.supportservice.services;

import io.github.seaniestack.supportservice.dtos.FineDTO;
import io.github.seaniestack.supportservice.entities.Borrow;
import io.github.seaniestack.supportservice.entities.BorrowStatus;
import io.github.seaniestack.supportservice.entities.Fine;
import io.github.seaniestack.supportservice.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static io.github.seaniestack.supportservice.entities.BorrowStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FineService {

    private final BigDecimal DAILY_RATE = BigDecimal.valueOf(0.5);

    private final FineRepository fineRepository;
    private final BorrowRepository borrowRepository;

    public List<FineDTO> getUnacknowledgedFines(Long userId) {
        log.debug("Fetching unacknowledged fines for user {}", userId);
        List<Fine> fines = fineRepository.findByUserIdAndAcknowledgedFalse(userId);
        log.trace("Found {} unacknowledged fines for user {}", fines.size(), userId);
        return fines.stream().map(FineDTO::from).toList();
    }

    public List<FineDTO> getFinesForUser(Long userId) {
        log.debug("Fetching all fines for user {}", userId);
        List<Fine> fines = fineRepository.findByUserId(userId);
        log.trace("Found {} fines for user {}", fines.size(), userId);
        return fines.stream().map(FineDTO::from).toList();
    }

    public FineDTO getFineById(Long fineId) {
        log.debug("Fetching fine {}", fineId);
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found. Id: " + fineId));
        return FineDTO.from(fine);
    }

    @Transactional
    public void acknowledgeFine(Long fineId) {
        log.debug("Acknowledging fine {}", fineId);
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found. Id: " + fineId));
        fine.setAcknowledged(true);
        fineRepository.save(fine);
        log.info("Fine {} acknowledged successfully", fineId);
    }

    @Transactional
    public void payFine(Long fineId) {
        log.debug("Paying fine {}", fineId);
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found. Id: " + fineId));
        fine.setPaid(true);
        fineRepository.save(fine);
        log.info("Fine {} paid successfully", fineId);
    }

    @Transactional
    public void processNewlyOverdue(LocalDateTime now){
        List<Borrow> newlyOverdue = borrowRepository.findByDeadlineBeforeAndStatus(now, BORROWED);
        log.info("Found {} newly overdue borrows", newlyOverdue.size());

        for(Borrow borrow : newlyOverdue){
            borrow.setStatus(BorrowStatus.OVERDUE);
            borrowRepository.save(borrow);
            log.info("Borrow {} marked as OVERDUE for user {}", borrow.getId(), borrow.getUserId());

            if(fineRepository.findByBorrowIdAndPaidFalse(borrow.getId()).isEmpty()){
                Fine fine = Fine.builder()
                        .borrowId(borrow.getId())
                        .userId(borrow.getUserId())
                        .amount(DAILY_RATE)
                        .issuedAt(now)
                        .acknowledged(false)
                        .paid(false)
                        .paidAt(null)
                        .build();
                fineRepository.save(fine);
                log.info("Created initial fine of {} for borrow {} (user {})", DAILY_RATE, borrow.getId(), borrow.getUserId());
            }
        }
    }

    @Transactional
    public void updateAccruingFines(LocalDateTime now){
        List<Borrow> accruing = borrowRepository.findByStatus(OVERDUE);
        log.info("Updating fines for {} overdue borrows", accruing.size());

        for(Borrow borrow : accruing){
            fineRepository.findByBorrowIdAndPaidFalse(borrow.getId())
                    .ifPresentOrElse(
                            fine -> {
                                BigDecimal newAmount = calcFine(fine.getIssuedAt(), now);
                                log.info("Updating fine {} for borrow {}: {} -> {}", fine.getId(), borrow.getId(), fine.getAmount(), newAmount);
                                fine.setAmount(newAmount);
                                fineRepository.save(fine);
                            },
                            () -> {
                                Fine fine = Fine.builder()
                                        .borrowId(borrow.getId())
                                        .userId(borrow.getUserId())
                                        .amount(DAILY_RATE)
                                        .issuedAt(now)
                                        .acknowledged(false)
                                        .paid(false)
                                        .paidAt(null)
                                        .build();
                                fineRepository.save(fine);
                                log.info("Created new fine of {} for borrow {} (user {}) - previous fine was paid", DAILY_RATE, borrow.getId(), borrow.getUserId());
                            }
                    );
        }
    }

    private BigDecimal calcFine(LocalDateTime from, LocalDateTime now) {
        long days = Math.max(1, Duration.between(from, now).toDays());
        return BigDecimal.valueOf(days).multiply(DAILY_RATE);
    }
}