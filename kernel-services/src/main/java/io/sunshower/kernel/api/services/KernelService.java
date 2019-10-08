package io.sunshower.kernel.api.services;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/kernel")
@Component
public class KernelService {

    @Requires
    BundleContext context;


    @GET
    @Path("/hello")
    public String sayHello() {
        return "Hello";
    }

    @PostConstruct
    public void start() {
        System.out.println("Start!");
    }

}
