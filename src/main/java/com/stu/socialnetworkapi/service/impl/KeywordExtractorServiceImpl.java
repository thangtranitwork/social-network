package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.entity.Keyword;
import com.stu.socialnetworkapi.entity.Post;
import com.stu.socialnetworkapi.enums.Language;
import com.stu.socialnetworkapi.repository.PostRepository;
import com.stu.socialnetworkapi.service.itf.KeywordExtractorService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordExtractorServiceImpl implements KeywordExtractorService {
    private static final HashMap<Language, Set<String>> STOPWORDS = new HashMap<>();
    private static final List<Language> SUPPORTED_LANGUAGES = Language.getLanguages();
    private static final int NUMBER_OF_KEYWORDS = 5;
    private static final int MIN_LENGTH_TO_PROCESS = 200;
    private final PostRepository postRepository;

    @Override
    public void extract(UUID postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            log.error("Post with id {} not found", postId);
            return;
        }
        if (post.getContent().length() < MIN_LENGTH_TO_PROCESS) {
            log.debug("Post with id {} is too short (length = {}), skipping...", postId, post.getContent().length());
            return;
        }
        Language language = detectLanguage(post.getContent());
        log.debug("Detected language for post with id {}: {}", postId, language.name());
        String cleanedContent = cleanText(post.getContent(), language);
        if (cleanedContent.isBlank()) return;
        Set<Keyword> keywords = getTopNgrams(cleanedContent, language);
        post.setKeywords(keywords);
        postRepository.save(post);
        log.debug("Extracted {} keywords for post with id {}:", keywords.size(), postId);
        for (Keyword keyword : keywords) {
            System.out.println(keyword);
        }
    }

    @PostConstruct
    public void init() {
        for (Language language : SUPPORTED_LANGUAGES) {
            Set<String> words = loadStopWords(language);
            STOPWORDS.put(language, words);
            log.debug("Loaded {} stopwords for language: {}", words.size(), language.name());
        }
    }

    private static Set<String> loadStopWords(Language language) {
        Set<String> result = new HashSet<>();
        String path = language.getStopwordsFilePath();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line.trim().toLowerCase());
            }

        } catch (IOException e) {
            log.error("Error reading file '{}': {}", path, e.getMessage());
        }
        return result;
    }

    private static Language detectLanguage(String text) {
        String[] tokens = text.toLowerCase().split("[^a-zA-Zà-ỹÀ-ỹ]+");

        Map<Language, Integer> scoreMap = new EnumMap<>(Language.class);
        for (Language language : SUPPORTED_LANGUAGES) {
            Set<String> stopwords = STOPWORDS.get(language);
            if (stopwords == null) continue;

            int count = 0;
            for (String token : tokens) {
                if (stopwords.contains(token)) {
                    count++;
                }
            }
            scoreMap.put(language, count);
            log.debug("Matched {} stopwords for {}", count, language.name());
        }

        List<Map.Entry<Language, Integer>> sorted = scoreMap.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .toList();

        if (sorted.isEmpty() || sorted.get(0).getValue() == 0) return Language.UNSUPPORTED;

        return sorted.get(0).getKey();
    }

    private static String cleanText(String text, Language language) {
        Set<String> stopWords = language == Language.UNSUPPORTED
                ? Collections.emptySet()
                : STOPWORDS.get(language);
        String preprocessed = text.replaceAll("[^\\p{L}\\p{N}\\s]", " ");
        String[] words = preprocessed.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            String cleaned = word.replaceAll("[^\\p{L}\\p{N}]", "");
            if (cleaned.isBlank()) continue;

            String normalized = cleaned.toLowerCase();
            if (!stopWords.contains(normalized)) {
                result.append(cleaned).append(" ");
            }
        }

        return result.toString().trim().replaceAll("\\s{2,}", " ");
    }

    private static Set<Keyword> getTopNgrams(String cleanedText, Language language) {
        int minN = language.getMinWords();
        int maxN = language.getMaxWords();
        double lengthWeight = language.getLengthWeight();
        String[] words = cleanedText.toLowerCase().split("\\s+");
        Map<String, Integer> ngramCounts = new HashMap<>();

        for (int n = minN; n <= maxN; n++) {
            for (int i = 0; i <= words.length - n; i++) {
                String ngram = String.join(" ", Arrays.copyOfRange(words, i, i + n));
                ngramCounts.put(ngram, ngramCounts.getOrDefault(ngram, 0) + 1);
            }
        }
        Stream<Map.Entry<String, Integer>> sorted2 = ngramCounts.entrySet().stream()
                .sorted((a, b) -> {
                    double scoreA = a.getValue() + a.getKey().split("\\s+").length * lengthWeight;
                    double scoreB = b.getValue() + b.getKey().split("\\s+").length * lengthWeight;
                    return Double.compare(scoreB, scoreA);
                });
        sorted2
                .limit(20).forEach(entry -> log.debug("Ngram: '{}' with count: {}, score: {}", entry.getKey(), entry.getValue(), entry.getValue() + entry.getKey().split("\\s+").length * lengthWeight));

        return ngramCounts.entrySet().stream()
                .sorted((a, b) -> {
                    double scoreA = a.getValue() + a.getKey().split("\\s+").length * lengthWeight;
                    double scoreB = b.getValue() + b.getKey().split("\\s+").length * lengthWeight;
                    return Double.compare(scoreB, scoreA);
                })
                .limit(NUMBER_OF_KEYWORDS)
                .map(entry -> new Keyword(entry.getKey()))
                .collect(Collectors.toSet());
    }
}
