package com.kbt.backend.common.utils;


import lombok.experimental.UtilityClass;

@UtilityClass
public class UuidGenerator {

    public String generate() {
        return java.util.UUID.randomUUID().toString();
    }
}
