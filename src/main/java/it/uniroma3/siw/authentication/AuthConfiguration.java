package it.uniroma3.siw.authentication;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration				
@EnableWebSecurity
public class AuthConfiguration implements WebMvcConfigurer {

    @Value("${app.upload.dir}")			// legge il valore da application.properties
    private String uploadDir;			

    @Autowired
    private DataSource dataSource;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
            .dataSource(dataSource)
            // Query per recuperare i ruoli dell'utente (aggiunge prefisso "ROLE_"
            .authoritiesByUsernameQuery("SELECT username, CONCAT('ROLE_', role) as authority FROM credentials WHERE username=?")
            // Query per recuperare username, password e stato abilitazione dell'utente
            .usersByUsernameQuery("SELECT username, password, 1 as enabled FROM credentials WHERE username=?");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    // metodo che configura le regole di sicurezza http (chi può acccedere ai vari link)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .authorizeHttpRequests(auth -> auth
            // Permette l'accesso pubblico (senza autenticazione) alle pagine principali e risorse statiche
                .requestMatchers(HttpMethod.GET, "/", "/index", "/login", "/register", "/success", "/css/**", "/images/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/books", "/books/**", "/books/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/authors/**", "/book/**", "/author/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/login", "/register").permitAll()
            // Richiede il ruolo admin per accedere alla sezione admin (sia per accedere sia per inviare dati)
                .requestMatchers(HttpMethod.GET, "/admin/**").hasRole("ADMIN") // Spirng aggiunge automaticamente ROLE_, quindi cercherà ROLE_ADMIN
                .requestMatchers(HttpMethod.POST, "/admin/**").hasRole("ADMIN")
            // Richiede il ruolo user per gestire recensioni
                .requestMatchers("/reviews/add/**", "/reviews/delete/**", "/reviews/*/edit")
                .hasAnyAuthority("ROLE_USER", "OIDC_USER", "OAUTH2_USER")		// A differenza di hasRole, qui cerca quello inserito tra ""
                .anyRequest().authenticated())
            .formLogin(form -> form // Configura il login tramite form
                .loginPage("/login")	// Specifica la pagina di login
                .permitAll()	// Permette a tutti di accedere alla pagina di login
                .defaultSuccessUrl("/success", true)	// Redirect in caso di successo
                .failureUrl("/login?error=true"))	// Redirect in caso di errore 
            .oauth2Login(oauth2 -> oauth2	// Configura il login tramite oAuth
                .loginPage("/login")	// Specifica la pagina di login
                .defaultSuccessUrl("/success", true))
            .logout(logout -> logout	// Configura il processo di logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")	// Redirect dopo logout avvenuto con successo
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")	// Cancella i cookie di sessione
                .clearAuthentication(true)
                .permitAll());	// Permette a tutti di fare logout
        
        return http.build();
    }

    @Override
    // Metodo per configurare la gestione delle risorse statiche
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	// Specifica il pattern url per le immagini e dove trovare i file immagine
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
