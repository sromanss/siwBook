package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.service.BookService;
import it.uniroma3.siw.service.AuthorService;
import it.uniroma3.siw.service.ImageService;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class AdminController {

    @Autowired
    private BookService bookService;
    
    @Autowired
    private AuthorService authorService;
    
    @Autowired
    private ImageService imageService;

    /* ========== PAGINA ADMIN PRINCIPALE ========== */
    
    @GetMapping("/admin")
    public String adminHome(@RequestParam(name = "sort", required = false) String sort,
                           @RequestParam(name = "sortDirection", required = false, defaultValue = "desc") String sortDirection,
                           @RequestParam(name = "sortAuthors", required = false) String sortAuthors,
                           @RequestParam(name = "sortAuthorsDirection", required = false, defaultValue = "desc") String sortAuthorsDirection,
                           @RequestParam(name = "searchBooks", required = false) String searchBooks,
                           @RequestParam(name = "searchAuthors", required = false) String searchAuthors,
                           @RequestParam(name = "booksLimit", required = false, defaultValue = "10") int booksLimit,
                           @RequestParam(name = "authorsLimit", required = false, defaultValue = "10") int authorsLimit,
                           @RequestParam(name = "error", required = false) String error,
                           @RequestParam(name = "success", required = false) String success,
                           Model model) {
        
        // Ricerca libri
        Iterable<Book> books;
        if (searchBooks != null && !searchBooks.trim().isEmpty()) {
            books = bookService.searchByTitleAuthorOrYear(searchBooks.trim());
        } else {
            books = bookService.findAll();
        }
        
        // Ricerca autori
        Iterable<Author> authors;
        if (searchAuthors != null && !searchAuthors.trim().isEmpty()) {
            authors = authorService.searchByNameSurnameOrNationality(searchAuthors.trim());
        } else {
            authors = authorService.findAll();
        }

        // Converti in List per poter ordinare i libri
        List<Book> bookList = StreamSupport.stream(books.spliterator(), false)
                                           .collect(Collectors.toList());

        // Applica l'ordinamento per i libri
        if (sort == null) {
            bookList.sort(Comparator.comparingLong(Book::getId).reversed());
        } else {
            switch (sort) {
                case "insertion":
                    bookList.sort(Comparator.comparingLong(Book::getId));
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
            }
            
            // Applica direzione ordinamento per i libri
            if ("asc".equals(sortDirection)) {
                // Ordine crescente già applicato
            } else {
                // Ordine decrescente
                Collections.reverse(bookList);
            }
        }

        // Converti in List per poter ordinare gli autori
        List<Author> authorList = StreamSupport.stream(authors.spliterator(), false)
                                              .collect(Collectors.toList());
        
        // Applica l'ordinamento per gli autori
        if (sortAuthors != null) {
            switch (sortAuthors) {
                case "insertion":
                    authorList.sort(Comparator.comparingLong(Author::getId));
                    break;
                case "surname":
                    authorList.sort(Comparator.comparing(Author::getSurname));
                    break;
                case "name":
                    authorList.sort(Comparator.comparing(Author::getName));
                    break;
                case "nationality":
                    authorList.sort(Comparator.comparing(Author::getNationality, 
                        Comparator.nullsLast(Comparator.naturalOrder())));
                    break;
                case "books":
                    authorList.sort(Comparator.comparingInt(author -> author.getBooks().size()));
                    break;
            }
            
            // Applica direzione ordinamento per gli autori
            if ("asc".equals(sortAuthorsDirection)) {
                // Ordine crescente già applicato
            } else {
                // Ordine decrescente
                Collections.reverse(authorList);
            }
        } else {
            authorList.sort(Comparator.comparingLong(Author::getId).reversed());
        }

        // Calcolo del numero totale di recensioni
        long totalReviews = bookList.stream()
                                    .flatMap(book -> book.getReviews().stream())
                                    .count();

        // Limita i risultati in base ai parametri
        List<Book> limitedBooks = bookList.stream()
                                         .limit(booksLimit)
                                         .collect(Collectors.toList());
        
        List<Author> limitedAuthors = authorList.stream()
                                               .limit(authorsLimit)
                                               .collect(Collectors.toList());

        model.addAttribute("books", limitedBooks);
        model.addAttribute("authors", limitedAuthors);
        model.addAttribute("totalBooks", bookList.size());
        model.addAttribute("totalAuthors", authorList.size());
        model.addAttribute("booksLimit", booksLimit);
        model.addAttribute("authorsLimit", authorsLimit);
        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentSortAuthors", sortAuthors);
        model.addAttribute("currentSortDirection", sortDirection);
        model.addAttribute("currentSortAuthorsDirection", sortAuthorsDirection);
        
        // Gestione messaggi di errore e successo
        if (error != null) {
            switch (error) {
                case "author_has_books":
                    model.addAttribute("errorMessage", 
                        "Impossibile eliminare l'autore perché ha libri associati. Rimuovi prima i libri dalla sua associazione.");
                    break;
                case "delete_failed":
                    model.addAttribute("errorMessage", 
                        "Errore durante l'eliminazione dell'autore. Riprova più tardi.");
                    break;
                case "book_delete_failed":
                    model.addAttribute("errorMessage", 
                        "Errore durante l'eliminazione del libro. Riprova più tardi.");
                    break;
                case "access_denied":
                    model.addAttribute("errorMessage", 
                        "Non hai i permessi per eseguire questa operazione.");
                    break;
                default:
                    model.addAttribute("errorMessage", 
                        "Si è verificato un errore imprevisto.");
            }
        }
        
        if (success != null) {
            switch (success) {
                case "author_deleted":
                    model.addAttribute("successMessage", "Autore eliminato con successo.");
                    break;
                case "book_deleted":
                    model.addAttribute("successMessage", "Libro eliminato con successo.");
                    break;
                case "author_updated":
                    model.addAttribute("successMessage", "Autore aggiornato con successo.");
                    break;
                case "book_updated":
                    model.addAttribute("successMessage", "Libro aggiornato con successo.");
                    break;
                case "author_created":
                    model.addAttribute("successMessage", "Nuovo autore creato con successo.");
                    break;
                case "book_created":
                    model.addAttribute("successMessage", "Nuovo libro creato con successo.");
                    break;
                default:
                    model.addAttribute("successMessage", "Operazione completata con successo.");
            }
        }
        
        addAuthenticationStatus(model);
        return "admin/adminHome";
    }

    /* ========== GESTIONE LIBRI ========== */
    
    @GetMapping("/admin/books/new")
    public String formNewBook(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("authors", authorService.findAll());
        addAuthenticationStatus(model);
        return "admin/formNewBook";
    }

    @PostMapping("/admin/books")
    public String saveBook(@Valid @ModelAttribute("book") Book book,
                          BindingResult bindingResult,
                          @RequestParam("immagineFile") MultipartFile file,
                          @RequestParam("authorIds") List<Long> authorIds,
                          Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorService.findAll());
            addAuthenticationStatus(model);
            return "admin/formNewBook";
        }

        try {
            // Assegna autori selezionati
            Set<Author> selectedAuthors = new HashSet<>();
            for (Long authorId : authorIds) {
                authorService.findById(authorId).ifPresent(selectedAuthors::add);
            }
            book.setAuthors(selectedAuthors);

            // Salva libro
            book = bookService.save(book);

            // Gestione upload immagine
            if (!file.isEmpty()) {
                if (!imageService.isValidImageFile(file)) {
                    model.addAttribute("errorImmagine", "Formato file non supportato. Usa JPG, JPEG o PNG.");
                    model.addAttribute("authors", authorService.findAll());
                    addAuthenticationStatus(model);
                    return "admin/formNewBook";
                }
                
                try {
                    // Salva l'immagine e ottieni l'oggetto Image
                    Image savedImage = imageService.saveImage(book.getId(), file);
                    
                    // ✅ CORREZIONE: aggiorna il photoFileName del book
                    book.setPhotoFileName(savedImage.getFileName());
                    
                    // Salva nuovamente il book con il photoFileName aggiornato
                    book = bookService.save(book);
                    
                } catch (IOException e) {
                    model.addAttribute("errorImmagine", "Errore nel salvataggio dell'immagine.");
                    model.addAttribute("authors", authorService.findAll());
                    addAuthenticationStatus(model);
                    return "admin/formNewBook";
                }
            }

            return "redirect:/admin?success=book_created";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore durante la creazione del libro.");
            model.addAttribute("authors", authorService.findAll());
            addAuthenticationStatus(model);
            return "admin/formNewBook";
        }
    }

    @GetMapping("/admin/books/{id}/edit")
    public String editBook(@PathVariable("id") Long id, Model model) {
        Optional<Book> bookOpt = bookService.findById(id);
        if (bookOpt.isEmpty()) {
            return "redirect:/admin?error=book_not_found";
        }
        
        model.addAttribute("book", bookOpt.get());
        model.addAttribute("authors", authorService.findAll());
        addAuthenticationStatus(model);
        return "admin/formEditBook";
    }

    @PostMapping("/admin/books/{id}")
    public String updateBook(@PathVariable("id") Long id,
                            @ModelAttribute("book") Book book,
                            @RequestParam("immagineFile") MultipartFile file,
                            @RequestParam("authorIds") List<Long> authorIds,
                            @RequestParam(name = "deleteImageIds", required = false) List<Long> deleteImageIds,
                            Model model) {
        
        try {
            // Recupera il libro esistente dal database
            Optional<Book> existingBookOpt = bookService.findById(id);
            if (existingBookOpt.isEmpty()) {
                return "redirect:/admin?error=book_not_found";
            }
            
            Book existingBook = existingBookOpt.get();
            
            // Aggiorna solo i campi modificati mantenendo le immagini
            existingBook.setTitle(book.getTitle());
            existingBook.setYear(book.getYear());
            
            // Assegna autori selezionati
            Set<Author> selectedAuthors = new HashSet<>();
            for (Long authorId : authorIds) {
                authorService.findById(authorId).ifPresent(selectedAuthors::add);
            }
            existingBook.setAuthors(selectedAuthors);
            
            // Elimina immagini selezionate
            if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                for (Long imageId : deleteImageIds) {
                    imageService.deleteImage(imageId);
                }
            }
            
            // Gestione upload nuova immagine
            if (!file.isEmpty()) {
                if (!imageService.isValidImageFile(file)) {
                    model.addAttribute("errorImmagine", "Formato file non supportato. Usa JPG, JPEG o PNG.");
                    model.addAttribute("book", existingBook);
                    model.addAttribute("authors", authorService.findAll());
                    addAuthenticationStatus(model);
                    return "admin/formEditBook";
                }
                
                try {
                    // ✅ CORREZIONE: Salva l'immagine e aggiorna il photoFileName
                    Image savedImage = imageService.saveImage(id, file);
                    existingBook.setPhotoFileName(savedImage.getFileName());
                    
                } catch (IOException e) {
                    model.addAttribute("errorImmagine", "Errore nel salvataggio dell'immagine.");
                    model.addAttribute("book", existingBook);
                    model.addAttribute("authors", authorService.findAll());
                    addAuthenticationStatus(model);
                    return "admin/formEditBook";
                }
            }
            
            // Salva il libro con le modifiche
            bookService.save(existingBook);
            return "redirect:/admin?success=book_updated";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento del libro.");
            model.addAttribute("book", book);
            model.addAttribute("authors", authorService.findAll());
            addAuthenticationStatus(model);
            return "admin/formEditBook";
        }
    }

    @PostMapping("/admin/books/{id}/delete")
    public String deleteBook(@PathVariable("id") Long id) {
        try {
            bookService.deleteById(id);
            return "redirect:/admin?success=book_deleted";
        } catch (Exception e) {
            return "redirect:/admin?error=book_delete_failed";
        }
    }

    /* ========== GESTIONE AUTORI ========== */
    
    @GetMapping("/admin/authors/new")
    public String formNewAuthor(Model model) {
        model.addAttribute("author", new Author());
        addAuthenticationStatus(model);
        return "admin/formNewAuthor";
    }

    // ✅ CORREZIONE: Metodo saveAuthor con gestione immagini
    @PostMapping("/admin/authors")
    public String saveAuthor(@Valid @ModelAttribute("author") Author author,
                            BindingResult bindingResult,
                            @RequestParam("immagineFile") MultipartFile file,
                            Model model) {
        
        if (bindingResult.hasErrors()) {
            addAuthenticationStatus(model);
            return "admin/formNewAuthor";
        }

        try {
            // Salva autore
            author = authorService.save(author);

            // Gestione upload immagine
            if (!file.isEmpty()) {
                if (!imageService.isValidImageFile(file)) {
                    model.addAttribute("errorImmagine", "Formato file non supportato. Usa JPG, JPEG o PNG.");
                    addAuthenticationStatus(model);
                    return "admin/formNewAuthor";
                }
                
                try {
                    // Salva l'immagine e ottieni l'oggetto Image
                    Image savedImage = imageService.saveImage(author.getId(), file);
                    
                    // ✅ CORREZIONE: aggiorna il photoFileName dell'autore
                    author.setPhotoFileName(savedImage.getFileName());
                    
                    // Salva nuovamente l'autore con il photoFileName aggiornato
                    author = authorService.save(author);
                    
                } catch (IOException e) {
                    model.addAttribute("errorImmagine", "Errore nel salvataggio dell'immagine.");
                    addAuthenticationStatus(model);
                    return "admin/formNewAuthor";
                }
            }

            return "redirect:/admin?success=author_created";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore durante la creazione dell'autore.");
            addAuthenticationStatus(model);
            return "admin/formNewAuthor";
        }
    }

    @GetMapping("/admin/authors/{id}/edit")
    public String editAuthor(@PathVariable("id") Long id, Model model) {
        Optional<Author> authorOpt = authorService.findById(id);
        if (authorOpt.isEmpty()) {
            return "redirect:/admin?error=author_not_found";
        }
        
        model.addAttribute("author", authorOpt.get());
        addAuthenticationStatus(model);
        return "admin/formEditAuthor";
    }

    // ✅ CORREZIONE: Metodo updateAuthor con gestione immagini
    @PostMapping("/admin/authors/{id}")
    public String updateAuthor(@PathVariable("id") Long id,
                              @ModelAttribute("author") Author author,
                              @RequestParam("immagineFile") MultipartFile file,
                              Model model) {
        
        try {
            // Recupera l'autore esistente dal database
            Optional<Author> existingAuthorOpt = authorService.findById(id);
            if (existingAuthorOpt.isEmpty()) {
                return "redirect:/admin?error=author_not_found";
            }
            
            Author existingAuthor = existingAuthorOpt.get();
            
            // Aggiorna solo i campi modificati mantenendo le relazioni
            existingAuthor.setName(author.getName());
            existingAuthor.setSurname(author.getSurname());
            existingAuthor.setBirthDate(author.getBirthDate());
            existingAuthor.setDeathDate(author.getDeathDate());
            existingAuthor.setNationality(author.getNationality());
            
            // Gestione upload nuova immagine
            if (!file.isEmpty()) {
                if (!imageService.isValidImageFile(file)) {
                    model.addAttribute("errorImmagine", "Formato file non supportato. Usa JPG, JPEG o PNG.");
                    model.addAttribute("author", existingAuthor);
                    addAuthenticationStatus(model);
                    return "admin/formEditAuthor";
                }
                
                try {
                    // ✅ CORREZIONE: Salva l'immagine e aggiorna il photoFileName
                    Image savedImage = imageService.saveImage(id, file);
                    existingAuthor.setPhotoFileName(savedImage.getFileName());
                    
                } catch (IOException e) {
                    model.addAttribute("errorImmagine", "Errore nel salvataggio dell'immagine.");
                    model.addAttribute("author", existingAuthor);
                    addAuthenticationStatus(model);
                    return "admin/formEditAuthor";
                }
            }
            
            // Salva l'autore con le modifiche
            authorService.save(existingAuthor);
            return "redirect:/admin?success=author_updated";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento dell'autore.");
            model.addAttribute("author", author);
            addAuthenticationStatus(model);
            return "admin/formEditAuthor";
        }
    }

    @PostMapping("/admin/authors/{id}/delete")
    public String deleteAuthor(@PathVariable("id") Long id, Model model) {
        
        try {
            // Verifica se l'autore esiste
            Optional<Author> authorOpt = authorService.findById(id);
            if (authorOpt.isEmpty()) {
                return "redirect:/admin?error=author_not_found";
            }
            
            Author author = authorOpt.get();
            
            // Controlla se l'autore ha libri associati
            if (!author.getBooks().isEmpty()) {
                return "redirect:/admin?error=author_has_books";
            }
            
            // Elimina l'autore se non ha libri
            authorService.deleteById(id);
            return "redirect:/admin?success=author_deleted";
            
        } catch (Exception e) {
            return "redirect:/admin?error=delete_failed";
        }
    }

    /* ========== METODO HELPER CORRETTO ========== */
    
    // ✅ CORREZIONE PRINCIPALE: Ora cerca ROLE_ADMIN invece di ADMIN
    private void addAuthenticationStatus(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);
        model.addAttribute("isAuthenticated", isAuthenticated);
        if (isAuthenticated) {
            // ✅ CORREZIONE: Ora cerca ROLE_ADMIN che sarà presente grazie alla query SQL corretta
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);
        } else {
            model.addAttribute("isAdmin", false);
        }
    }
}
