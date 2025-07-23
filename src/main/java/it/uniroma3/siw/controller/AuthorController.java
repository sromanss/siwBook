package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    @GetMapping("/authors")
    public String authors(@RequestParam(name = "sort", required = false) String sort,	//parametro opzionale per tipo di ordinamento
                         @RequestParam(name = "sortDirection", required = false) String sortDirection,	//parametro opzionale per direzione
                         Model model) {
        
        // Recupera tutti gli autori
        Iterable<Author> allAuthors = authorService.findAll();
        
        // Converti in List per l'ordinamento
        List<Author> authorList = StreamSupport.stream(allAuthors.spliterator(), false)
                                              .collect(Collectors.toList());
        
        // Applica l'ordinamento in base al parametro
        if (sort != null) {
            switch (sort) {	//switch sul tipo di ordinamento richiesto
                case "insertion":
                    // Ordinamento per ID (più recenti primi se desc, più vecchi primi se asc)
                    authorList.sort(Comparator.comparing(Author::getId));
                    break;
                case "surname":
                    authorList.sort(Comparator.comparing(Author::getSurname));
                    break;
                case "birthYear":
                    authorList.sort(Comparator.comparing(author -> 
                        author.getBirthDate() != null ? author.getBirthDate().getYear() : 0));
                    break;
                case "books":
                    authorList.sort(Comparator.comparingInt(author -> author.getBooks().size()));
                    break;
            }
            
            // Applica l'inversione se richiesta
            if ("desc".equals(sortDirection)) {
                Collections.reverse(authorList);
            }
        } else {
            // Ordinamento predefinito per ID decrescente (più recenti primi)
            authorList.sort(Comparator.comparing(Author::getId).reversed());
        }
        
        model.addAttribute("authors", authorList);	//aggiunge lista autori ordinata al model
        model.addAttribute("currentSort", sort);	//aggiunge criterio ordinamento corrente
        model.addAttribute("currentSortDirection", sortDirection);	//aggiunge direzione ordinamento corrente
        return "authors";	//restituisce il template authors.html
    }
    
    @GetMapping("/author/{id}")
    public String author(@PathVariable("id") Long id, Model model) {
        Optional<Author> optionalAuthor = authorService.findById(id);	//cerca autore per id (potrebbe non esistere)
        if (optionalAuthor.isPresent()) {
        	// se l'autore è stato trovato restituisce author.html con autore aggiunto al model
            model.addAttribute("author", optionalAuthor.get());
            return "author";
        }
        // se autore non trovato redirect alla lista
        return "redirect:/authors";
    }
    
    @GetMapping("/authors/search")
    public String searchAuthors(@RequestParam(value = "search", required = false) String search,	//termine di ricerca (opzionale)
                               @RequestParam(name = "sort", required = false) String sort,	//criterio di ordinamento (opzionale)
                               @RequestParam(name = "sortDirection", required = false) String sortDirection,	//direzione ordinamento (opzionale)
                               Model model) {
        
        List<Author> searchResults;	//crea lista per contenere i risultati della ricerca
        
        if (search != null && !search.trim().isEmpty()) {
        	//se c'è un termine di ricerca esegue ricerca tramite service per nome, cognome o nazionalità
            searchResults = authorService.searchByNameSurnameOrNationality(search);
        } else {
        	//altrimenti prendi tutti gli autori e convertili in lista
            searchResults = StreamSupport.stream(authorService.findAll().spliterator(), false)
                                       .collect(Collectors.toList());
        }
        
        // Applica l'ordinamento ai risultati di ricerca
        if (sort != null) {
            switch (sort) {
                case "insertion":
                    // Ordinamento per ID (più recenti primi se desc, più vecchi primi se asc)
                    searchResults.sort(Comparator.comparing(Author::getId));
                    break;
                case "surname":
                    searchResults.sort(Comparator.comparing(Author::getSurname));
                    break;
                case "birthYear":
                    searchResults.sort(Comparator.comparing(author -> 
                        author.getBirthDate() != null ? author.getBirthDate().getYear() : 0));
                    break;
                case "books":
                    searchResults.sort(Comparator.comparingInt(author -> author.getBooks().size()));
                    break;
            }
            
            // Applica l'inversione se richiesta
            if ("desc".equals(sortDirection)) {
                Collections.reverse(searchResults);
            }
        } else {
            // Ordinamento predefinito per ID decrescente (più recenti primi)
            searchResults.sort(Comparator.comparing(Author::getId).reversed());
        }
        //aggiunge risultati ricerca al model e restiuisce stesso template della lista normale
        model.addAttribute("authors", searchResults);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentSortDirection", sortDirection);
        return "authors";
    }
}
