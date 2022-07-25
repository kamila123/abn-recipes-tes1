package com.abn.recipes.recipe.controller.config;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public abstract class BaseTest {

    @BeforeAll
    public static void before() {
        RestAssured.baseURI = "http://localhost:8082/api";
    }
}
