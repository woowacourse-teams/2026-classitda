package com.classitda.authentication.presentation.resolver;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import java.security.Principal;
import org.springframework.core.MethodParameter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentMemberIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMemberId.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Long resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer modelAndViewContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Principal principal = webRequest.getUserPrincipal();
        if (!(principal instanceof JwtAuthenticationToken authentication)
                || !authentication.isAuthenticated()) {
            throw internalFailure();
        }

        return parseMemberId(authentication.getToken().getSubject());
    }

    private Long parseMemberId(String subject) {
        try {
            Long memberId = Long.valueOf(subject);
            if (memberId < 1L) {
                throw internalFailure();
            }
            return memberId;
        } catch (NumberFormatException exception) {
            throw internalFailure();
        }
    }

    private IllegalStateException internalFailure() {
        return new IllegalStateException("인증 회원 정보를 확인할 수 없습니다.");
    }
}
