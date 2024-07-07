package com.company.SocialNetwork.useraccount;

import com.company.SocialNetwork.exception.FieldListValidationException;
import com.company.SocialNetwork.utils.SlugGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Validated
@RequiredArgsConstructor
public class UserAccountService {

    private final SlugGenerator slugGenerator;
    private final UserAccountRepository userAccountRepository;

    public String createUserAccount(@Valid CreateUserAccountRequestDTO requestData) {
        String slug = slugGenerator.generateSlug();
        String username = requestData.getUsername().trim();
        String profileName = requestData.getProfileName().trim();
        String email = requestData.getEmail().trim();

        checkForDuplicatedUser(username, profileName, email);

        var userAccount = UserAccount.builder()
                .slug(slug)
                .email(email)
                .username(username)
                .password(requestData.getPassword())
                .profileName(profileName)
                .createdAt(ZonedDateTime.now())
                .build();

        var savedUserAccount = userAccountRepository.save(userAccount);

        return savedUserAccount.getSlug();
    }

    private void checkForDuplicatedUser(String username, String profileName, String email) throws FieldListValidationException {
        var userExistenceDTO = userAccountRepository.checkUserExistence(username, email, profileName);

        Map<String, String> messageToFieldName = new HashMap<>();
        if (userExistenceDTO.getUsernameExists()) {
            messageToFieldName.put(CreateUserAccountRequestDTO.Fields.username, "already exists");
        }

        if (userExistenceDTO.getProfileExists()) {
            messageToFieldName.put(CreateUserAccountRequestDTO.Fields.profileName, "already exists");
        }

        if (userExistenceDTO.getEmailExists()) {
            messageToFieldName.put(CreateUserAccountRequestDTO.Fields.email, "already exists");
        }

        if (!messageToFieldName.isEmpty()) {
            throw new FieldListValidationException(messageToFieldName);
        }
    }
}
