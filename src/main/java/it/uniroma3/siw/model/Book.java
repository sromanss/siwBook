package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.*;

@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    private String title;
    
    @NotNull
    @Min(0)
    @Max(2025)
    private Integer year;
    
    private String photoFileName;
    
    @ManyToMany(fetch = FetchType.EAGER)	//relazione molti a molti con caricamento immediato
    @JoinTable(name = "book_author",	//nome della tabella di join
               joinColumns = @JoinColumn(name = "book_id"),	//colonna che punta a questo libro
               inverseJoinColumns = @JoinColumn(name = "author_id"))	//colonna che punta all'autore
    private Set<Author> authors = new HashSet<>();	//set degli autori di questo libro
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)	//uno a molti con gestione cascade completa (se un'immagine viene rimossa, viene eliminata dal db)
    private List<Image> images = new ArrayList<>();
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();
    
    // Costruttori, getter e setter
    public Book() {}
    
    public Book(String title, Integer year) {
        this.title = title;
        this.year = year;
    }
    
    // Getter e setter completi
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public Set<Author> getAuthors() { return authors; }
    public void setAuthors(Set<Author> authors) { this.authors = authors; }
    
    public List<Image> getImages() { return images; }
    public void setImages(List<Image> images) { this.images = images; }
    
    public String getPhotoFileName() { return photoFileName; }
    public void setPhotoFileName(String photoFileName) { this.photoFileName = photoFileName; }
    
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    // Metodi helper per le statistiche recensioni
    public double getAverageRating() {
        if (reviews.isEmpty()) {
            return 0.0;
        } //somma tutti i rating delle recensioni e dividi per la size
        double sum = reviews.stream().mapToInt(Review::getRating).sum();
        return sum / reviews.size();
    }

    public int getTotalReviews() {
        return reviews.size();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Book book = (Book) obj;
        return Objects.equals(title, book.title) && Objects.equals(year, book.year);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, year);
    }
}
