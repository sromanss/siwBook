package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Il nome è obbligatorio")
    private String name;

    @NotBlank(message = "L'e-mail è obbligatoria")
    @Email(message = "Formato e-mail non valido")
    @Column(unique = true)
    private String email;

    // Campo opzionale per identificare il provider OAuth (google, github, local)
    private String provider;

    /* costruttori */
    public User() { 
        this.provider = "local"; // default per registrazione normale
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.provider = "local";
    }

    // Costruttore per utenti OAuth
    public User(String name, String email, String provider) {
        this.name = name;
        this.email = email;
        this.provider = provider;
    }

    /* getter & setter */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    // Metodo di utilità
    public boolean isOAuthUser() {
        return !"local".equals(this.provider);
    }
}
