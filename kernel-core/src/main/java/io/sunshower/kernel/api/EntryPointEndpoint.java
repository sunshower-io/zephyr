package io.sunshower.kernel.api;

import com.github.resource4j.spring.annotations.InjectValue;
import io.sunshower.kernel.Endpoint;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Component
@Path("entry")
@Endpoint
public class EntryPointEndpoint {

    @InjectValue
    private String value;

    @GET
    @Path("hello")
    public String message() {
        return value;
    }
}

