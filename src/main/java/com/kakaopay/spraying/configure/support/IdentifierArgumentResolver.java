package com.kakaopay.spraying.configure.support;

import com.kakaopay.spraying.exception.SprayResponseException;
import com.kakaopay.spraying.exception.SprayResponseException.SprayErrors;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Objects;

public class IdentifierArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String USER_IDENTIFY_KEY = "X-USER-ID";
    private static final String ROOM_IDENTIFY_KEY = "X-ROOM-ID";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Identifier.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        try {
            final int userId = Integer.parseInt(Objects.requireNonNull(
                    webRequest.getHeader(USER_IDENTIFY_KEY)));
            final String roomId = Objects.requireNonNull(webRequest.getHeader(ROOM_IDENTIFY_KEY));
            return Identifier.of(userId, roomId);
        } catch (NullPointerException | NumberFormatException e) {
            throw SprayResponseException.of(SprayErrors.NO_IDENTIFYING_INFORMATION);
        }
    }
}
