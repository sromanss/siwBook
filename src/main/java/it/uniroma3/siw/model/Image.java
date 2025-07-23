package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String fileName;
    //tipo del file immagine
    private String contentType;
    
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;
    
    // Costruttori
    public Image() {}
    
    public Image(String fileName, String contentType, Book book) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.book = book;
    }
    
    // Getter e setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Image image = (Image) obj;
        return Objects.equals(fileName, image.fileName) && Objects.equals(book, image.book);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fileName, book);
    }
}
