package com.heima.search.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApUserSearchServiceTest {

    @Autowired
    private ApUserSearchService apUserSearchService;

    @Test
    void insert() {
        apUserSearchService.insert("hhh",4);
    }
}