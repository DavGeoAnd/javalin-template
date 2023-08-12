package com.davgeoand.api;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceRunner {
    public static void main(String[] args) {
        JavalinService javalinService = new JavalinService();
        javalinService.start();
    }
}
