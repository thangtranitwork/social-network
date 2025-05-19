package com.stu.socialnetworkapi.service.itf;

import java.util.Map;
import java.util.Set;

public interface MailService {
    void sendHTML(String to, String subject, String template, Map<String, Object> variables);
    void sendHTML(Set<String> to, String subject, String template, Map<String, Object> variables);
    String buildHTML(String template, Map<String, Object> variables);
}
