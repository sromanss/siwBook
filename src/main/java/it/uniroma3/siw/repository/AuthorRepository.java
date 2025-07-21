package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Author;
import org.springframework.data.repository.CrudRepository;
import java.time.LocalDate;
import java.util.List;

public interface AuthorRepository extends CrudRepository<Author, Long> {
    
    public List<Author> findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(String name, String surname);
    public boolean existsByNameAndSurnameAndBirthDate(String name, String surname, LocalDate birthDate);
    public List<Author> findByNationality(String nationality);
    public List<Author> findByOrderBySurnameAsc();
    public List<Author> findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrNationalityContainingIgnoreCase(
            String name, String surname, String nationality);
}
