package com.company.SocialNetwork.post;

import com.company.SocialNetwork.shared.BaseEntity;
import com.company.SocialNetwork.useraccount.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Post extends BaseEntity {

    @Column(nullable = false, unique = true, length = 12)
    private String slug;

    @Column(nullable = false, length = 500)
    private String content;

    @JoinColumn
    @ManyToOne
    private UserAccount userAccount;

    @Column
    private ZonedDateTime createdAt;

}
