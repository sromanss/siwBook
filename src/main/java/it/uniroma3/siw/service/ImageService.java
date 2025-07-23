package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.repository.ImageRepository;
import it.uniroma3.siw.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private BookRepository bookRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;	//directory dove salvare fisicamente i file immagine

    @Transactional
    public String saveImage(MultipartFile file) throws IOException {
        // Genera nome file unico
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        // Crea directory se non esiste
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Salva file su disco
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        
        return uniqueFileName;
    }

    @Transactional
    public Image saveImage(Long bookId, MultipartFile file) throws IOException {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isEmpty()) { //controlla se il libro esiste
            throw new IllegalArgumentException("Book not found");
        }
        // se esiste chiama il metodo precedente per salvare il file su disco
        Book book = optionalBook.get();
        String fileName = saveImage(file);

        // Salva metadati nel database
        Image image = new Image(fileName, file.getContentType(), book);
        return this.imageRepository.save(image);	//salva nel database
    }

    public boolean isValidImageFile(MultipartFile file) {
        if (file.isEmpty()) return false;
        
        String contentType = file.getContentType();
        return contentType != null && 
               (contentType.equals("image/jpeg") || 
                contentType.equals("image/jpg") || 
                contentType.equals("image/png"));
    }

    public void deleteImage(String fileName) {
        try {
        	//costruisce percroso completo del file e lo elimina se esiste, altrimenti errore
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log dell'errore, ma non bloccare l'operazione
            System.err.println("Errore nell'eliminazione del file: " + fileName);
        }
    }

    public Optional<Image> findById(Long id) {
        return this.imageRepository.findById(id);
    }

    public List<Image> findByBook(Book book) {
        return this.imageRepository.findByBook(book);
    }

    @Transactional
    public void deleteById(Long id) {
    	//cerca l'immagine. Se esiste prima elimina il file fisico poi i dati dal database
        Optional<Image> optionalImage = this.imageRepository.findById(id);
        if (optionalImage.isPresent()) {
            Image image = optionalImage.get();
            deleteImage(image.getFileName());
            this.imageRepository.deleteById(id);
        }
    }

    @Transactional
    public void deleteImage(Long imageId) {
        deleteById(imageId);
    }
}
