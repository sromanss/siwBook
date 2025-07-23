package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Book;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface BookRepository extends CrudRepository<Book, Long> {
    
    public List<Book> findByTitleContainingIgnoreCase(String title);
    public List<Book> findByYear(Integer year);
    public boolean existsByTitleAndYear(String title, Integer year);
    public List<Book> findByOrderByYearDesc();
    public List<Book> findByAuthorsNameContainingIgnoreCase(String name);
    public List<Book> findByAuthorsSurnameContainingIgnoreCase(String surname);
}
