package hh.sof03.mybudgetpal.controllers;

import hh.sof03.mybudgetpal.domain.Keyword;
import hh.sof03.mybudgetpal.domain.KeywordRepository;
import hh.sof03.mybudgetpal.domain.KeywordType;
import hh.sof03.mybudgetpal.domain.User;
import hh.sof03.mybudgetpal.payload.response.MessageResponse;
import hh.sof03.mybudgetpal.payload.request.KeywordRequest;
import hh.sof03.mybudgetpal.security.services.UserService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/user")
public class UserController {

  private static final Logger log = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private UserService userService;

  @Autowired
  private KeywordRepository keywordRepository;

  /**
   * Get all keywords for the user
   * 
   * @param request
   * @return ResponseEntity<Map<String, Object>> with the keywords
   */
  @GetMapping("/keywords")
  public ResponseEntity<?> keywords(HttpServletRequest request) {

    User user = userService.getUserFromRequest(request);
    if (user == null) {
      return ResponseEntity.badRequest().body(new MessageResponse("User not found or invalid token", "error"));
    }

    List<Keyword> includedKeywords = keywordRepository.findAllByUserIdAndType(user.getId(), KeywordType.INCLUDED);
    includedKeywords.forEach(keyword -> keyword.setUserId(null));

    List<Keyword> excludedKeywords = keywordRepository.findAllByUserIdAndType(user.getId(), KeywordType.EXCLUDED);
    excludedKeywords.forEach(keyword -> keyword.setUserId(null));

    Map<String, Object> keywords = new HashMap<>();
    keywords.put("includedKeywords", includedKeywords);
    keywords.put("excludedKeywords", excludedKeywords);

    return ResponseEntity.ok(keywords);
  }

  /**
   * Get a keyword by id
   * 
   * @param id
   * @param request
   * @return ResponseEntity<Keyword> with the keyword
   */
  @GetMapping("/keywords/edit/{id}")
  public ResponseEntity<?> editKeyword(@PathVariable String id, HttpServletRequest request) {
    try {

      User user = userService.getUserFromRequest(request);

      if (user == null) {
        return ResponseEntity.badRequest().body(new MessageResponse("User not found or invalid token", "error"));
      }

      Optional<Keyword> keywordOptional = keywordRepository.findById(id);

      if (!keywordOptional.isPresent()) {
        return ResponseEntity.badRequest().body(new MessageResponse("Keyword not found", "error"));
      }

      Keyword keyword = keywordOptional.get();
      if (!keyword.getUserId().equals(user.getId()) && !user.getRoles().contains("ROLE_ADMIN")) {
        return ResponseEntity.status(403)
            .body(new MessageResponse("You do not have permission to edit this keyword", "error"));
      }

      // Hide user information before sending response
      keyword.setUserId(null);

      return ResponseEntity.ok(keyword);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), "error"));
    }
  }

  /**
   * Update a keyword
   * 
   * @param id
   * @param updatedKeyword
   * @param request
   * @return ResponseEntity<MessageResponse> with the result
   */
  @PostMapping("/keywords/update/{id}")
  public ResponseEntity<MessageResponse> updateKeyword(@PathVariable String id, @RequestBody Keyword updatedKeyword,
      HttpServletRequest request) {

    User user = userService.getUserFromRequest(request);
    if (user == null) {
      return ResponseEntity.badRequest().body(new MessageResponse("User not found or invalid token", "error"));
    }

    Optional<Keyword> keywordOptional = keywordRepository.findById(id);
    if (!keywordOptional.isPresent()) {
      return ResponseEntity.badRequest().body(new MessageResponse("Keyword not found", "error"));
    }

    Keyword existingKeyword = keywordOptional.get();

    if (!existingKeyword.getUserId().equals(user.getId()) && !user.getRoles().contains("ROLE_ADMIN")) {
      return ResponseEntity.status(403)
          .body(new MessageResponse("You do not have permission to update this keyword", "error"));
    }

    // Update keyword details
    existingKeyword.setCategory(updatedKeyword.getCategory());
    existingKeyword.setKeywords(updatedKeyword.getKeywords());
    existingKeyword.setType(updatedKeyword.getType());

    keywordRepository.save(existingKeyword);

    return ResponseEntity.ok(new MessageResponse("Keyword updated successfully", "succes"));
  }

  /**
   * Add a keyword for the user
   * 
   * @param keyword
   * @param request
   * @return ResponseEntity<?> with the keyword
   */
  @PostMapping("/keywords/add")
  public ResponseEntity<?> addKeyword(@Valid @RequestBody KeywordRequest keyword, HttpServletRequest request) {

    User user = userService.getUserFromRequest(request);
    if (user == null) {
      return ResponseEntity.badRequest().body("User not found or invalid token");
    }


    if (keyword.getType().equals(KeywordType.EXCLUDED) && !keyword.getCategory().toLowerCase().equals("excluded")) {
      return ResponseEntity.badRequest().body(new MessageResponse("Excluded keywords must have category 'excluded'", "error"));
    }

    Keyword newKeyword = new Keyword(keyword.getKeywords(), keyword.getCategory(), keyword.getType(), user.getId());
    keywordRepository.save(newKeyword);

    return ResponseEntity.ok(newKeyword);
  }

  /**
   * Delete a keyword by id
   * 
   * @param id
   * @param request
   * @return ResponseEntity<MessageResponse> with the result
   */
  @GetMapping("/keywords/delete/{id}")
  public ResponseEntity<MessageResponse> deleteKeyword(@PathVariable String id, HttpServletRequest request) {

    User user = userService.getUserFromRequest(request);
    if (user == null) {

      return ResponseEntity.badRequest().body(new MessageResponse("User not found or invalid token", "error"));
    }

    Optional<Keyword> keywordOptional = keywordRepository.findById(id);

    if (!keywordOptional.isPresent()) {

      return ResponseEntity.badRequest().body(new MessageResponse("Keyword not found", "error"));
    }

    Keyword keyword = keywordOptional.get();

    // Check if the authenticated user is the owner of the keyword or has the role
    // of ADMIN
    if (keyword.getUserId().equals(user.getId()) || user.getRoles().contains("ROLE_ADMIN")) {
      keywordRepository.deleteById(id);

      return ResponseEntity.ok(new MessageResponse("Keyword deleted successfully", "succes"));
    }

    return ResponseEntity.status(403)
        .body(new MessageResponse("You do not have permission to delete this keyword", "error"));

  }
}