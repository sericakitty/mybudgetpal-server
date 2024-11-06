
package hh.sof03.mybudgetpal.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import hh.sof03.mybudgetpal.domain.Entry;
import hh.sof03.mybudgetpal.domain.EntryRepository;
import hh.sof03.mybudgetpal.domain.KeywordRepository;
import hh.sof03.mybudgetpal.domain.Keyword;
import hh.sof03.mybudgetpal.domain.KeywordType;
import hh.sof03.mybudgetpal.domain.User;
import hh.sof03.mybudgetpal.security.services.UserService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class StatisticController {

    private static final Logger log = LoggerFactory.getLogger(EntryController.class);

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private UserService userService;

    /**
     * Get statistics for the user
     * 
     * @param request
     * @return ResponseEntity<Map<String, Object>> with the statistics
     */
   @GetMapping("/api/statistics")
    public ResponseEntity<Map<String, Object>> stats(HttpServletRequest request) {
        User user = userService.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.status(401).body(null);
        }

        List<Entry> entries = entryRepository.findAllByUserId(user.getId());
        List<Keyword> includedKeywords = keywordRepository.findAllByUserIdAndType(user.getId(), KeywordType.INCLUDED);

        Map<String, Double> categoryExpenses = new HashMap<>();
        Map<String, Map<String, Double>> monthlyExpenses = new HashMap<>();
        double balance = 0.0;

        // Initialize all categories in the map
        for (Keyword keyword : includedKeywords) {
            categoryExpenses.put(keyword.getCategory(), 0.0);
        }

        // Calculate expenses for each category
        for (Entry entry : entries) {
            log.info("Entry: " + entry.getTitle());
            List<String> categories = getCategories(entry.getTitle(), includedKeywords, user);
            double amount = entry.getAmount().doubleValue();
            balance += amount;

            if (categories.isEmpty()) {
                // If no category matches, add to Other Expenses or Other Income
                String otherCategory = amount < 0 ? "Other Expenses" : "Other Income";
                categoryExpenses.put(otherCategory, categoryExpenses.getOrDefault(otherCategory, 0.0) + amount);
            } else {
                // Add amount to the matched category
                String category = categories.get(0);
                categoryExpenses.put(category, categoryExpenses.get(category) + amount);
            }

            // Monthly expenses
            String month = entry.getDate().getMonth().toString();
            monthlyExpenses.putIfAbsent(month, new HashMap<>());
            if (categories.isEmpty()) {
                String otherCategory = amount < 0 ? "Other Expenses" : "Other Income";
                monthlyExpenses.get(month).put(otherCategory, monthlyExpenses.get(month).getOrDefault(otherCategory, 0.0) + amount);
            } else {
                String category = categories.get(0);
                monthlyExpenses.get(month).put(category, monthlyExpenses.get(month).getOrDefault(category, 0.0) + amount);
            }
        }

        // Remove categories with zero values
        categoryExpenses = categoryExpenses.entrySet()
            .stream()
            .filter(entry -> entry.getValue() != 0)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Round all values
        categoryExpenses.replaceAll((k, v) -> Math.round(v * 100.0) / 100.0);
        monthlyExpenses.forEach((month, categories) -> categories.replaceAll((k, v) -> Math.round(v * 100.0) / 100.0));

        Map<String, Object> response = new HashMap<>();
        response.put("categoryExpenses", categoryExpenses);
        response.put("monthlyExpenses", monthlyExpenses);
        response.put("balance", Math.round(balance * 100.0) / 100.0);

        return ResponseEntity.ok(response);
    }

    /**
     * Get suitable categories for the keyword
     * 
     * @param keyword
     * @param allKeywords
     * @param user
     * @return List<String> of suitable categories for the keyword
     */
    private List<String> getCategories(String keyword, List<Keyword> allKeywords, User user) {
      log.info("Keyword: " + keyword, "AllKeywords: " + allKeywords, "User: " + user);
        ArrayList<String> suitableCategories = new ArrayList<>();
        ArrayList<String> exactMatchCategories = new ArrayList<>();
        ArrayList<String> commonWordCategories = new ArrayList<>();

        if (keyword == null || keyword.isEmpty() || user == null) {
            return suitableCategories;
        }

        keyword = keyword.toLowerCase();
        String bestCategory = null;
        int mostCommonWords = -1;

        // Loop through all categories and their titles
        for (Keyword keywordEntity : allKeywords) {
            for (String title : keywordEntity.getKeywords()) {
                title = title.toLowerCase();

                // If keyword exactly matches the category's title, add the category to the list
                if (keyword.equals(title)) {
                    exactMatchCategories.add(keywordEntity.getCategory());
                    continue;
                } else if (keyword.contains(title)) {
                    int commonWordCount = countCommonWords(keyword, title); // calculate the number of common words
                    if (commonWordCount > mostCommonWords) {
                        mostCommonWords = commonWordCount;
                        bestCategory = keywordEntity.getCategory(); // set the best category if the number of common words is higher than the previous best
                    } else if (commonWordCount == mostCommonWords) {
                        commonWordCategories.add(keywordEntity.getCategory()); // add the category to the list of suitable categories if the number of common words is the same as the previous best
                    }
                }
            }
        }
        
        // Add the exact match categories to the list of suitable categories
        suitableCategories.addAll(exactMatchCategories);
        
        // Add the common word categories to the list of suitable categories if it is not empty
        if (!commonWordCategories.isEmpty()) {
            suitableCategories.addAll(commonWordCategories);
        }

        // Add the best category to the list of suitable categories if it is not null
        if (bestCategory != null) {
            suitableCategories.add(bestCategory);
        }

        return suitableCategories;
    }

    /**
     * Count the number of common words between the keyword and the title
     * 
     * @param keyword
     * @param title
     * @return int count of common words
     */
    private int countCommonWords(String keyword, String title) {
        String[] keywords = keyword.split("\\s+"); // Split words based on whitespace
        String[] titleWords = title.split("\\s+"); // Split words based on whitespace
        int matchCount = 0;
        for (String word : keywords) {
            for (String titleWord : titleWords) {
                if (word.equals(titleWord)) {
                    matchCount++;
                    break; // When a match is found, no need to continue checking this word
                }
            }
        }
        return matchCount;
    }
}
