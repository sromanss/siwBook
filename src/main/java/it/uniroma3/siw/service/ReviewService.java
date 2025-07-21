package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Transactional
    public Review save(Review review) {
        return this.reviewRepository.save(review);
    }

    public Optional<Review> findById(Long id) {
        return this.reviewRepository.findById(id);
    }

    public List<Review> findByBook(Book book) {
        return this.reviewRepository.findByBook(book);
    }

    public List<Review> findByCredentials(Credentials credentials) {
        return this.reviewRepository.findByCredentials(credentials);
    }

    public Optional<Review> findByBookAndCredentials(Book book, Credentials credentials) {
        return this.reviewRepository.findByBookAndCredentials(book, credentials);
    }

    public boolean existsByBookAndCredentials(Book book, Credentials credentials) {
        return this.reviewRepository.existsByBookAndCredentials(book, credentials);
    }

    public boolean canUserReviewBook(Book book, Credentials credentials) {
        return !this.existsByBookAndCredentials(book, credentials);
    }

    @Transactional
    public void deleteById(Long id) {
        this.reviewRepository.deleteById(id);
    }

    public boolean isReviewOwner(Review review, Credentials credentials) {
        return review.getCredentials().equals(credentials);
    }
}
