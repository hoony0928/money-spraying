package com.kakaopay.spraying.configure.support;

import com.kakaopay.spraying.domain.entity.embedable.User;
import com.kakaopay.spraying.util.ModelMapperUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Identifier {
    @NotNull
    private Integer userId;

    @NotBlank
    private String roomId;

    public User toEntity() {
        return ModelMapperUtils.map(this, User.class);
    }
}