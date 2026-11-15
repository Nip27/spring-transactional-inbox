package io.github.inbox.loyalty.config;

import io.github.dmitriyiliyov.oncebox.starter.EnableOncebox;
import org.springframework.context.annotation.Configuration;

/**
 * Switches the inbox on. Kept off the application class so that test slices, which do not load configuration
 * classes, do not start it.
 */
@Configuration(proxyBeanMethods = false)
@EnableOncebox
public class OnceboxConfiguration {
}
