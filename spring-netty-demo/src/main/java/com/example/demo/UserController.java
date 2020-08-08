package com.example.demo;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-09
 */
@RestController
@RequestMapping("spring")
public class UserController {

    @RequestMapping("user")
    public Object info() {

        System.out.println("nihao ");
        System.out.println(1/0);
        Map<String, String> map = new HashMap<>();
        map.put("name", "thinking");
        return map;
    }
}
