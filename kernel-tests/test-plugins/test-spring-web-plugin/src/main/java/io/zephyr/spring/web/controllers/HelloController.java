package io.zephyr.spring.web.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
  @RequestMapping(name = "hello")
  public String index() {
    return "Hello world!";
  }
}
