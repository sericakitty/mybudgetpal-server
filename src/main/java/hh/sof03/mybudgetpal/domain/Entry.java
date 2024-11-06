package hh.sof03.mybudgetpal.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "entries")
public class Entry {

  @Id
  private String id;

  private LocalDate date;

  private BigDecimal amount;

  private String title;

  @Field("reference_id")
  private String referenceId;
  
  @Field("bank_name")
  private String bankName;
  
  @Field("user_id")
  private String userId;
  
  public Entry() {
  }

  public Entry(LocalDate date, BigDecimal amount, String title, String bankname, String referenceid, String userId) {
    this.date = date;
    this.amount = amount;
    this.title = title;
    this.bankName = bankname;
    this.referenceId = referenceid;
    this.userId = userId;
  }

  public String getId() {
    return id;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title.toLowerCase();
  }

  public String getBankName() {
    return bankName;
  }

  public void setBankName(String bankName) {
    this.bankName = bankName.toLowerCase();
  }

  public String getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId.toLowerCase();
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
    
} 
