package de.gigabitzauber.jancontrol.config;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import de.gigabitzauber.jancontrol.JcLifecycle;
import de.gigabitzauber.jancontrol.cruise.CruiseCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.Executors;

@Configuration
public class JcSpringConfig {

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ListeningScheduledExecutorService fanCruiseExecutor() {
        return MoreExecutors.listeningDecorator(Executors.newSingleThreadScheduledExecutor());
    }

    @Bean
    public JcLifecycle lifecycle(ListeningScheduledExecutorService fanCruiseExecutor, Logger log) {
        return new JcLifecycle(fanCruiseExecutor, log);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Logger log(InjectionPoint injectionPoint) {
        var declaringClass = injectionPoint.getMember().getDeclaringClass();
        return LoggerFactory.getLogger(declaringClass);
    }

    @Bean
    public CruiseCommand cruiseCommand(JcLifecycle lifecycle) {
        return new CruiseCommand(lifecycle);
    }
}
