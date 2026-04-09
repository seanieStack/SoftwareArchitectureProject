package io.github.seaniestack.coreservice.repositories;

import io.github.seaniestack.coreservice.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);

    @Query("""
            select distinct b from Book b
            left join b.authors a
            left join b.categories c
            where (:keyword is null or lower(b.title) like lower(concat('%', :keyword, '%'))
                   or lower(b.isbn) like lower(concat('%', :keyword, '%')))
              and (:author is null or lower(a.name) = lower(:author))
              and (:category is null or lower(c.name) = lower(:category))
            """)
    List<Book> search(
            @Param("keyword") String keyword,
            @Param("author") String author,
            @Param("category") String category
    );
}
