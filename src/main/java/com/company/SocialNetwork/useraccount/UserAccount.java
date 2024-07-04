package com.company.SocialNetwork.useraccount;

import com.company.SocialNetwork.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnTransformer;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UserAccount extends BaseEntity {

    @Column(nullable = false, unique = true, updatable = false, length = 12)
    private String slug;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 40)
    private String username;

    @Column(nullable = false)
    @ColumnTransformer(write = "social_network.crypt(?, social_network.gen_salt('bf'))")
    private String password;

    @Column(nullable = false, unique = true, length = 60)
    private String profileName;

    @Column
    private ZonedDateTime createdAt;
}
