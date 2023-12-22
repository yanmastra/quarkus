package org.acme.authenticationService.services;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.CheckedTemplate;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.File;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class MailService {

    @Inject
    ReactiveMailer mailer;
    @Inject
    Logger logger;

    @ConfigProperty(name = "quarkus.mailer.from", defaultValue = "")
    String mailerFrom;
    @ConfigProperty(name = "company.main.logo_file_path", defaultValue = "")
    String companyMainLogo;

    public static class MailMessageTemplateData {
        public String beginningMessage;
        public String contentMessage;
        public String mainImage;
    }

    @CheckedTemplate
    public static class Templates {
        public static native MailTemplate.MailTemplateInstance mailMessage(MailMessageTemplateData data);
    }

    public static final String OTP_REG_MAIL_SUBJECT = "otp_register_mail_subject";
    public static final String OTP_REG_MAIL_BEGINNING = "otp_register_mail_beginning";
    public static final String OTP_REG_MAIL_CONTENT = "otp_register_mail_content";
    public static final String DFT_USER_PASSWORD_SUBJECT = "default_user_password_subject";
    public static final String DFT_USER_PASSWORD_BEGINNING = "default_user_password_beginning";
    public static final String DFT_USER_PASSWORD_CONTENT = "default_user_password_content";
    public static final Map<String, Map<String, String>> messageLists = Map.of(
            "ID", Map.of(
                    OTP_REG_MAIL_SUBJECT, "[Rahasia] Kode OTP %s untuk mendaftar sebagai %s",
                    OTP_REG_MAIL_BEGINNING, "<p>Halo <strong>%s</strong></p><p>Anda telah mendaftar sebagai %s di aplikasi %s</p>",
                    OTP_REG_MAIL_CONTENT, "<p>Masukan kode OTP berikut untuk verifikasi akun anda!</p><p><strong>%s</strong></p></br>" +
                            "<p>Atau anda bisa klik link berikut jika ingin memverifikasinya lewat browser</p>" +
                            "<p><a href=\"%s\">Verifikasi</a></p>",
                    DFT_USER_PASSWORD_SUBJECT, "[Username dan Password] Anda telah didaftarkan sebagai %s di %s",
                    DFT_USER_PASSWORD_BEGINNING, "<p>Hai %s, Gunakan Username dan Password berikut untuk masuk ke Aplikasi <strong>%s</strong></p>",
                    DFT_USER_PASSWORD_CONTENT, "<p><ul>" +
                            "<li>Username:<strong><span class=\"code\"> %s</span></strong></li>" +
                            "<li>Password:<strong><span class=\"code\"> %s</span></strong></li></ul></p>" +
                            "</br><p>Silahkan <strong>ganti password sesuai keinginan anda</strong> setelah masuk ke aplikasi!</p>"
            ),
            "EN", Map.of(
                    OTP_REG_MAIL_SUBJECT, "[Confidential] %s OTP code to register as %s",
                    OTP_REG_MAIL_BEGINNING, "<p>Hello <strong>%s</strong></p><p>You've been registered as %s in %s</p>",
                    OTP_REG_MAIL_CONTENT, "<p>Input this OTP code to verify your account!</p><p><strong>%s</strong></p></br>" +
                            "<p>Or you click link below if you want to verify it on browser</p>" +
                            "<p><a href=\"%s\">Verify</a></p>",
                    DFT_USER_PASSWORD_SUBJECT, "[Username and Password] You've been registered as %s in %s",
                    DFT_USER_PASSWORD_BEGINNING, "<p>Hi %s, Please use the Username and Password below to sign in to <strong>%s</strong> Apps</p>",
                    DFT_USER_PASSWORD_CONTENT, "<p><ul>" +
                            "<li>Username:<strong><span class=\"code\"> %s</span></strong></li>" +
                            "<li>Password:<strong><span class=\"code\"> %s</span></strong></li></ul></p>" +
                            "</br><p>Please <strong>change your password by as you want</strong> after signed in to the application!</p>"
            )
    );

    public Uni<Void> createRegisterEmail(String lang, String otpCode, String userName, String userEmail, String appName, String roleName) {
        MailMessageTemplateData messageTemplateData = new MailMessageTemplateData();
        messageTemplateData.beginningMessage = messageLists.get(lang).get(OTP_REG_MAIL_BEGINNING)
                .formatted(userName, roleName, appName);
        messageTemplateData.contentMessage = messageLists.get(lang).get(OTP_REG_MAIL_CONTENT)
                .formatted(otpCode, "http://onknown-host.com");
        String subject = messageLists.get(lang).get(OTP_REG_MAIL_SUBJECT).formatted(appName, roleName);

        return Templates.mailMessage(messageTemplateData)
                        .to(userEmail).subject(subject)
                        .from(mailerFrom).replyTo(mailerFrom)
                .send();
//        return Mail.withHtml(userEmail, subject,
//                Templates.mailMessage(messageTemplateData).render());
    }

    public Uni<Void> createSignInfoEmail(String lang, String username, String password, String userName, String userEmail, String appName, String roleName) {
        MailMessageTemplateData messageTemplateData = new MailMessageTemplateData();
        messageTemplateData.beginningMessage = messageLists.get(lang).get(DFT_USER_PASSWORD_BEGINNING)
                .formatted(userName, appName);
        messageTemplateData.contentMessage = messageLists.get(lang).get(DFT_USER_PASSWORD_CONTENT)
                .formatted(username, password);
        messageTemplateData.mainImage = "cid:mylogo@quarkus.io";

        String content =
                """
                        <style type="text/css">
                        p {
                        font-family: arial, helvetica, sans-serif;
                        font-size: 14px;
                        }
                        strong {
                        font-weight: 900;
                        font-size: 15px
                        }
                        
                        .code {
                        font-family: Monospace;
                        }
                        </style>
                        <center><img width="100" src="cid:mylogo@quarkus.io"/></center>
                        """ +
                messageTemplateData.beginningMessage +
                messageTemplateData.contentMessage;
        String subject = messageLists.get(lang).get(DFT_USER_PASSWORD_SUBJECT).formatted(roleName, appName)+" - "+ UUID.randomUUID();

//        Uni<String> uniContent = Templates.mailMessage(messageTemplateData)
//                .data("data", messageTemplateData)
//                .createUni();

//        return uniContent.map(content -> {
//            logger.info("email: "+content);
//            return Mail.withHtml(userEmail, subject, content)
//                    .setFrom(mailerFrom).addReplyTo(mailerFrom)
//                    .addInlineAttachment("mylogo.png", new File(companyMainLogo), "image/png","<mylogo@quarkus.io>");
//        });
//        return Templates.mailMessage(messageTemplateData)
//                .to(userEmail)
//                .subject(subject)
//                .from(mailerFrom)
//                .replyTo(mailerFrom)
//                .addInlineAttachment("mylogo.png", new File(companyMainLogo), "image/png","<mylogo@quarkus.io>")
//                .send();
        Mail mail = Mail.withHtml(userEmail, subject, content)
                    .setFrom(mailerFrom).addReplyTo(mailerFrom)
                    .addInlineAttachment("mylogo.png", new File(companyMainLogo), "image/png","<mylogo@quarkus.io>");
        return mailer.send(mail);
    }

    public Uni<Void> sendEmail(Mail mail) {
        return mailer.send(mail).onFailure().invoke(throwable -> logger.error(throwable.getMessage(), throwable));
    }
}
