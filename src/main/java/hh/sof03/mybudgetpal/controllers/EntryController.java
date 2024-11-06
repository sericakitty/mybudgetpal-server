package hh.sof03.mybudgetpal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import hh.sof03.mybudgetpal.domain.User;
import hh.sof03.mybudgetpal.payload.response.MessageResponse;
import hh.sof03.mybudgetpal.security.services.UserService;
import hh.sof03.mybudgetpal.domain.Entry;
import hh.sof03.mybudgetpal.domain.EntryRepository;
import hh.sof03.mybudgetpal.domain.Keyword;
import hh.sof03.mybudgetpal.domain.KeywordRepository;
import hh.sof03.mybudgetpal.domain.KeywordType;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class EntryController {

  private static final Logger log = LoggerFactory.getLogger(EntryController.class);

  @Autowired
  private EntryRepository entryRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private KeywordRepository keywordRepository;

  /**
   * Get all entries for the user
   * 
   * @param request
   * @return List of entries
   */
  @GetMapping("/entries")
  public ResponseEntity<List<Entry>> getEntries(HttpServletRequest request) throws IOException {
    try {

      User user = userService.getUserFromRequest(request);
      if (user == null) {
        return ResponseEntity.badRequest().body(null);
      }

      List<Entry> entries = entryRepository.findAllByUserId(user.getId());
      return ResponseEntity.ok(entries);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(null);
    }
  }

  /**
   * Add a new entry for the user
   * 
   * @param entry
   * @param request includes the user's token
   * @return MessageResponse with success or error message
   */
  @GetMapping("/entries/delete/{id}")
  public ResponseEntity<MessageResponse> deleteEntry(@PathVariable String id, HttpServletRequest request) {
    try {

      User user = userService.getUserFromRequest(request);
      if (user == null) {
        return ResponseEntity.badRequest().body(new MessageResponse("User not found or invalid token", "error"));
      }

      entryRepository.deleteById(id);
      return ResponseEntity.ok().body(new MessageResponse("Entry deleted successfully", "success"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new MessageResponse("Entry not found", "error"));
    }
  }

  /**
   * Add a new entry for the user
   *
   * @param entry
   * @param request includes the user's token
   * @return MessageResponse with success or error message
   */
  @PostMapping("/entries/import-data")
  public ResponseEntity<?> importFiles(@RequestParam("file") MultipartFile[] files, HttpServletRequest request) {
    try {

      User user = userService.getUserFromRequest(request);
      if (user == null) {
        return ResponseEntity.badRequest().body(new MessageResponse("User not found or invalid token", "error"));
      }

      if (files.length == 0) {
        return ResponseEntity.badRequest()
            .body(new MessageResponse("Please select at least one file to upload.", "error"));
      }

      boolean hasInvalidFiles = false;

      for (MultipartFile file : files) {
        if (!file.getContentType().equals("text/csv")) {
          hasInvalidFiles = true;
          files = Arrays.stream(files).filter(f -> !f.equals(file)).toArray(MultipartFile[]::new);
        }
      }

      if (hasInvalidFiles) {
        return ResponseEntity.badRequest()
            .body(new MessageResponse("Only CSV file allowed. Please try importing again.", "error"));
      }

      List<String> uploadedFiles = new ArrayList<>();
      List<String> failedFiles = new ArrayList<>();

      for (MultipartFile file : files) {
        try {
          boolean isStatementFile = processStatementFile(file, user);
          if (isStatementFile) {
            uploadedFiles.add(file.getOriginalFilename());
          } else {
            failedFiles.add(file.getOriginalFilename());
          }
        } catch (Exception e) {
          failedFiles.add(file.getOriginalFilename());
          e.printStackTrace();
        }
      }

      Map<String, List<String>> result = new HashMap<>();
      result.put("uploadedFiles", uploadedFiles);
      result.put("failedFiles", failedFiles);

      return ResponseEntity.ok().body(result);

    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error importing files", "error"));
    }
  }

  /**
   * Process the statement file and save the entries to the database
   *
   * @param file
   * @param user
   * @return true if the file was processed successfully, false otherwise
   */
  private boolean processStatementFile(MultipartFile file, User user) throws IOException {
    String bankName = determineBankName(file, user);
    log.info("Bank name is this: " + bankName);
    if (bankName.isEmpty()) {
      return false;
    }

    if (file.isEmpty()) {
      return false;
    }

    // Fetch excluded keywords for the user
    List<Keyword> excludedKeywords = keywordRepository.findAllByUserIdAndType(user.getId(), KeywordType.EXCLUDED);

    try (Scanner scanner = new Scanner(file.getInputStream())) {
      String[] headers = scanner.nextLine().split(";");
      Map<String, Integer> columnIndex = mapColumnIndexes(headers, bankName);
      log.info("Column indexes: " + columnIndex);
      while (scanner.hasNext()) {
        String line = scanner.nextLine();
        String[] columns = line.split(";");
        String date = columns[columnIndex.get("date")].replace(".",
            "-").replace("\"", "");
        LocalDate formattedDate = parseDate(date, bankName);
        BigDecimal amount = new BigDecimal(columns[columnIndex.get("amount")].replace(",", "."));
        String title = columns[columnIndex.get("title")].toLowerCase().replace("\"",
            "");
        String referenceId = columns[columnIndex.get("referenceId")].replace("\"",
            "");
        bankName = bankName.toLowerCase();

        title = title.replaceAll("\\s{2,}", " ");

        if (entryRepository.findByReferenceIdAndBankName(referenceId, bankName) != null) {
          continue;
        }

        if (containsExcludedKeyword(title, excludedKeywords)) {
          continue;
        }

        Entry newEntry = new Entry(formattedDate, amount, title, bankName, referenceId, user.getId());
                entryRepository.save(newEntry);
      }

      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Parse the date based on the bank name
   *
   * @param date
   * @param bankName
   * @return LocalDate object
   */
  private LocalDate parseDate(String date, String bankName) {
    switch (bankName) {
      case "s-pankki":
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
      case "op-pankki":
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      default:
        break;
    }
    return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
  }

  /**
   * Determine the bank name based on the file content
   *
   * @param file
   * @param user
   * @return bank name
   */
  private String determineBankName(MultipartFile file, User user) {
    String bankName = "";
    String filename = file.getOriginalFilename();
    bankName = getBankNameFromFileName(filename);

    if (bankName.isEmpty()) {
      try (Scanner scanner = new Scanner(file.getInputStream())) {
        String[] headers = scanner.nextLine().split(";");
        Map<String, Integer> columnIndex = mapColumnIndexes(headers, bankName);

        while (scanner.hasNext()) {
          String line = scanner.nextLine();
          String[] columns = line.split(";");

          String title = columns[columnIndex.get("title")].toLowerCase().replace("\"",
              "");
          BigDecimal amount = new BigDecimal(columns[columnIndex.get("amount")].replace(",", "."));

          if ((title.contains(user.getFirstName().toLowerCase() + " " +
              user.getLastName().toLowerCase())
              || title.contains(user.getLastName().toLowerCase() + " " +
                  user.getFirstName().toLowerCase()))
              && amount.compareTo(BigDecimal.ZERO) >= 0) {
            log.info("Bank name found from BIC" + title);
            log.info("line" + line);
            bankName = BicToBankName(columns[columnIndex.get("bic")]);
            log.info("Bank name found from BIC is :" + bankName);
            if (!bankName.isEmpty()) {
              return bankName;
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return bankName;
  }

  /**
   * Map the column indexes based on the headers
   *
   * @param headers
   * @param bankName
   * @return Map of column indexes
   */
  private Map<String, Integer> mapColumnIndexes(String[] headers, String bankName) {
    Map<String, Integer> columnIndex = new HashMap<>();

    for (int i = 0; i < headers.length; i++) {
      String header = headers[i].trim().toLowerCase();
      if (header.contains("kirjauspäivä") || header.contains("päivämäärä")) {
        columnIndex.put("date", i);
      } else if (header.contains("summa") || header.contains("määrä")) {
        columnIndex.put("amount", i);
      } else if (header.contains("saaja/maksaja") || header.equals("saajan nimi")) {
        columnIndex.put("title", i);
      } else if (header.contains("arkistointitunnus")) {
        columnIndex.put("referenceId", i);
      } else if (header.contains("bic")) {
        log.info("BIC found at index: " + i);
        columnIndex.put("bic", i);
      }
    }

    return columnIndex;
  }

  /**
   * Convert BIC code to bank name
   *
   * @param bic
   * @return bank name
   */
  private String BicToBankName(String bic) {
    switch (bic.toLowerCase()) {
      case "sbanfihh":
        return "s-pankki";
      case "okoyfihh":
        return "op-pankki";
      default:
        break;
    }
    return "";
  }

  /**
   * Get the bank name from the file name
   *
   * @param filename
   * @return bank name
   */
  private String getBankNameFromFileName(String filename) {
    if (filename.toLowerCase().contains("spankki") ||
        filename.toLowerCase().contains("s-pankki")) {
      return "s-pankki";
    } else if (filename.toLowerCase().contains("oppankki") ||
        filename.toLowerCase().contains("op")) {
      return "op-pankki";
    }
    return "";
  }

  /**
   * Check if the title contains any excluded keywords
   *
   * @param title
   * @param excludedKeywords
   * @return true if the title contains excluded keywords, false otherwise
   */
  public boolean containsExcludedKeyword(String title, List<Keyword> excludedKeywords) {
    if (title == null || excludedKeywords == null) {
      return false;
    }

    String lowerCaseTitle = title.toLowerCase();

    for (Keyword keyword : excludedKeywords) {
      String lowerCaseKeyword = keyword.getKeywords().get(0).toLowerCase();

      if (lowerCaseTitle.contains(lowerCaseKeyword)) {
        return true;
      }
    }

    return false;
  }

}
