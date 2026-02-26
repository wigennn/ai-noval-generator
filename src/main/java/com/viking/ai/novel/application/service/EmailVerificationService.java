package com.viking.ai.novel.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮箱验证码：内存存储 + 发送邮件（未配置 SMTP 时仅打印到日志，便于开发）
 */
@Service
@Slf4j
public class EmailVerificationService {

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;

    /** key: email (lowercase), value: [code, expireAtMillis, lastSendAtMillis] */
    private final Map<String, CodeEntry> store = new ConcurrentHashMap<>();

    public EmailVerificationService(
            org.springframework.beans.factory.ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.host:}") String mailHost) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.mailEnabled = mailHost != null && !mailHost.isBlank() && this.mailSender != null;
    }

    /**
     * 生成并发送验证码，同一邮箱 60 秒内只能发送一次
     */
    public void sendCode(String email) {
        String key = email.trim().toLowerCase();
        long now = System.currentTimeMillis();

        CodeEntry existing = store.get(key);
        if (existing != null && (now - existing.lastSendAt) < RESEND_COOLDOWN_SECONDS * 1000L) {
            throw new RuntimeException("发送过于频繁，请 " + RESEND_COOLDOWN_SECONDS + " 秒后再试");
        }

        String code = generateCode();
        long expireAt = now + CODE_EXPIRE_MINUTES * 60 * 1000L;
        store.put(key, new CodeEntry(code, expireAt, now));

        if (mailEnabled && mailSender != null) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(email.trim());
                msg.setSubject("AI 小说生成器 - 验证码");
                msg.setText("您的验证码是：" + code + "\n\n" + CODE_EXPIRE_MINUTES + " 分钟内有效。\n\n如非本人操作请忽略。");
                mailSender.send(msg);
                log.info("Verification email sent to {}", email);
            } catch (Exception e) {
                log.error("Failed to send verification email to {}", email, e);
                store.remove(key);
                throw new RuntimeException("邮件发送失败，请稍后重试");
            }
        } else {
            log.info("[开发模式] 邮箱 {} 验证码: {}", email, code);
        }
    }

    /**
     * 校验验证码，正确则移除并返回 true
     */
    public boolean verifyAndConsume(String email, String code) {
        if (email == null || code == null) return false;
        String key = email.trim().toLowerCase();
        CodeEntry entry = store.get(key);
        if (entry == null) return false;
        if (System.currentTimeMillis() > entry.expireAt) {
            store.remove(key);
            return false;
        }
        if (!entry.code.equals(code.trim())) return false;
        store.remove(key);
        return true;
    }

    private static String generateCode() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(r.nextInt(10));
        }
        return sb.toString();
    }

    private static class CodeEntry {
        final String code;
        final long expireAt;
        final long lastSendAt;

        CodeEntry(String code, long expireAt, long lastSendAt) {
            this.code = code;
            this.expireAt = expireAt;
            this.lastSendAt = lastSendAt;
        }
    }
}
