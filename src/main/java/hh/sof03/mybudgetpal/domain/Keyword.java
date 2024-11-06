package hh.sof03.mybudgetpal.domain;

import jakarta.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "category")
public class Keyword {
  
  @Id
  @NotNull
  private String id;

  @NotNull
  private List<String> keywords;

  @NotNull
  private String category;

  @NotNull
  private KeywordType type;

  @NotNull
  @Field("user_id")
  private String userId;

  public Keyword() {}

  public Keyword(List<String> keywords, String category, KeywordType type, String userId) {
    this.keywords = keywords;
    this.category = category;
    this.type = type;
    this.userId = userId;
  }

  public String getId() {
    return id;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  public void setKeywords(List<String> keywords) {
    keywords.replaceAll(String::toLowerCase);
    this.keywords = keywords;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category.toLowerCase();
  }

  public KeywordType getType() {
    return type;
  }

  public void setType(KeywordType type) {
    this.type = type;
  }

  public String getKeywordsAsString() {
    return String.join(", ", keywords);
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public boolean doesKeywordExist(String keyword) {
    return this.keywords.contains(keyword.toLowerCase());
  }

}
