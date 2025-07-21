package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.service.BookService;
import it.uniroma3.siw.service.ReviewService;
import it.uniroma3.siw.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ImageService imageService;

    @GetMapping("/books")
    public String books(@RequestParam(name = "sort", required = false) String sort,
                       @RequestParam(name = "sortDirection", required = false) String sortDirection,
                       Model model) {
        
        // Recupera tutti i libri
        Iterable<Book> allBooks = bookService.findAll();
        
        // Converti in List per l'ordinamento
        List<Book> bookList = StreamSupport.stream(allBooks.spliterator(), false)
                                          .collect(Collectors.toList());
        
        // Applica l'ordinamento in base al parametro
        if (sort != null) {
            switch (sort) {
                case "insertion":
                    // Ordinamento per ID (più recenti primi se desc, più vecchi primi se asc)
                    bookList.sort(Comparator.comparing(Book::getId));
                    break;
                case "title":
                    bookList.sort(Comparator.comparing(Book::getTitle));
                    break;
                case "year":
                    bookList.sort(Comparator.comparing(Book::getYear));
                    break;
                case "author":
                    bookList.sort(Comparator.comparing(book -> 
                        book.getAuthors().stream()
                            .map(author -> author.getSurname() + " " + author.getName())
                            .findFirst().orElse("")
                    ));
                    break;
                case "reviews":
                    bookList.sort(Comparator.comparingInt(book -> book.getReviews().size()));
                    break;
                case "rating":
                case "averageRating":
                    bookList.sort(Comparator.comparingDouble(Book::getAverageRating));
                    break;
            }
            
            // Applica l'inversione se richiesta
            if ("desc".equals(sortDirection)) {
                Collections.reverse(bookList);
            }
        } else {
            // Ordinamento predefinito per ID decrescente (più recenti primi)
            bookList.sort(Comparator.comparing(Book::getId).reversed());
        }
        
        model.addAttribute("books", bookList);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentSortDirection", sortDirection);
        return "books";
    }

    @GetMapping("/book/{id}")
    public String book(@PathVariable("id") Long id,
                      @RequestParam(name = "reviewSort", required = false) String reviewSort,
                      @RequestParam(name = "reviewSortDirection", required = false) String reviewSortDirection,
                      Model model) {
        Optional<Book> optionalBook = bookService.findById(id);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            
            // Recupera le recensioni
            List<Review> reviews = new ArrayList<>(book.getReviews());
            
            // Applica l'ordinamento alle recensioni
            if (reviewSort != null && !reviews.isEmpty()) {
                switch (reviewSort) {
                    case "insertion":
                        // Ordinamento per data di creazione (con fallback su ID)
                        if (reviews.get(0).getCreatedAt() != null) {
                            reviews.sort(Comparator.comparing(Review::getCreatedAt));
                        } else {
                            reviews.sort(Comparator.comparing(Review::getId));
                        }
                        break;
                    case "rating":
                        reviews.sort(Comparator.comparing(Review::getRating));
                        break;
                }
                
                // Applica l'inversione se richiesta
                if ("desc".equals(reviewSortDirection)) {
                    Collections.reverse(reviews);
                }
            } else {
                // Ordinamento predefinito: più recenti prima
                if (!reviews.isEmpty() && reviews.get(0).getCreatedAt() != null) {
                    reviews.sort(Comparator.comparing(Review::getCreatedAt).reversed());
                } else {
                    reviews.sort(Comparator.comparing(Review::getId).reversed());
                }
            }
            
            // Aggiorna la lista delle recensioni nel libro
            book.setReviews(reviews);
            
            model.addAttribute("book", book);
            model.addAttribute("reviews", reviews);
            model.addAttribute("images", imageService.findByBook(book));
            return "book";
        }
        return "redirect:/books";
    }

    @GetMapping("/books/search")
    public String searchBooks(@RequestParam("search") String search,
                             @RequestParam(name = "sort", required = false) String sort,
                             @RequestParam(name = "sortDirection", required = false) String sortDirection,
                             Model model) {
        
        // Esegui la ricerca
        List<Book> searchResults = bookService.searchByTitleAuthorOrYear(search);
        
        // Applica l'ordinamento ai risultati di ricerca
        if (sort != null) {
            switch (sort) {
                case "insertion":
                    // Ordinamento per ID (più recenti primi se desc, più vecchi primi se asc)
                    searchResults.sort(Comparator.comparing(Book::getId));
                    break;
                case "title":
                    searchResults.sort(Comparator.comparing(Book::getTitle));
                    break;
                case "year":
                    searchResults.sort(Comparator.comparing(Book::getYear));
                    break;
                case "author":
                    searchResults.sort(Comparator.comparing(book -> 
                        book.getAuthors().stream()
                            .map(author -> author.getSurname() + " " + author.getName())
                            .findFirst().orElse("")
                    ));
                    break;
                case "reviews":
                    searchResults.sort(Comparator.comparingInt(book -> book.getReviews().size()));
                    break;
                case "rating":
                case "averageRating":
                    searchResults.sort(Comparator.comparingDouble(Book::getAverageRating));
                    break;
            }
            
            // Applica l'inversione se richiesta
            if ("desc".equals(sortDirection)) {
                Collections.reverse(searchResults);
            }
        } else {
            // Ordinamento predefinito per ID decrescente (più recenti primi)
            searchResults.sort(Comparator.comparing(Book::getId).reversed());
        }
        
        model.addAttribute("books", searchResults);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentSortDirection", sortDirection);
        return "books";
    }
}
