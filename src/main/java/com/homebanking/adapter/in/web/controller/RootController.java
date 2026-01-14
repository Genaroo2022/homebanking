package com.homebanking.adapter.in.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/hola")
    public String root() {
        return "Hola mundo";
    }
}
