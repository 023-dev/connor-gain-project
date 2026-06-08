package com.kbt.backend.common.config;

import com.kbt.backend.core.auth.presentation.AccessTokenArgumentResolver;
import com.kbt.backend.core.auth.presentation.AuthenticationInterceptor;
import com.kbt.backend.core.auth.presentation.UserIdArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final UserIdArgumentResolver userIdArgumentResolver;
    private final AccessTokenArgumentResolver accessTokenArgumentResolver;

    @Value("${storage.upload-dir}")
    private String uploadDirectory;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userIdArgumentResolver);
        resolvers.add(accessTokenArgumentResolver);
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        final String normalizedLocation = Path.of(uploadDirectory).toAbsolutePath().normalize().toUri().toString();
        final String location = normalizedLocation.endsWith("/")
                ? normalizedLocation
                : normalizedLocation + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
