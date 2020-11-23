package com.kakaopay.spraying.domain.entity.embedable;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Token implements Serializable {
    private static final long serialVersionUID = 1;

    @NotNull
    @Column(nullable = false, unique = true)
    private String token;
}
