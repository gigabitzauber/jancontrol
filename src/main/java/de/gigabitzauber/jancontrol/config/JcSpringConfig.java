package de.gigabitzauber.jancontrol.config;

import com.google.common.util.concurrent.MoreExecutors;
import de.gigabitzauber.jancontrol.JcLifecycle;
import de.gigabitzauber.jancontrol.cruise.CruiseCommand;
import de.gigabitzauber.jancontrol.cruise.FanCruiseExecutor;
import de.gigabitzauber.jancontrol.cruise.WatchConfigCommand;
import de.gigabitzauber.jancontrol.util.JcSystemTime;
import de.gigabitzauber.jancontrol.util.JcTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.Executors;

@Configuration
public class JcSpringConfig {

    private static final int FAN_CRUISE_THREADPOOL_SIZE = 10;
    private static final int SYSTEM_THREADPOOL_SIZE = 1;

    @Bean
    @Qualifier("fanCruise")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public FanCruiseExecutor fanCruiseExecutor() {
        return new FanCruiseExecutor(() ->
            MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(FAN_CRUISE_THREADPOOL_SIZE)));
    }

    @Bean
    @Qualifier("system")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public FanCruiseExecutor systemThreadsExecutor() {
        return new FanCruiseExecutor(() ->
            MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(SYSTEM_THREADPOOL_SIZE)));
    }

    @Bean
    public JcLifecycle lifecycle(@Qualifier("fanCruise") FanCruiseExecutor executor, JcTime time, Logger log) {
        return new JcLifecycle(executor, time, log);
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

    @Bean
    public WatchConfigCommand watchConfigCommand(@Qualifier("system") FanCruiseExecutor executor, JcLifecycle lifecycle, Logger log) {
        return new WatchConfigCommand(executor, lifecycle, log);
    }

    @Bean
    public JcTime time() {
        return new JcSystemTime();
    }
}
