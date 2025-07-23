package it.uniroma3.siw.service;

import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // RIMOSSA la dipendenza da CredentialsService per evitare ciclo

    @Transactional
    public User save(User user) {
        return this.userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return this.userRepository.findById(id);
    }

    public Iterable<User> findAll() {
        return this.userRepository.findAll();
    }

    public boolean existsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public Optional<User> findByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    @Transactional
    public void deleteById(Long id) {
        this.userRepository.deleteById(id);
    }

    @Transactional
    public User findOrCreateOAuthUser(String email, String name) {
        
        // Controlla se esiste già un utente con questa email
        Optional<User> existingUser = findByEmail(email);
        
        if (existingUser.isPresent()) {
           
            return existingUser.get();
        }
        
        // Crea solo il nuovo utente OAuth (le credenziali verranno create altrove)
        User newUser = new User(name, email, "oauth2");
        User savedUser = save(newUser);
        
        return savedUser;
    }
}
