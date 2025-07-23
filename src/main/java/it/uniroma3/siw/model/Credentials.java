package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "credentials")
public class Credentials {

    public static final String DEFAULT_ROLE = "USER";	//ruolo di default per utenti normali
    public static final String ADMIN_ROLE = "ADMIN";	//ruolo per amministratori
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Username è obbligatorio")
    @Size(min = 4, message = "Username deve avere almeno 4 caratteri")
    @Column(unique = true)	//vincolo di unicità a livello di db
    private String username;
    
    @Size(min = 3, message = "Password deve avere almeno 3 caratteri")
    private String password;
    
    @NotBlank
    private String role;

    @OneToOne(cascade = CascadeType.ALL) //relazione uno a uno con propagazione completa delle operazioni
    private User user;
    
    // Costruttori
    public Credentials() {
        this.role = DEFAULT_ROLE;
    }
    
    // Costruttore unico per tutti i tipi di utenti
    public Credentials(String username, String password, String role) {
        this.username = username;
        this.password = password; // può essere null per utenti OAuth
        this.role = role != null ? role : DEFAULT_ROLE; //usa ruolo fornito o default se nullo
    }
    
    // Metodi di utilità
    public boolean isOAuthUser() {
        return this.password == null || this.password.isEmpty();
    }
    
    // Getter e Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
