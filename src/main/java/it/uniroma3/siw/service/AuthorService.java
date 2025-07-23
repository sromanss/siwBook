package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AuthorService {
	
	// Repository per accedere ai dati degli autori nel database
    @Autowired
    private AuthorRepository authorRepository;

    @Transactional // per gestire la transazione database automaticamente
    public Author save(Author author) {
        return this.authorRepository.save(author);
    }

    public Optional<Author> findById(Long id) {
        return this.authorRepository.findById(id);
    }

    public Iterable<Author> findAll() {
        return this.authorRepository.findAll();
    }

    public List<Author> findByNameOrSurname(String searchTerm) {
        return this.authorRepository.findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(
                searchTerm, searchTerm);
    }

    public List<Author> findByNationality(String nationality) {
        return this.authorRepository.findByNationality(nationality);
    }

    public List<Author> findAllOrderBySurname() {
        return this.authorRepository.findByOrderBySurnameAsc();
    }

    public boolean existsByNameAndSurnameAndBirthDate(String name, String surname, LocalDate birthDate) {
        return this.authorRepository.existsByNameAndSurnameAndBirthDate(name, surname, birthDate);
    }
   
    public List<Author> searchByNameSurnameOrNationality(String searchTerm) {
        return this.authorRepository.findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrNationalityContainingIgnoreCase(
                searchTerm, searchTerm, searchTerm);
    }

    @Transactional
    public void deleteById(Long id) {
        Optional<Author> authorOpt = this.authorRepository.findById(id);
        if (authorOpt.isPresent()) {
            Author author = authorOpt.get();
            
            // Rimuovi l'autore da tutti i libri prima di eliminarlo
            for (Book book : author.getBooks()) {
                book.getAuthors().remove(author);
            }
            
            // Ora elimina l'autore
            this.authorRepository.deleteById(id);
        }
    }

}
