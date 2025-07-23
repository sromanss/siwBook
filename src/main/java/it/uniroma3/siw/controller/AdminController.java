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
    // Service per gestire operazioni sui libri
    private BookService bookService;
    
    @Autowired
    // Service per gestire operazioni sugli autori
    private AuthorService authorService;
    
    @Autowired
    // Service per gestire operazioni sulle immagini
    private ImageService imageService;

    /* ========== PAGINA ADMIN PRINCIPALE ========== */
    // Richieste GET verso /admin
    @GetMapping("/admin")
    public String adminHome(@RequestParam(name = "sort", required = false) String sort, // Parametro opzionale sort per ordinamento
                           @RequestParam(name = "sortDirection", required = false, defaultValue = "desc") String sortDirection, // Parametro per direzione ordinamento (default desc)
                           @RequestParam(name = "sortAuthors", required = false) String sortAuthors, // Parametro per ordinamento autori
                           @RequestParam(name = "sortAuthorsDirection", required = false, defaultValue = "desc") String sortAuthorsDirection,
                           @RequestParam(name = "searchBooks", required = false) String searchBooks, //termini di ricerca per libri
                           @RequestParam(name = "searchAuthors", required = false) String searchAuthors,
                           @RequestParam(name = "booksLimit", required = false, defaultValue = "10") int booksLimit, //numero massimo di libri/autori da mostrare
                           @RequestParam(name = "authorsLimit", required = false, defaultValue = "10") int authorsLimit,
                           @RequestParam(name = "error", required = false) String error,	//parametro per messaggi di errore/successo
                           @RequestParam(name = "success", required = false) String success,
                           Model model) { //oggetto per passare dati alla vista
        
        // Ricerca libri
        Iterable<Book> books;	//variabile per contenere i libri trovati
        // Se è stato inserito un termine di ricerca
        if (searchBooks != null && !searchBooks.trim().isEmpty()) {
        	// cercca libri per titolo, autore o anno
            books = bookService.searchByTitleAuthorOrYear(searchBooks.trim());
        } else {
        	// altrimenti mostrali tutti
            books = bookService.findAll();
        }
        
        // Ricerca autori
        Iterable<Author> authors;
        // Se è stato inserito un termine di ricerca
        if (searchAuthors != null && !searchAuthors.trim().isEmpty()) {
        	// cerca libri per nome, cognome o nazionalità
            authors = authorService.searchByNameSurnameOrNationality(searchAuthors.trim());
        } else {
        	// altrimenti mostrali tutti
            authors = authorService.findAll();
        }

        // Converti in List per poter ordinare i libri
        List<Book> bookList = StreamSupport.stream(books.spliterator(), false)
                                           .collect(Collectors.toList());

        // Applica l'ordinamento per i libri
        // se non è specificato un ordinamento
        if (sort == null) {
        	// ordina per id decrescente (più recenti prima)
            bookList.sort(Comparator.comparingLong(Book::getId).reversed());
        } else {
            switch (sort) {	// switch basato sul tipo di ordinamento richiesto
                case "insertion":	// ordinamento per data inserimento
                    bookList.sort(Comparator.comparingLong(Book::getId));
                    break;
                case "title":	// ordinamento alfabetico per titolo
                    bookList.sort(Comparator.comparing(Book::getTitle));
                    break;
                case "year":	// ordinamento per anno di pubblicazione
                    bookList.sort(Comparator.comparing(Book::getYear));
                    break;
                case "author":	//ordinamento per nome/cognome autore
                    bookList.sort(Comparator.comparing(book -> 
                        book.getAuthors().stream()
                            .map(author -> author.getSurname() + " " + author.getName())
                            .findFirst().orElse("") //prende il primo autore o stringa vuota
                    ));
                    break;
                case "reviews":	//ordinamento per numero recensioni
                    bookList.sort(Comparator.comparingInt(book -> book.getReviews().size()));
                    break;
            }
            
            // Applica direzione ordinamento per i libri
            if ("asc".equals(sortDirection)) {	// se richiesto ordine crescente
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
        if (sortAuthors != null) { // se è specificato un ordinamento per autori
            switch (sortAuthors) { // switch basato sul tipo di ordinamento
                case "insertion": // per data inserimento
                    authorList.sort(Comparator.comparingLong(Author::getId));
                    break;
                case "surname":	//per cognome
                    authorList.sort(Comparator.comparing(Author::getSurname));
                    break;
                case "name":	//per nome
                    authorList.sort(Comparator.comparing(Author::getName));
                    break;
                case "nationality": //per nazionalità
                    authorList.sort(Comparator.comparing(Author::getNationality, 
                        Comparator.nullsLast(Comparator.naturalOrder())));
                    break;
                case "books":	//per numero di libri scritti
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
        	// ordinamento di default: id crescente
            authorList.sort(Comparator.comparingLong(Author::getId).reversed());
        }

        // Calcolo del numero totale di recensioni
        long totalReviews = bookList.stream()
                                    .flatMap(book -> book.getReviews().stream())
                                    .count();

        // Limita i risultati in base ai parametri (prendi i primi 10)
        List<Book> limitedBooks = bookList.stream()
                                         .limit(booksLimit)
                                         .collect(Collectors.toList());
        // Limita i risultati in base ai parametri (prendi i primi 10)
        List<Author> limitedAuthors = authorList.stream()
                                               .limit(authorsLimit)
                                               .collect(Collectors.toList());
        // aggiungi tutti i dati al model per la vista
        model.addAttribute("books", limitedBooks);	//libri da mostrare
        model.addAttribute("authors", limitedAuthors);	//autori da mostrare
        model.addAttribute("totalBooks", bookList.size());	//numero totale di libri
        model.addAttribute("totalAuthors", authorList.size());	//numero totale di autori
        model.addAttribute("booksLimit", booksLimit);	//limite libri corrente
        model.addAttribute("authorsLimit", authorsLimit);	//limite autori corrente
        model.addAttribute("totalReviews", totalReviews);	//numero totale recensioni
        model.addAttribute("currentSort", sort);	//ordinamento corrente libri
        model.addAttribute("currentSortAuthors", sortAuthors);	//ordinamento corrente autori
        model.addAttribute("currentSortDirection", sortDirection);	//direzione di ordinamento libri
        model.addAttribute("currentSortAuthorsDirection", sortAuthorsDirection);	//direzione di ordinamento autori
        
        // Gestione messaggi di errore e successo
        if (error != null) {	// se c'è un parametro error nell'url
            switch (error) {	// switch sui diversi tipi di errore
                case "author_has_books":	//L'autore ha libri associati
                    model.addAttribute("errorMessage", 
                        "Impossibile eliminare l'autore perché ha libri associati. Rimuovi prima i libri dalla sua associazione.");
                    break;
                case "delete_failed":	// errore generico eliminazione
                    model.addAttribute("errorMessage", 
                        "Errore durante l'eliminazione dell'autore. Riprova più tardi.");
                    break;
                case "book_delete_failed":	//errore eliminazione libro
                    model.addAttribute("errorMessage", 
                        "Errore durante l'eliminazione del libro. Riprova più tardi.");
                    break;
                case "access_denied":	//errore permessi
                    model.addAttribute("errorMessage", 
                        "Non hai i permessi per eseguire questa operazione.");
                    break;
                default:
                    model.addAttribute("errorMessage",  //errore generico
                        "Si è verificato un errore imprevisto.");
            }
        }
        
        if (success != null) { // se c'è un parametro success nell'url
            switch (success) { // switch basato sui diversi tipi si successo
                case "author_deleted":	//successo eliminazione autore
                    model.addAttribute("successMessage", "Autore eliminato con successo.");
                    break;
                case "book_deleted":	//successo eliminazione libro
                    model.addAttribute("successMessage", "Libro eliminato con successo.");
                    break;
                case "author_updated":	//successo aggiornamento autore
                    model.addAttribute("successMessage", "Autore aggiornato con successo.");
                    break;
                case "book_updated":	//successo aggiornamento libro
                    model.addAttribute("successMessage", "Libro aggiornato con successo.");
                    break;
                case "author_created":	//successo creazione autore
                    model.addAttribute("successMessage", "Nuovo autore creato con successo.");
                    break;
                case "book_created":	//successo creazione libro
                    model.addAttribute("successMessage", "Nuovo libro creato con successo.");
                    break;
                default:	//successo generico
                    model.addAttribute("successMessage", "Operazione completata con successo.");
            }
        }
        
        addAuthenticationStatus(model);	//aggiunge informazioni di autenticazione
        return "admin/adminHome";	//restituisce il template Thymeleaf
    }

    /* ========== GESTIONE LIBRI ========== */
    // Risponde a get per creare nuovo libro
    @GetMapping("/admin/books/new")
    public String formNewBook(Model model) {
    	// aggiunge oggetto book vuoto per il form
        model.addAttribute("book", new Book());
        // aggiunge lista autori per select
        model.addAttribute("authors", authorService.findAll());
        // aggiunge info autenticazione
        addAuthenticationStatus(model);
        return "admin/formNewBook";	//restituisce il template Thymeleaf
    }
    // Risponde a post per salvare un nuovo libro
    @PostMapping("/admin/books")
    public String saveBook(@Valid @ModelAttribute("book") Book book, //Libro del form, validato automaticamente
                          BindingResult bindingResult,	// Risultato della validazione
                          @RequestParam("immagineFile") MultipartFile file,	// File immagine uploadato
                          @RequestParam("authorIds") List<Long> authorIds,	//Lista id autori selezionati
                          Model model) {	//Model per passare dati alla vista
        
        if (bindingResult.hasErrors()) { //Se ci sono errori di validazione
            model.addAttribute("authors", authorService.findAll());	//Ricarica lista autori
            addAuthenticationStatus(model); //Ricarica info autenticazione
            return "admin/formNewBook"; // Torna al form con errori
        }

        try {
            // Assegna autori selezionati
            Set<Author> selectedAuthors = new HashSet<>();	//Crea set per autori selezionati
            for (Long authorId : authorIds) {	//Per ogni ID autore selezionato
                authorService.findById(authorId).ifPresent(selectedAuthors::add);	//Trova autore e aggiungilo al set
            }
            book.setAuthors(selectedAuthors);	//assegna set autori al libro

            // Salva libro
            book = bookService.save(book);

            // Gestione upload immagine
            // Se è stato caricato un file
            if (!file.isEmpty()) {
                if (!imageService.isValidImageFile(file)) {	//Verifica che il formato sia valido
                	// Altrimenti manda messaggio di errore e torna al form con errore
                    model.addAttribute("errorImmagine", "Formato file non supportato. Usa JPG, JPEG o PNG.");
                    model.addAttribute("authors", authorService.findAll());
                    addAuthenticationStatus(model);
                    return "admin/formNewBook";
                }
                
                try {
                    // Salva l'immagine e ottieni l'oggetto Image
                    Image savedImage = imageService.saveImage(book.getId(), file);
                    
                    // aggiorna il photoFileName del libro
                    book.setPhotoFileName(savedImage.getFileName());
                    
                    // Salva nuovamente il book con il photoFileName aggiornato
                    book = bookService.save(book);
                    
                } catch (IOException e) {	//Se ci sono errori nel salvataggio file torna messaggio di errore
                    model.addAttribute("errorImmagine", "Errore nel salvataggio dell'immagine.");
                    model.addAttribute("authors", authorService.findAll());
                    addAuthenticationStatus(model);
                    return "admin/formNewBook";
                }
            }
            // redirect alla pagina admin con messaggio di successo
            return "redirect:/admin?success=book_created";
            
        } catch (Exception e) {	//se c'è qualsiasi altro errore torna al form con errore
            model.addAttribute("errorMessage", "Errore durante la creazione del libro.");
            model.addAttribute("authors", authorService.findAll());
            addAuthenticationStatus(model);
            return "admin/formNewBook";
        }
    }
    //Risponde a GET per modificare il libro esistente
    @GetMapping("/admin/books/{id}/edit")
    public String editBook(@PathVariable("id") Long id, Model model) {
        Optional<Book> bookOpt = bookService.findById(id);	//Cerca libro per ID
        if (bookOpt.isEmpty()) {	//Se non lo trova ritorna errore
            return "redirect:/admin?error=book_not_found";
        }
        // Aggiunge libro trovato al model e la lista autori, poi restituisce template form
        model.addAttribute("book", bookOpt.get());
        model.addAttribute("authors", authorService.findAll());
        addAuthenticationStatus(model);
        return "admin/formEditBook";
    }
    //Risponde a POST per modificare il libro
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
    
    //Risponde a POST per eliminare libro
    @PostMapping("/admin/books/{id}/delete")
    public String deleteBook(@PathVariable("id") Long id) {
        try {
        	//Elimina libro dal databse e restiusci successo, altrimenti errore
            bookService.deleteById(id);
            return "redirect:/admin?success=book_deleted";
        } catch (Exception e) {
            return "redirect:/admin?error=book_delete_failed";
        }
    }

    /* ========== GESTIONE AUTORI ========== */
    //Risponde a GET per creare nuovo libro
    @GetMapping("/admin/authors/new")
    public String formNewAuthor(Model model) {
    	//aggiunge oggetto Author vuoto, info autenticazione e restituisce template form
        model.addAttribute("author", new Author());
        addAuthenticationStatus(model);
        return "admin/formNewAuthor";
    }

    //Risponde a POST per salvare nuovo autore
    @PostMapping("/admin/authors")
    public String saveAuthor(@Valid @ModelAttribute("author") Author author,	//autore dal form, già validato
                            BindingResult bindingResult,
                            @RequestParam("immagineFile") MultipartFile file,
                            Model model) {
        // Se ci sono errori di validazione torna al form con errori
        if (bindingResult.hasErrors()) {
            addAuthenticationStatus(model);
            return "admin/formNewAuthor";
        }

        try {
            // Salva autore
            author = authorService.save(author);

            // Gestione upload immagine
            if (!file.isEmpty()) {
                if (!imageService.isValidImageFile(file)) {	//Verifica formato file
                    model.addAttribute("errorImmagine", "Formato file non supportato. Usa JPG, JPEG o PNG.");
                    addAuthenticationStatus(model);
                    return "admin/formNewAuthor";
                }
                
                try {
                    // Salva l'immagine e ottieni l'oggetto Image
                    Image savedImage = imageService.saveImage(author.getId(), file);
                    
                    // aggiorna il photoFileName dell'autore
                    author.setPhotoFileName(savedImage.getFileName());
                    
                    // Salva nuovamente l'autore con il photoFileName aggiornato
                    author = authorService.save(author);
                    
                } catch (IOException e) {	//Gestione errore di salvataggio
                    model.addAttribute("errorImmagine", "Errore nel salvataggio dell'immagine.");
                    addAuthenticationStatus(model);
                    return "admin/formNewAuthor";
                }
            }
            //Redirect con successo
            return "redirect:/admin?success=author_created";
            
        } catch (Exception e) {	//Gestione errore generico
            model.addAttribute("errorMessage", "Errore durante la creazione dell'autore.");
            addAuthenticationStatus(model);
            return "admin/formNewAuthor";
        }
    }
    // Risponde a GET per modificare autore
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

    // Risponde a POST per modificare autore
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
                    //Salva l'immagine e aggiorna il photoFileName
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
    // Risponde a POST per  dedlete autore
    @PostMapping("/admin/authors/{id}/delete")
    public String deleteAuthor(@PathVariable("id") Long id, Model model) {
        
        try {
            // Verifica se l'autore esiste
            Optional<Author> authorOpt = authorService.findById(id);
            if (authorOpt.isEmpty()) {
                return "redirect:/admin?error=author_not_found";
            }
            
            Author author = authorOpt.get();	//ottiene l'oggetto autore
            
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

    /* ========== METODO HELPER ========== */
    
    // Metodo privato per aggiungere ingo autenticazione
    private void addAuthenticationStatus(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);
        model.addAttribute("isAuthenticated", isAuthenticated);
        if (isAuthenticated) {
            
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);
        } else {
            model.addAttribute("isAdmin", false);
        }
    }
}
