package dev.siugi.backendkit.kit_auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "kit.auth")
public class KitAuthProperties {

    private final Csrf csrf = new Csrf();

    public Csrf getCsrf() {
        return csrf;
    }

    private final Security security = new Security();

    public Security getSecurity() {
        return security;
    }

    public static class Csrf {
        private boolean enabled = true;
        private boolean cookieBased = true;
        private List<String> ignoredPaths = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isCookieBased() {
            return cookieBased;
        }

        public void setCookieBased(boolean cookieBased) {
            this.cookieBased = cookieBased;
        }

        public List<String> getIgnoredPaths() {
            return ignoredPaths;
        }

        public void setIgnoredPaths(List<String> ignoredPaths) {
            this.ignoredPaths = ignoredPaths;
        }
    }

    public static class Security {

        private List<String> publicPaths = new ArrayList<>();

        public List<String> getPublicPaths() {
            return publicPaths;
        }

        public void setPublicPaths(List<String> publicPaths) {
            this.publicPaths = publicPaths;
        }
    }
}