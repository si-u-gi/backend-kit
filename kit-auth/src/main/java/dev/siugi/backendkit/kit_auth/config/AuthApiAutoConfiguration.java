
package dev.siugi.backendkit.kit_auth.config;

import dev.siugi.backendkit.kit_auth.controller.AuthController;
import dev.siugi.backendkit.kit_auth.service.AuthService;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@AutoConfigureAfter(KitAuthAutoConfiguration.class)
@ConditionalOnBean(AuthService.class)
@Import(AuthController.class)
public class AuthApiAutoConfiguration {
}
