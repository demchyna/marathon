package com.softserve.edu.security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AccessDeniedController {

    @RequestMapping(path = "/access-denied", method = RequestMethod.GET)
    public String accessPage() {
        return "access-page";
    }
}
