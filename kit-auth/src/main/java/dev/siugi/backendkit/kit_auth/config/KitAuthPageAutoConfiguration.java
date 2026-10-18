package dev.siugi.backendkit.kit_auth.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.RequestPredicates.GET;

@AutoConfiguration
@ConditionalOnProperty(
        prefix = "kit-auth.page",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class KitAuthPageAutoConfiguration {

    @Bean
    public RouterFunction<ServerResponse> kitAuthPageRoutes() {
        return route(GET("/login"), request ->
                ServerResponse.temporaryRedirect(
                        java.net.URI.create("/kit-auth/index.html")
                ).build()
        ).andRoute(GET("/signup"), request ->
                ServerResponse.temporaryRedirect(
                        java.net.URI.create("/kit-auth/index.html?mode=signup")
                ).build()
        );
    }
}