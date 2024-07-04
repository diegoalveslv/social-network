package com.company.SocialNetwork.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.OptimizableGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EqualsAndHashCode
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SequencePerEntityGenerator")
    @GenericGenerator(name = "SequencePerEntityGenerator", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {@Parameter(name = SequenceStyleGenerator.CONFIG_SEQUENCE_PER_ENTITY_SUFFIX, value = "_id_seq"),
                    @Parameter(name = OptimizableGenerator.INCREMENT_PARAM, value = "1")
            })
    @Column(name = "ID", nullable = false, unique = true, updatable = false)
    @JsonProperty
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Integer id;
}