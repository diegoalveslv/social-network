package com.company.SocialNetwork.user;

import com.company.SocialNetwork.user.exception.WeakPasswordException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class AccountService {

    private static final Pattern passwordRegex = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    public void createUserAccount(CreateUserAccountRequestDTO createUserAccountRequest) throws WeakPasswordException {
        if (!passwordRegex.matcher(createUserAccountRequest.getPassword()).matches())
            throw new WeakPasswordException(List.of("password: does not meet security requirements"
                    , "Should have at least one lowercase letter"
                    , "Should have at least one uppercase letter"
                    , "Should have at least one special character"));
    }
}
