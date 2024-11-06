package hh.sof03.mybudgetpal.domain;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface KeywordRepository extends MongoRepository<Keyword, String> {
    List<Keyword> findAllByUserIdAndType(String userId, KeywordType type);
    Keyword findByIdAndUserId(String id, String userId);
    List<Keyword> findAllByUserId(String userId);
}
