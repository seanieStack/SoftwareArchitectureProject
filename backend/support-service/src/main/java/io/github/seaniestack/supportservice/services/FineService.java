package io.github.seaniestack.supportservice.services;

import io.github.seaniestack.supportservice.dtos.FineDTO;
import io.github.seaniestack.supportservice.entities.Fine;
import io.github.seaniestack.supportservice.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FineService {

    private final FineRepository fineRepository;

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

    public void acknowledgeFine(Long fineId) {
        log.debug("Acknowledging fine {}", fineId);
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found. Id: " + fineId));
        fine.setAcknowledged(true);
        fineRepository.save(fine);
        log.info("Fine {} acknowledged successfully", fineId);
    }

    public void payFine(Long fineId) {
        log.debug("Paying fine {}", fineId);
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found. Id: " + fineId));
        fine.setPaid(true);
        fineRepository.save(fine);
        log.info("Fine {} paid successfully", fineId);
    }
}
