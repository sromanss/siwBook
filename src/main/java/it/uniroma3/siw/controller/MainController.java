package it.uniroma3.siw.controller;

import it.uniroma3.siw.service.BookService;
import it.uniroma3.siw.service.AuthorService;
import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class MainController {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @GetMapping("/")
    public String index(Model model) {
        // Ultimi 6 libri INSERITI nel database (invariato)
        List<Book> recentBooks = StreamSupport.stream(bookService.findAll().spliterator(), false)
                                             .sorted((b1, b2) -> Long.compare(b2.getId(), b1.getId()))
                                             .limit(6)
                                             .collect(Collectors.toList());
        
        // Autori con più libri E ordinamento alfabetico in caso di parità
        List<Author> topAuthors = StreamSupport.stream(authorService.findAll().spliterator(), false)
                                              .sorted((a1, a2) -> {
                                                  // Prima ordina per numero libri (decrescente)
                                                  int bookCountComparison = Integer.compare(a2.getBooks().size(), a1.getBooks().size());
                                                  if (bookCountComparison != 0) {
                                                      return bookCountComparison;
                                                  }
                                                  // Se hanno lo stesso numero di libri, ordina alfabeticamente per cognome
                                                  return a1.getSurname().compareToIgnoreCase(a2.getSurname());
                                              })
                                              .limit(6)
                                              .collect(Collectors.toList());
        
        model.addAttribute("books", recentBooks);
        model.addAttribute("authors", topAuthors);
        return "index";
    }


}
