package com.carrental.util;

import java.util.regex.Pattern;

public class ReviewUtil {
    
    private static final Pattern PROFANITY_PATTERN = Pattern.compile(
        "\\b(badword1|badword2|badword3)\\b",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * Check if review contains profanity
     */
    public static boolean containsProfanity(String content) {
        if (content == null) {
            return false;
        }
        return PROFANITY_PATTERN.matcher(content).find();
    }
    
    /**
     * Clean review content of special characters
     */
    public static String sanitizeContent(String content) {
        if (content == null) {
            return null;
        }
        return content.trim().replaceAll("\\s+", " ");
    }
    
    /**
     * Get rating emoji representation
     */
    public static String getRatingEmoji(Integer rating) {
        if (rating == null) {
            return "⭐";
        }
        return switch (rating) {
            case 5 -> "⭐⭐⭐⭐⭐";
            case 4 -> "⭐⭐⭐⭐";
            case 3 -> "⭐⭐⭐";
            case 2 -> "⭐⭐";
            case 1 -> "⭐";
            default -> "⭐";
        };
    }
    
    /**
     * Get rating description
     */
    public static String getRatingDescription(Integer rating) {
        if (rating == null) {
            return "Not rated";
        }
        return switch (rating) {
            case 5 -> "Excellent";
            case 4 -> "Very Good";
            case 3 -> "Good";
            case 2 -> "Fair";
            case 1 -> "Poor";
            default -> "Not rated";
        };
    }
    
    /**
     * Validate review can be submitted
     */
    public static boolean isValidReviewContent(String title, String content) {
        return title != null && !title.isBlank() && 
               (content == null || !content.isBlank());
    }
}
