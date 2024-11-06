package hh.sof03.mybudgetpal.domain;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailVerificationToken(String emailVerificationToken);
    Optional<User> findByEmailAndPasswordResetToken(String email, String passwordResetToken);
    Optional<User> findByEmailAndEmailVerificationToken(String email, String emailVerificationToken);
}
