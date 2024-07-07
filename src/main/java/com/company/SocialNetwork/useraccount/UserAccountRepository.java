package com.company.SocialNetwork.useraccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {

    Optional<UserAccount> findFirstBySlug(String slug);

    @Query(value = "select (select count(*) > 0 from user_account where username = :username limit 1) as usernameExists, " +
            "(select count(*) > 0 from user_account where email = :email limit 1) as emailExists, " +
            "(select count(*) > 0 from user_account where profile_name = :profileName limit 1) as profileExists",
            nativeQuery = true)
    UserExistenceDTO checkUserExistence(@Param("username") String username,
                                        @Param("email") String email,
                                        @Param("profileName") String profileName);
}
