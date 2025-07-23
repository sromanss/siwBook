package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.service.BookService;
import it.uniroma3.siw.service.ReviewService;
import it.uniroma3.siw.service.CredentialsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired private ReviewService reviewService;
    @Autowired private BookService bookService;
    @Autowired private CredentialsService credentialsService;

    // Metodo helper semplificato per ottenere le credenziali
    private Credentials getCurrentUserCredentials(Authentication auth) {
        String username = null;
        
        if (auth.getPrincipal() instanceof UserDetails) {
            // Utente con registrazione normale
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            username = userDetails.getUsername();
        } else if (auth.getPrincipal() instanceof OAuth2User) {
            // Utente OAuth2 - usa email come username
            OAuth2User oauth2User = (OAuth2User) auth.getPrincipal();
            username = oauth2User.getAttribute("email");
            
            // Fallback per GitHub
            if (username == null) {
                String login = oauth2User.getAttribute("login");
                if (login != null) {
                    username = login + "@github.local";
                }
            }
        }
        
        if (username != null) {
            return credentialsService.findByUsername(username);
        }
        
        return null;
    }

    /* ========= FORM NUOVA RECENSIONE ========= */
    @GetMapping("/add/{bookId}")
    public String formNewReview(@PathVariable Long bookId, Model model) {
        Optional<Book> opt = bookService.findById(bookId);
        if (opt.isEmpty()) {
            return "redirect:/books?error=book_not_found";
        }
        //Ottieni info autenticazione corrente
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Credentials credentials = getCurrentUserCredentials(auth);
        
        if (credentials == null) {
            return "redirect:/login?error=credentials_not_found";
        }

        //Controlla se l'utente può fare recensione o se ne ha già una
        if (!reviewService.canUserReviewBook(opt.get(), credentials)) {
            return "redirect:/book/" + bookId + "?error=already_reviewed";
        }

        model.addAttribute("book", opt.get());
        model.addAttribute("review", new Review());
        return "formNewReview";
    }

    /* ========= SALVA RECENSIONE ========= */
    @PostMapping("/add/{bookId}")
    public String saveReview(@PathVariable Long bookId,
                             @Valid @ModelAttribute("review") Review review,
                             BindingResult br,
                             Model model) {

        Optional<Book> opt = bookService.findById(bookId);
        if (opt.isEmpty()) {
            return "redirect:/books?error=book_not_found";
        }
        
        Book book = opt.get();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Credentials credentials = getCurrentUserCredentials(auth);
        
        if (credentials == null) {
            return "redirect:/login?error=credentials_not_found";
        }

        if (br.hasErrors()) {
            model.addAttribute("book", book);
            return "formNewReview";
        }

        if (!reviewService.canUserReviewBook(book, credentials)) {
            return "redirect:/book/" + bookId + "?error=already_reviewed";
        }

        try {
            review.setBook(book);
            review.setCredentials(credentials);
            reviewService.save(review);
            return "redirect:/book/" + bookId + "?success=review_added";
        } catch (Exception e) {
            model.addAttribute("error", "Errore durante il salvataggio della recensione.");
            model.addAttribute("book", book);
            return "formNewReview";
        }
    }

    /* ========= MODIFICA RECENSIONE ========= */
    @GetMapping("/{id}/edit")
    public String formEditReview(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Review> reviewOpt = reviewService.findById(id);
        if (reviewOpt.isEmpty()) {
            return "redirect:/books?error=review_not_found";
        }

        Review review = reviewOpt.get();
        Credentials currentCredentials = getCurrentUserCredentials(authentication);
        
        if (currentCredentials == null) {
            return "redirect:/login?error=credentials_not_found";
        }

        // Solo il proprietario può modificare la recensione
        if (!review.getCredentials().getUsername().equals(currentCredentials.getUsername())) {
            return "redirect:/book/" + review.getBook().getId() + "?error=unauthorized";
        }

        model.addAttribute("book", review.getBook());
        model.addAttribute("review", review);
        return "formEditReview";
    }

    /* ========= SALVA MODIFICA RECENSIONE ========= */
    @PostMapping("/{id}/edit")
    public String updateReview(@PathVariable Long id,
                              @Valid @ModelAttribute("review") Review review,
                              BindingResult br,
                              Model model,
                              Authentication authentication) {

        Optional<Review> existingReviewOpt = reviewService.findById(id);
        if (existingReviewOpt.isEmpty()) {
            return "redirect:/books?error=review_not_found";
        }

        Review existingReview = existingReviewOpt.get();
        Credentials currentCredentials = getCurrentUserCredentials(authentication);
        
        if (currentCredentials == null) {
            return "redirect:/login?error=credentials_not_found";
        }

        // Solo il proprietario può modificare la recensione
        if (!existingReview.getCredentials().getUsername().equals(currentCredentials.getUsername())) {
            return "redirect:/book/" + existingReview.getBook().getId() + "?error=unauthorized";
        }

        if (br.hasErrors()) {
            model.addAttribute("book", existingReview.getBook());
            return "formEditReview";
        }

        try {
            // Aggiorna solo i campi modificabili
            existingReview.setTitle(review.getTitle());
            existingReview.setText(review.getText());
            existingReview.setRating(review.getRating());
            
            reviewService.save(existingReview);
            return "redirect:/book/" + existingReview.getBook().getId() + "?success=review_updated";
        } catch (Exception e) {
            model.addAttribute("error", "Errore durante l'aggiornamento della recensione.");
            model.addAttribute("book", existingReview.getBook());
            return "formEditReview";
        }
    }

    /* ========= CANCELLA RECENSIONE CON CONTROLLI ========= */
    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id, Authentication authentication) {
        
        try {
            Optional<Review> reviewOpt = reviewService.findById(id);
            if (reviewOpt.isEmpty()) {
                return "redirect:/books?error=review_not_found";
            }
            
            Review review = reviewOpt.get();
            Long bookId = review.getBook().getId();
            Credentials currentCredentials = getCurrentUserCredentials(authentication);
            
            if (currentCredentials == null) {
                return "redirect:/login?error=credentials_not_found";
            }
            
            // Verifica se l'utente è admin
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            // Verifica se l'utente è il proprietario della recensione
            boolean isOwner = review.getCredentials().getUsername().equals(currentCredentials.getUsername());
            
            // Controlla se l'utente può eliminare la recensione
            if (!isOwner && !isAdmin) {
                return "redirect:/book/" + bookId + "?error=unauthorized";
            }
            
            // Elimina la recensione
            reviewService.deleteById(id);
            
            // Redirect con messaggio di successo
            if (isAdmin && !isOwner) {
                return "redirect:/book/" + bookId + "?success=review_deleted_admin";
            } else {
                return "redirect:/book/" + bookId + "?success=review_deleted";
            }
            
        } catch (Exception e) {
            return "redirect:/books?error=delete_failed";
        }
    }
}
