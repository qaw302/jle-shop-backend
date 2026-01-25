package com.smplatform.product_service.aop;

import com.smplatform.product_service.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Aspect
@Component
public class BeforeExecuteMethodAspect {
    @Before("@annotation(com.smplatform.product_service.annotation.AdminOnly)")
    public void hasAdminRole() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest();

        String userId = request.getHeader("X-MEMBER-ID");
        String role = request.getHeader("ROLE");

        if (userId == null && role == null) {
            throw new UnauthorizedException("token을 확인할 수 없습니다.");
        } else if (!role.equals("ADMIN")) {
            throw new UnauthorizedException("관리자 전용입니다.");
        }

    }
}
