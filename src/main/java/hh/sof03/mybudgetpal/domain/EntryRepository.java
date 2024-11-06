package hh.sof03.mybudgetpal.domain;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EntryRepository extends MongoRepository<Entry, String> {
    List<Entry> findAllByUserId(String userId);
    Entry findByReferenceIdAndBankName(String referenceId, String bankName);
}
