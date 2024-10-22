package com.twitter.XClone.services;

import com.twitter.XClone.exceptions.EmailFailureException;
import com.twitter.XClone.model.LocalUser;
import com.twitter.XClone.model.VerificationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailService {
    private JavaMailSender javaMailSender;
    @Value("${email.from}")
    private String fromAddress;
    @Value("${app.url}")
    private String url;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /* This function returns a simple mail message with the from address set to email.from value*/
    private SimpleMailMessage makeMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        return message;
    }

    /*
     *Takes in a verificationToken object
     *Creates the email structure by setting subject, body, and to
     *tries to send the email
     */
    public void sendVerificationEmail(VerificationToken token) throws EmailFailureException {
        SimpleMailMessage message = makeMessage();
        message.setTo(token.getUser().getEmail());
        message.setSubject("Verify your email");
        message.setText("Please follow the link below to verify your email. \n + " +
                url + "/auth/verify?token=" + token.getToken());
        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            throw new EmailFailureException();
        }
    }

    public void sendPasswordResetEmail(LocalUser user, String token) throws EmailFailureException {
        SimpleMailMessage message = makeMessage();
        message.setTo(user.getEmail());
        message.setSubject("Password Reset");
        message.setText("Please follow the link provided to reset your password. \n"
                + "If you did not request a password change, please ignore this message. \n"
                + url + "/auth/reset?token=" + token);
        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            throw new EmailFailureException();
        }
    }

}
