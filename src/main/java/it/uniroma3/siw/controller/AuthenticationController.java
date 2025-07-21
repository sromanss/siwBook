package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.UserService;
import jakarta.validation.Valid;

@Controller
public class AuthenticationController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CredentialsService credentialsService;
    
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        System.out.println("DEBUG: Accesso alla pagina di registrazione");
        model.addAttribute("user", new User());
        model.addAttribute("credentials", new Credentials());
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                              @Valid @ModelAttribute("credentials") Credentials credentials,
                              BindingResult bindingResult,
                              @RequestParam("confirmPassword") String confirmPassword,
                              Model model) {

        System.out.println("=== DEBUG REGISTRAZIONE ===");
        System.out.println("Username: " + credentials.getUsername());
        System.out.println("Nome: " + user.getName());
        System.out.println("Email: " + user.getEmail());

        if (bindingResult.hasErrors()) {
            System.out.println("DEBUG: Errori di validazione trovati");
            model.addAttribute("errorMessage", "Errori di validazione nei campi del form.");
            return "register";
        }

        if (!credentials.getPassword().equals(confirmPassword)) {
            System.out.println("DEBUG: Password non coincidono");
            model.addAttribute("errorMessage", "Le password non coincidono.");
            return "register";
        }

        if (credentialsService.findByUsername(credentials.getUsername()) != null) {
            System.out.println("DEBUG: Username già esistente: " + credentials.getUsername());
            model.addAttribute("errorMessage", "Username già esistente");
            return "register";
        }
        
        if (userService.existsByEmail(user.getEmail())) {
            System.out.println("DEBUG: Email già esistente: " + user.getEmail());
            model.addAttribute("errorMessage", "Email già in uso, scegline un'altra.");
            return "register";
        }

        try {
            user = userService.save(user);
            credentials.setUser(user);
            credentialsService.save(credentials);
            System.out.println("DEBUG: Registrazione completata con successo!");
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            System.err.println("DEBUG: ERRORE durante la registrazione: " + e.getMessage());
            model.addAttribute("errorMessage", "Errore durante la registrazione: " + e.getMessage());
            return "register";
        }
    }
    
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
    
    @GetMapping("/success")
    public String defaultAfterLogin(Authentication authentication) {
        if (authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        System.out.println("DEBUG Success: Authentication type=" + authentication.getClass().getSimpleName());
        System.out.println("DEBUG Success: Authorities=" + authentication.getAuthorities());

        // Gestione utenti OAuth
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();
            String email = oauthToken.getPrincipal().getAttribute("email");
            String name = oauthToken.getPrincipal().getAttribute("name");
            
            System.out.println("DEBUG OAuth: Provider=" + registrationId);
            System.out.println("DEBUG OAuth: Email=" + email);
            System.out.println("DEBUG OAuth: Name=" + name);
            
            // Gestione specifica per GitHub
            if ("github".equals(registrationId)) {
                String login = oauthToken.getPrincipal().getAttribute("login");
                
                if (email == null && login != null) {
                    email = login + "@github.local";
                }
                
                if (name == null && login != null) {
                    name = login;
                }
            }
            
            // Gestione per Google
            if ("google".equals(registrationId)) {
                if (name == null) {
                    name = "Google User";
                }
            }
            
            if (email == null) {
                System.err.println("ERRORE: Impossibile ottenere email dal provider OAuth: " + registrationId);
                return "redirect:/login?error=oauth";
            }
            
            try {
                // Crea o trova l'utente OAuth
                User user = userService.findOrCreateOAuthUser(email, name);
                
                // ✅ RIPRISTINO: Crea sempre credenziali con ruolo USER per compatibilità
                Credentials existingCredentials = credentialsService.findByUsername(email);
                if (existingCredentials == null) {
                    Credentials credentials = new Credentials(email, null, "USER");
                    credentials.setUser(user);
                    credentialsService.save(credentials);
                    System.out.println("DEBUG: Credenziali OAuth create per: " + email + " con ruolo: USER");
                } else {
                    System.out.println("DEBUG: Credenziali OAuth esistenti per: " + email + " con ruolo: " + existingCredentials.getRole());
                }
                
                System.out.println("DEBUG: Utente OAuth gestito con successo: " + user.getEmail());
                return "redirect:/";
                
            } catch (Exception e) {
                System.err.println("ERRORE durante la gestione dell'utente OAuth: " + e.getMessage());
                e.printStackTrace();
                return "redirect:/login?error=oauth";
            }
        }

        // Gestione utenti tradizionali
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
        
        System.out.println("DEBUG: Login tradizionale - isAdmin=" + isAdmin);
        return isAdmin ? "redirect:/admin" : "redirect:/";
    }
}
