package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.service.itf.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    @Value("${spring.mail.username}")
    private String serverEmail;

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void sendHTML(String to, String subject, String template, Map<String, Object> variables) {
        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setFrom(serverEmail);
            messageHelper.setSubject(subject);
            messageHelper.setTo(to);

            messageHelper.setText(buildHTML(template, variables), true);
        };

        javaMailSender.send(preparator);
    }

    @Override
    @Async
    public void sendHTML(Set<String> to, String subject, String template, Map<String, Object> variables) {
        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setFrom(serverEmail);
            messageHelper.setSubject(subject);
            messageHelper.setTo(to.toArray(new String[0]));  // Chuyển Set<String> thành String[]

            messageHelper.setText(buildHTML(template, variables), true);
        };

        javaMailSender.send(preparator);
    }

    @Override
    public String buildHTML(String template, Map<String, Object> variables) {
        Context context = new Context();
        for (Map.Entry<String, Object> entry : variables.entrySet()){
            context.setVariable(entry.getKey(), entry.getValue());
        }
        return templateEngine.process(template, context);
    }
}
