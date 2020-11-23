package com.kakaopay.spraying.configure.support;

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
        final int userId = Integer.parseInt(Objects.requireNonNull(
                webRequest.getHeader(USER_IDENTIFY_KEY)));
        final String roomId = webRequest.getHeader(ROOM_IDENTIFY_KEY);
        return Identifier.of(userId, roomId);
    }
}
