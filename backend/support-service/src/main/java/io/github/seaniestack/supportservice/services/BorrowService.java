package io.github.seaniestack.supportservice.services;

import io.github.seaniestack.supportservice.clients.CoreServiceClient;
import io.github.seaniestack.supportservice.dtos.BorrowDTO;
import io.github.seaniestack.supportservice.dtos.BorrowRequest;
import io.github.seaniestack.supportservice.entities.Borrow;
import io.github.seaniestack.supportservice.entities.BorrowStatus;
import io.github.seaniestack.supportservice.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRepository borrowRepository;
    private final CoreServiceClient coreServiceClient;

    public List<BorrowDTO> getBorrowsForUser(Long userId) {
        log.debug("Fetching borrows for user {}", userId);
        List<Borrow> borrows = borrowRepository.findByUserId(userId);
        return borrows.stream().map(BorrowDTO::from).toList();
    }

    public BorrowDTO getBorrowById(Long borrowId) {
        log.debug("Fetching borrow {}", borrowId);
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow not found. Id: " + borrowId));
        return BorrowDTO.from(borrow);
    }

    @Transactional
    public BorrowDTO createBorrow(BorrowRequest request) {
        log.debug("Creating borrow for user {} book {}", request.userId(), request.bookId());

        if (!coreServiceClient.bookExists(request.bookId())) {
            throw new ResourceNotFoundException("Book not found. Id: " + request.bookId());
        }

        boolean alreadyBorrowed = borrowRepository.existsByBookIdAndStatusIn(
                request.bookId(), List.of(BorrowStatus.BORROWED, BorrowStatus.OVERDUE));
        if (alreadyBorrowed) {
            throw new IllegalStateException("Book " + request.bookId() + " is already borrowed");
        }

        Borrow borrow = Borrow.builder()
                .userId(request.userId())
                .bookId(request.bookId())
                .status(BorrowStatus.BORROWED)
                .borrowedAt(LocalDateTime.now())
                .deadline(request.deadline())
                .build();
        borrowRepository.save(borrow);
        log.info("Created borrow {} for user {} book {}", borrow.getId(), request.userId(), request.bookId());
        return BorrowDTO.from(borrow);
    }

    @Transactional
    public BorrowDTO returnBorrow(Long borrowId) {
        log.debug("Returning borrow {}", borrowId);
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow not found. Id: " + borrowId));

        if (borrow.getStatus() == BorrowStatus.RETURNED) {
            throw new IllegalStateException("Borrow " + borrowId + " is already returned");
        }

        borrow.setStatus(BorrowStatus.RETURNED);
        borrow.setReturnedAt(LocalDateTime.now());
        borrowRepository.save(borrow);
        log.info("Borrow {} returned", borrowId);
        return BorrowDTO.from(borrow);
    }
}
