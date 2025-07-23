package it.uniroma3.siw.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.CredentialsRepository;

@Service
public class CredentialsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CredentialsRepository credentialsRepository;

    @Autowired
    private UserService userService;

    // Metodo per trovare credenziali per ID
    @Transactional
    public Credentials getCredentials(Long id) {
        Optional<Credentials> result = this.credentialsRepository.findById(id);
        return result.orElse(null);
    }

    // Metodo principale per trovare per username (usato dall'AuthController)
    @Transactional
    public Credentials findByUsername(String username) {
        Optional<Credentials> result = this.credentialsRepository.findByUsername(username);
        return result.orElse(null);
    }

    // Mantiene compatibilità con codice esistente
    @Transactional
    public Credentials getCredentials(String username) {
        return findByUsername(username);
    }

    // Salvataggio semplice per utenti OAuth (senza encoding password)
    @Transactional
    public Credentials save(Credentials credentials) {
        // Se la password è null (utente OAuth), non encodare
        if (credentials.getPassword() != null && !credentials.getPassword().isEmpty()) {
            credentials.setPassword(this.passwordEncoder.encode(credentials.getPassword()));
        }
        return this.credentialsRepository.save(credentials);
    }

    // Salvataggio con encoding per registrazione normale
    @Transactional
    public Credentials saveCredentials(Credentials credentials) {
        credentials.setRole(Credentials.DEFAULT_ROLE);
        if (credentials.getPassword() != null && !credentials.getPassword().isEmpty()) {
            credentials.setPassword(this.passwordEncoder.encode(credentials.getPassword()));
        }
        return this.credentialsRepository.save(credentials);
    }
    
    @Transactional
    public Credentials saveCredentials(Credentials credentials, User user) {
        // Prima salva l'utente
        User savedUser = userService.save(user);
        
        // Poi associa l'utente alle credenziali
        credentials.setUser(savedUser);
        credentials.setRole(Credentials.DEFAULT_ROLE);
        
        // Encoda password solo se presente (non per OAuth)
        if (credentials.getPassword() != null && !credentials.getPassword().isEmpty()) {
            credentials.setPassword(this.passwordEncoder.encode(credentials.getPassword()));
        }
        
        return this.credentialsRepository.save(credentials);
    }
    
    @Transactional
    public Credentials saveAdminCredentials(Credentials credentials, User user) {
        // Prima salva l'utente
        User savedUser = userService.save(user);
        
        // Poi associa l'utente alle credenziali
        credentials.setUser(savedUser);
        credentials.setRole(Credentials.ADMIN_ROLE);
        
        // Encoda password solo se presente
        if (credentials.getPassword() != null && !credentials.getPassword().isEmpty()) {
            credentials.setPassword(this.passwordEncoder.encode(credentials.getPassword()));
        }
        
        return this.credentialsRepository.save(credentials);
    }
    
    public boolean existsByUsername(String username) {
        return this.credentialsRepository.existsByUsername(username);
    }

    public Iterable<Credentials> findAll() {
        return this.credentialsRepository.findAll();
    }

    public Iterable<Credentials> findByRole(String role) {
        return this.credentialsRepository.findByRole(role);
    }

    public long countByRole(String role) {
        return this.credentialsRepository.countByRole(role);
    }

    @Transactional
    public void deleteById(Long id) {
        this.credentialsRepository.deleteById(id);
    }
}
