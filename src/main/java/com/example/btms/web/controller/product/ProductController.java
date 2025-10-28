package com.example.btms.web.controller.product;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product")
public class ProductController {

    @GetMapping("/btms")
    public String btmsOverview() {
        return "product/btms";
    }
}
