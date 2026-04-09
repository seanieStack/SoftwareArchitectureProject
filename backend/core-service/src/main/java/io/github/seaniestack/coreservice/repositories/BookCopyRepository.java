package io.github.seaniestack.coreservice.repositories;

import io.github.seaniestack.coreservice.entities.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
}
