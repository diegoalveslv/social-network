package com.company.SocialNetwork.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CachePage<T> {

    private Set<T> content = new HashSet<>();
    private String totalItems = "*";
    private Double nextScore;
}
