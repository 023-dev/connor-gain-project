package com.kbt.backend.common.utils;

import lombok.experimental.UtilityClass;

import java.util.Objects;
import java.util.function.Consumer;

@UtilityClass
public class Functions {

    public static <T> void update(final Consumer<T> function, final T input) {
        if (Objects.nonNull(input)) {
            function.accept(input);
        }
    }
}
