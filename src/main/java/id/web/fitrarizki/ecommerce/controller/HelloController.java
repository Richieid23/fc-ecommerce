package id.web.fitrarizki.ecommerce.controller;

import id.web.fitrarizki.ecommerce.exception.BadRequestException;
import id.web.fitrarizki.ecommerce.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/bad-request")
    public String badRequest() {
        throw new BadRequestException("Bad Request");
    }

    @GetMapping("/not-found")
    public String notFound() {
        throw new ResourceNotFoundException("Not Found");
    }

    @GetMapping("/generic-error")
    public String genericError() {
        throw new RuntimeException("Generic Error");
    }
}
