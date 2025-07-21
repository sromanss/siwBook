package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.model.Book;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface ImageRepository extends CrudRepository<Image, Long> {
    
    public List<Image> findByBook(Book book);
    
    public void deleteByBook(Book book);
}
