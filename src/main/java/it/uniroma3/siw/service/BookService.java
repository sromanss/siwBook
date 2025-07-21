package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Transactional
    public Book save(Book book) {
        return this.bookRepository.save(book);
    }

    public Optional<Book> findById(Long id) {
        return this.bookRepository.findById(id);
    }

    public Iterable<Book> findAll() {
        return this.bookRepository.findAll();
    }

    public List<Book> findByTitle(String title) {
        return this.bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Book> findByYear(Integer year) {
        return this.bookRepository.findByYear(year);
    }

    public List<Book> findAllByYearDesc() {
        return this.bookRepository.findByOrderByYearDesc();
    }

    public boolean existsByTitleAndYear(String title, Integer year) {
        return this.bookRepository.existsByTitleAndYear(title, year);
    }

    @Transactional
    public void deleteById(Long id) {
        this.bookRepository.deleteById(id);
    }

    @Transactional
    public Book addAuthorToBook(Book book, Author author) {
        book.getAuthors().add(author);
        return this.bookRepository.save(book);
    }

    @Transactional
    public Book removeAuthorFromBook(Book book, Author author) {
        book.getAuthors().remove(author);
        return this.bookRepository.save(book);
    }

    public List<Book> searchByTitleAuthorOrYear(String searchTerm) {
        Set<Book> results = new HashSet<>();
        
        // Cerca per titolo e autori
        results.addAll(bookRepository.findByTitleContainingIgnoreCase(searchTerm));
        results.addAll(bookRepository.findByAuthorsNameContainingIgnoreCase(searchTerm));
        results.addAll(bookRepository.findByAuthorsSurnameContainingIgnoreCase(searchTerm));
        
        // Prova a convertire il termine di ricerca in anno
        try {
            Integer year = Integer.parseInt(searchTerm);
            results.addAll(bookRepository.findByYear(year));
        } catch (NumberFormatException e) {
            // Se non è un numero, ignora la ricerca per anno
        }
        
        return new ArrayList<>(results);
    }

}
