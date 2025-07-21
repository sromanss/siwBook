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
    public String authors(@RequestParam(name = "sort", required = false) String sort,
                         @RequestParam(name = "sortDirection", required = false) String sortDirection,
                         Model model) {
        
        // Recupera tutti gli autori
        Iterable<Author> allAuthors = authorService.findAll();
        
        // Converti in List per l'ordinamento
        List<Author> authorList = StreamSupport.stream(allAuthors.spliterator(), false)
                                              .collect(Collectors.toList());
        
        // Applica l'ordinamento in base al parametro
        if (sort != null) {
            switch (sort) {
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
        
        model.addAttribute("authors", authorList);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentSortDirection", sortDirection);
        return "authors";
    }

    @GetMapping("/author/{id}")
    public String author(@PathVariable("id") Long id, Model model) {
        Optional<Author> optionalAuthor = authorService.findById(id);
        if (optionalAuthor.isPresent()) {
            model.addAttribute("author", optionalAuthor.get());
            return "author";
        }
        return "redirect:/authors";
    }

    @GetMapping("/authors/search")
    public String searchAuthors(@RequestParam(value = "search", required = false) String search,
                               @RequestParam(name = "sort", required = false) String sort,
                               @RequestParam(name = "sortDirection", required = false) String sortDirection,
                               Model model) {
        
        List<Author> searchResults;
        
        if (search != null && !search.trim().isEmpty()) {
            searchResults = authorService.searchByNameSurnameOrNationality(search);
        } else {
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
        
        model.addAttribute("authors", searchResults);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentSortDirection", sortDirection);
        return "authors";
    }
}
