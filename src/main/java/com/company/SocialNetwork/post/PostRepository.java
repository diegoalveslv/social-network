package com.company.SocialNetwork.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    Optional<Post> findFirstBySlug(String postSlug);
}
