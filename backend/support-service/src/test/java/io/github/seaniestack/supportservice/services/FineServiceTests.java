package io.github.seaniestack.supportservice.services;

import io.github.seaniestack.supportservice.dtos.FineDTO;
import io.github.seaniestack.supportservice.entities.Borrow;
import io.github.seaniestack.supportservice.entities.BorrowStatus;
import io.github.seaniestack.supportservice.entities.Fine;
import io.github.seaniestack.supportservice.exceptions.ResourceNotFoundException;
import io.github.seaniestack.supportservice.messaging.EventPublisher;
import io.github.seaniestack.supportservice.repositories.BorrowRepository;
import io.github.seaniestack.supportservice.repositories.FineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FineServiceTests {

    @Mock
    FineRepository fineRepository;

    @Mock
    BorrowRepository borrowRepository;

    @Mock
    EventPublisher eventPublisher;

    @InjectMocks
    FineService fineService;

    @Test
    void getFinesForUserReturnsMappedDTOs() {
        Fine fine = Fine.builder()
                .id(1L)
                .borrowId(10L)
                .userId(5L)
                .amount(BigDecimal.valueOf(1.0))
                .issuedAt(LocalDateTime.now())
                .paid(false)
                .acknowledged(false)
                .build();
        when(fineRepository.findByUserId(5L)).thenReturn(List.of(fine));

        List<FineDTO> result = fineService.getFinesForUser(5L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().borrowId()).isEqualTo(10L);
    }

    @Test
    void getUnacknowledgedFinesReturnsMappedDTOs() {
        Fine fine = Fine.builder()
                .id(1L)
                .borrowId(10L)
                .userId(5L)
                .amount(BigDecimal.valueOf(1.0))
                .issuedAt(LocalDateTime.now())
                .paid(false)
                .acknowledged(false)
                .build();

        when(fineRepository.findByUserIdAndAcknowledgedFalse(5L)).thenReturn(List.of(fine));

        List<FineDTO> result = fineService.getUnacknowledgedFines(5L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().acknowledged()).isFalse();
    }

    @Test
    void getFineByIdReturnsDTO() {
        Fine fine = Fine.builder()
                .id(1L)
                .borrowId(10L)
                .userId(5L)
                .amount(BigDecimal.valueOf(2.0))
                .issuedAt(LocalDateTime.now())
                .paid(false)
                .acknowledged(false)
                .build();

        when(fineRepository.findById(1L)).thenReturn(Optional.of(fine));

        FineDTO result = fineService.getFineById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
    }

    @Test
    void getFineByIdThrowsWhenNotFound() {
        when(fineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fineService.getFineById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- acknowledgeFine ---

    @Test
    void acknowledgeFineMarksAsAcknowledged() {
        Fine fine = Fine.builder().id(1L).acknowledged(false).build();
        when(fineRepository.findById(1L)).thenReturn(Optional.of(fine));

        fineService.acknowledgeFine(1L);

        assertThat(fine.isAcknowledged()).isTrue();
        verify(fineRepository).save(fine);
    }

    @Test
    void acknowledgeFineThrowsWhenNotFound() {
        when(fineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fineService.acknowledgeFine(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- payFine ---

    @Test
    void payFineMarksAsPaidOnly() {
        Fine fine = Fine.builder().id(1L).borrowId(10L).paid(false).build();
        when(fineRepository.findById(1L)).thenReturn(Optional.of(fine));

        fineService.payFine(1L);

        assertThat(fine.isPaid()).isTrue();
        verify(fineRepository).save(fine);
        verifyNoInteractions(borrowRepository);
    }

    @Test
    void payFineDoesNotChangeBorrowStatus() {
        Fine fine = Fine.builder().id(1L).borrowId(10L).paid(false).build();
        when(fineRepository.findById(1L)).thenReturn(Optional.of(fine));

        fineService.payFine(1L);

        verify(borrowRepository, never()).save(any());
    }

    @Test
    void payFineThrowsWhenNotFound() {
        when(fineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fineService.payFine(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- processNewlyOverdue ---

    @Test
    void processNewlyOverdueMarksBorrowAndCreatesFine() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 30, 0, 0);
        Borrow borrow = Borrow.builder()
                .id(10L)
                .userId(5L)
                .status(BorrowStatus.BORROWED)
                .deadline(now.minusDays(1))
                .build();

        when(borrowRepository.findByDeadlineBeforeAndStatus(now, BorrowStatus.BORROWED))
                .thenReturn(List.of(borrow));
        when(fineRepository.findByBorrowIdAndPaidFalse(10L)).thenReturn(Optional.empty());

        fineService.processNewlyOverdue(now);

        assertThat(borrow.getStatus()).isEqualTo(BorrowStatus.OVERDUE);
        verify(borrowRepository).save(borrow);

        ArgumentCaptor<Fine> captor = ArgumentCaptor.forClass(Fine.class);
        verify(fineRepository).save(captor.capture());
        Fine created = captor.getValue();
        assertThat(created.getBorrowId()).isEqualTo(10L);
        assertThat(created.getUserId()).isEqualTo(5L);
        assertThat(created.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(0.5));
        assertThat(created.getIssuedAt()).isEqualTo(now);
        assertThat(created.isPaid()).isFalse();
        assertThat(created.isAcknowledged()).isFalse();
    }

    @Test
    void processNewlyOverdueSkipsFineIfUnpaidFineExists() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 30, 0, 0);
        Borrow borrow = Borrow.builder()
                .id(10L)
                .userId(5L)
                .status(BorrowStatus.BORROWED)
                .deadline(now.minusDays(1))
                .build();

        Fine existingFine = Fine.builder()
                .id(1L)
                .borrowId(10L)
                .paid(false)
                .build();

        when(borrowRepository.findByDeadlineBeforeAndStatus(now, BorrowStatus.BORROWED))
                .thenReturn(List.of(borrow));
        when(fineRepository.findByBorrowIdAndPaidFalse(10L)).thenReturn(Optional.of(existingFine));

        fineService.processNewlyOverdue(now);

        assertThat(borrow.getStatus()).isEqualTo(BorrowStatus.OVERDUE);
        verify(borrowRepository).save(borrow);
        verify(fineRepository, never()).save(any());
    }

    @Test
    void processNewlyOverdueHandlesEmptyList() {
        LocalDateTime now = LocalDateTime.now();
        when(borrowRepository.findByDeadlineBeforeAndStatus(now, BorrowStatus.BORROWED))
                .thenReturn(List.of());

        fineService.processNewlyOverdue(now);

        verify(borrowRepository, never()).save(any());
        verify(fineRepository, never()).save(any());
    }

    // --- updateAccruingFines ---

    @Test
    void updateAccruingFinesUpdatesExistingUnpaidFine() {
        LocalDateTime issuedAt = LocalDateTime.of(2026, 3, 27, 0, 0);
        LocalDateTime now = issuedAt.plusDays(3);

        Borrow borrow = Borrow.builder()
                .id(10L)
                .userId(5L)
                .status(BorrowStatus.OVERDUE)
                .build();

        Fine fine = Fine.builder()
                .id(1L)
                .borrowId(10L)
                .userId(5L)
                .amount(BigDecimal.valueOf(0.5))
                .issuedAt(issuedAt)
                .paid(false)
                .build();

        when(borrowRepository.findByStatus(BorrowStatus.OVERDUE)).thenReturn(List.of(borrow));
        when(fineRepository.findByBorrowIdAndPaidFalse(10L)).thenReturn(Optional.of(fine));

        fineService.updateAccruingFines(now);

        // 3 days * 0.5 = 1.5
        assertThat(fine.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1.5));
        verify(fineRepository).save(fine);
    }

    @Test
    void updateAccruingFinesCreatesNewFineWhenPreviousWasPaid() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 30, 0, 0);

        Borrow borrow = Borrow.builder()
                .id(10L)
                .userId(5L)
                .status(BorrowStatus.OVERDUE)
                .build();

        when(borrowRepository.findByStatus(BorrowStatus.OVERDUE)).thenReturn(List.of(borrow));
        when(fineRepository.findByBorrowIdAndPaidFalse(10L)).thenReturn(Optional.empty());

        fineService.updateAccruingFines(now);

        ArgumentCaptor<Fine> captor = ArgumentCaptor.forClass(Fine.class);
        verify(fineRepository).save(captor.capture());
        Fine created = captor.getValue();
        assertThat(created.getBorrowId()).isEqualTo(10L);
        assertThat(created.getUserId()).isEqualTo(5L);
        assertThat(created.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(0.5));
        assertThat(created.getIssuedAt()).isEqualTo(now);
        assertThat(created.isPaid()).isFalse();
    }

    @Test
    void updateAccruingFinesHandlesEmptyList() {
        when(borrowRepository.findByStatus(BorrowStatus.OVERDUE)).thenReturn(List.of());

        fineService.updateAccruingFines(LocalDateTime.now());

        verify(fineRepository, never()).save(any());
    }

    // --- Full cycle: overdue -> pay -> new fine next day ---

    @Test
    void fullCyclePayFineAndGetNewFineNextDay() {
        LocalDateTime day1 = LocalDateTime.of(2026, 3, 28, 0, 0);
        LocalDateTime day3 = LocalDateTime.of(2026, 3, 30, 0, 0);

        Borrow borrow = Borrow.builder().id(10L).userId(5L)
                .status(BorrowStatus.BORROWED)
                .deadline(day1.minusDays(1)).build();

        // Day 1: book becomes overdue, fine created
        when(borrowRepository.findByDeadlineBeforeAndStatus(day1, BorrowStatus.BORROWED))
                .thenReturn(List.of(borrow));
        when(fineRepository.findByBorrowIdAndPaidFalse(10L)).thenReturn(Optional.empty());

        fineService.processNewlyOverdue(day1);

        assertThat(borrow.getStatus()).isEqualTo(BorrowStatus.OVERDUE);
        ArgumentCaptor<Fine> captor = ArgumentCaptor.forClass(Fine.class);
        verify(fineRepository).save(captor.capture());
        Fine firstFine = captor.getValue();
        assertThat(firstFine.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(0.5));

        // Day 2: user pays the fine
        firstFine.setId(1L);
        when(fineRepository.findById(1L)).thenReturn(Optional.of(firstFine));

        fineService.payFine(1L);

        assertThat(firstFine.isPaid()).isTrue();
        verify(borrowRepository, never()).findById(any());

        // Day 3: scheduler runs, borrow still overdue, new fine created
        reset(fineRepository);
        when(borrowRepository.findByStatus(BorrowStatus.OVERDUE)).thenReturn(List.of(borrow));
        when(fineRepository.findByBorrowIdAndPaidFalse(10L)).thenReturn(Optional.empty());

        fineService.updateAccruingFines(day3);

        ArgumentCaptor<Fine> captor2 = ArgumentCaptor.forClass(Fine.class);
        verify(fineRepository).save(captor2.capture());
        Fine secondFine = captor2.getValue();
        assertThat(secondFine.getBorrowId()).isEqualTo(10L);
        assertThat(secondFine.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(0.5));
        assertThat(secondFine.getIssuedAt()).isEqualTo(day3);
    }
}
