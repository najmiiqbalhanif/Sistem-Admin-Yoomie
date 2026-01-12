package com.yoomie.web.controllerREST;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryControllerREST {

    @GetMapping("/api/categories")
    public List<String> getCategories() {
        return List.of(
                "makanan",
                "minuman",
                "sabun",
                "perabot",
                "pakaian",
                "minyak",
                "alat_tulis"
        );
    }
}
