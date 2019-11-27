package io.zephyr.spring.web.controllers;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
  public String index() {
    return "Hello world!";
  }
}
