package com.stu.socialnetworkapi.enums;

import lombok.Getter;

import java.util.List;

@Getter
public enum Language {
    VIETNAMESE(4, 2, 3),
    ENGLISH(3, 1, 1.5),
    UNSUPPORTED(1, 1, 1);

    public static List<Language> getLanguages() {
        return List.of(VIETNAMESE, ENGLISH);
    }

    final int maxWords;
    final int minWords;
    final double lengthWeight;

    Language(int maxWords, int minWords, double lengthWeight) {
        this.maxWords = maxWords;
        this.minWords = minWords;
        this.lengthWeight = lengthWeight;
    }

    public String getStopwordsFilePath() {
        return "stopwords/" + this.name().toLowerCase() + "-stop-words.txt";
    }
}
