package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Credentials;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends CrudRepository<Review, Long> {
    
    List<Review> findByBook(Book book);
    List<Review> findByCredentials(Credentials credentials);
    Optional<Review> findByBookAndCredentials(Book book, Credentials credentials);
    boolean existsByBookAndCredentials(Book book, Credentials credentials);
}
