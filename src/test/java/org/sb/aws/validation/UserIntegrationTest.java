package org.sb.aws.validation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sb.aws.config.auth.CustomOAuth2UserService;
import org.sb.aws.config.auth.dto.OAuthAttributes;
import org.sb.aws.entity.mail.VerificationToken;
import org.sb.aws.entity.mail.VerificationTokenRepository;
import org.sb.aws.entity.user.Role;
import org.sb.aws.entity.user.User;
import org.sb.aws.entity.user.UserRepository;
import org.sb.aws.exception.EmailExistsException;
import org.sb.aws.rest.dto.EmailDto;
import org.sb.aws.service.MailService;
import org.sb.aws.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "application.properties")
@Transactional
public class UserIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;
    @Autowired
    private VerificationTokenService verificationTokenService;
    @Autowired
    private MailService mailService;

    @PersistenceContext
    private EntityManager entityManager;

    private Long userId;

    @Before
    public void givenUserAndVerificationToken() throws EmailExistsException {
        User user = User.builder()
                .email("wearegang369@gmail.com")
                .name("halfDev")
                .role(Role.USER)
                .build();

        getVerificationToken();

        entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();

        userId = user.getId();
    }

    @After
    public void flushAfter() {
        entityManager.flush();
        entityManager.clear();
    }

    /// /// /// TEST /// /// ///

    @Test
    public void whenContextLoad_thenCorrect() {
        assertTrue(userRepository.count() > 0);
    }

    // TODO ACCOUNT CONFIRM EMAIL
    @Test
    public void givenNewUser_whenRegistered_thenCorrect() throws EmailExistsException {
        final String userEmail = UUID.randomUUID().toString();
        final OAuthAttributes oAuthAttributes = createUserDto(userEmail);
        final User user = customOAuth2UserService.saveOrUpdate(oAuthAttributes);

        final VerificationToken token = getVerificationToken();
        final EmailDto emailDto = createEmailDto(token);
        verificationTokenService.createVerification(emailDto);

        mailService.sendEmail(createMailMessage());

        assertNotNull(token.getToken());

        assertNotNull(user);
        assertNotNull(user.getEmail());
        assertEquals(userEmail, user.getEmail());
        assertNotNull(user.getId());
    }

    private OAuthAttributes createUserDto(final String email) {
        return OAuthAttributes.builder()
                .email(email)
                .name("halfDev")
                .build();
    }

    public VerificationToken getVerificationToken() {
        VerificationToken verificationToken = new VerificationToken();
        String token = verificationToken.getToken();
        verificationTokenService.verifyEmail(token);

        assertNotNull(token);

        return verificationToken;
    }

    private EmailDto createEmailDto(VerificationToken token) {
        EmailDto emailDto = new EmailDto();
        emailDto.setTo("wearegang369@gmail.com");
        emailDto.setSubject("JUNIT");
        emailDto.setVerificationapi("http://localhost:8080/verify-email?code=" + token.getToken());
        return emailDto;
    }

    private SimpleMailMessage createMailMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("wearegang369@gmail.com");
        message.setSubject("JUNIT");
        message.setText("TEST");
        return message;
    }

}
