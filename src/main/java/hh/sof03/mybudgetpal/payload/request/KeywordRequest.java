package hh.sof03.mybudgetpal.payload.request;

import java.util.List;

import hh.sof03.mybudgetpal.domain.KeywordType;
import jakarta.validation.constraints.NotEmpty;


public class KeywordRequest {

  @NotEmpty(message = "Category cannot be empty")
  private String category;

  @NotEmpty(message = "Keywords cannot be empty")
  private List<String> keywords;

  private KeywordType type;

  public KeywordRequest() {
  }

  public KeywordRequest(String category, List<String> keywords, KeywordType type) {
    this.category = category;
    this.keywords = keywords;
    this.type = type;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  public void setKeywords(List<String> keywords) {
    this.keywords = keywords;
  }

  public KeywordType getType() {
    return type;
  }

  public void setType(KeywordType type) {
    this.type = type;
  }
}
