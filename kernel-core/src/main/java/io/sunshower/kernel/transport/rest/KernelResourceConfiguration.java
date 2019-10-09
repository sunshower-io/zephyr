package io.sunshower.kernel.transport.rest;

import io.sunshower.kernel.Endpoint;
import io.sunshower.kernel.api.EntryPointEndpoint;
import lombok.val;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;


@Component
public class KernelResourceConfiguration extends ResourceConfig implements ApplicationListener<ContextRefreshedEvent> {


    public KernelResourceConfiguration() {
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        val context = event.getApplicationContext();
        val factory = context.getAutowireCapableBeanFactory();
        val endpointNames = context.getBeanNamesForAnnotation(Endpoint.class);
        for(val name : endpointNames) {
            val type = factory.getType(name);
            register(type);
        }
    }
}
