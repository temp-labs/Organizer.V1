package kpi.ipt.organizer.users.service.impl;

import kpi.ipt.organizer.users.UsersMessagingConstants;
import kpi.ipt.organizer.users.exceptions.IllegalUserParametersException;
import kpi.ipt.organizer.users.model.User;
import kpi.ipt.organizer.users.model.messaging.MailRequest;
import kpi.ipt.organizer.users.model.messaging.MailType;
import kpi.ipt.organizer.users.repository.UsersRepository;
import kpi.ipt.organizer.users.service.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Service
public class UsersServiceImpl implements UsersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersServiceImpl.class);

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final AmqpTemplate amqpTemplate;

    public UsersServiceImpl(UsersRepository usersRepository,
                            PasswordEncoder passwordEncoder,
                            AmqpTemplate amqpTemplate) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public User getUser(long userId) {
        return usersRepository.findOne(userId);
    }

    @Override
    @Transactional
    public User registerUser(String email, String password, String name) {
        User newUser = new User(name, email, passwordEncoder.encode(password));

        try {
            User savedUser = usersRepository.save(newUser);

            LOGGER.info("New user has been successfully registered: id={}, name={}",
                    savedUser.getId(), savedUser.getName());

            sendGreetingEmail(savedUser);

            return savedUser;
        } catch (DataIntegrityViolationException e) {
            throw new IllegalUserParametersException(e);
        }
    }

    private void sendGreetingEmail(User user) {

        Map<String, String> userParams = Collections.singletonMap("name", user.getName());
        Map<String, Object> mailParams = Collections.singletonMap("user", userParams);

        MailRequest mail = new MailRequest();

        mail.setReceivers(Collections.singleton(user.getEmail()));
        mail.setMailType(MailType.GREETING);
        mail.setMailParameters(mailParams);

        try {
            amqpTemplate.convertAndSend(
                    UsersMessagingConstants.MAIL_EXCHANGE_NAME,
                    UsersMessagingConstants.MAIL_ROUTING_KEY,
                    mail
            );

            LOGGER.info("Greeting email has been sent: {}", mail);
        } catch (Exception e) {
            //Successful greeting email delivery is not critical, so just log this exception and forget.
            LOGGER.warn("Message sending failed: {}", mail, e);
        }
    }

    @Override
    public boolean checkEmail(String email) {
        return usersRepository.countUsersByEmail(email) == 0L;
    }

    @Override
    public User authenticate(String email, String password) {
        User user = usersRepository.findByEmail(email);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }

        return null;
    }
}
