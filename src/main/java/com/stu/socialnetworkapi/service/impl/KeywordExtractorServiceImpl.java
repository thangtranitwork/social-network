package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.service.itf.KeywordExtractorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class KeywordExtractorServiceImpl implements KeywordExtractorService {
    private final ChatClient chatClient;

    private static final SystemMessage systemMessageWith3Keywords = new SystemMessage("Extract 3 keywords from the text. Format: [key, key]:");
    private static final SystemMessage systemMessageWithAKeyword = new SystemMessage("Extract 1 keywords from the text. Format: [key]:");

    private static final ParameterizedTypeReference<List<String>> parameterizedTypeReference = new ParameterizedTypeReference<>() {
    };
    private static final int MIN_LENGTH_TO_GET_MORE_KEYWORDS = 200;

    public KeywordExtractorServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public List<String> extract(String content) {
        if (content == null || content.isBlank())
            return List.of();
        UserMessage userMessage = new UserMessage(content);
        SystemMessage systemMessage = content.length() < MIN_LENGTH_TO_GET_MORE_KEYWORDS
                ? systemMessageWithAKeyword
                : systemMessageWith3Keywords;
        Prompt prompt = new Prompt(systemMessage, userMessage);

        return chatClient
                .prompt(prompt)
                .call()
                .entity(parameterizedTypeReference);
    }
}
