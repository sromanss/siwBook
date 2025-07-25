package it.uniroma3.siw.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CredentialsService;

@Component // Dice a spring di registrare questa classe come Bean
public class DataInitializer implements ApplicationRunner {

    @Autowired // Spring inietterà automaticamente un'istanza di CredentialsService
    private CredentialsService credentialsService;

    @Override
    // Metodo eseguito automaticamente da Spring all'avvio dell'applicazione
    public void run(ApplicationArguments args) throws Exception {
        // Verifica se l'admin esiste già
        if (credentialsService.getCredentials("admin") == null) {
            // Crea l'utente admin
            User adminUser = new User();
            adminUser.setName("Amministratore");
            adminUser.setEmail("admin@siwlibrary.com");

            // Crea le credenziali admin
            Credentials adminCredentials = new Credentials();
            adminCredentials.setUsername("admin");
            adminCredentials.setPassword("admin"); 
            adminCredentials.setRole(Credentials.ADMIN_ROLE);

            // Associa l'utente alle credenziali (relazione OneToOne)
            adminCredentials.setUser(adminUser);

            // Salva usando il metodo specifico per admin
            credentialsService.saveAdminCredentials(adminCredentials, adminUser);
            
            // Righe di debug
            System.out.println("✅ Utente amministratore creato con successo!");
            System.out.println("Username: admin");
            System.out.println("Password: admin");
        } else {
            System.out.println("ℹ️ Utente amministratore già esistente.");
        }
        
        // Crea utente di test per debugging
        // Controlla se esiste già un utente di nome mario
        if (credentialsService.getCredentials("mario") == null) {
            // Crea l'utente di test
            User testUser = new User();
            testUser.setName("Mario Rossi");
            testUser.setEmail("mario.rossi@email.com");

            // Crea le credenziali di test
            Credentials testCredentials = new Credentials();
            testCredentials.setUsername("mario");
            testCredentials.setPassword("admin");
            testCredentials.setRole(Credentials.DEFAULT_ROLE);

            // Associa l'utente alle credenziali
            testCredentials.setUser(testUser);

            // Salva usando il metodo per utenti normali
            credentialsService.saveCredentials(testCredentials, testUser);
            // Righe di debug
            System.out.println("✅ Utente di test creato!");
            System.out.println("Username: mario");
            System.out.println("Password: admin");
        } else {
            System.out.println("ℹ️ Utente di test già esistente.");
        }
    }
}
