package io.sunshower.kernel.api.services;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/kernel")
public class KernelService {



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
