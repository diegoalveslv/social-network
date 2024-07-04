package com.company.SocialNetwork.useraccount;

import com.company.SocialNetwork.utils.SlugGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;

@Service
@Validated
@RequiredArgsConstructor
public class UserAccountService {

    private final SlugGenerator slugGenerator;
    private final UserAccountRepository userAccountRepository;

    public String createUserAccount(@Valid CreateUserAccountRequestDTO requestData) {
        String slug = slugGenerator.generateSlug();

        var userAccount = UserAccount.builder()
                .slug(slug)
                .email(requestData.getEmail())
                .username(requestData.getUsername())
                .password(requestData.getPassword())
                .profileName(requestData.getProfileName())
                .createdAt(ZonedDateTime.now())
                .build();

        var savedUserAccount = userAccountRepository.save(userAccount);

        return savedUserAccount.getSlug();
    }
}
