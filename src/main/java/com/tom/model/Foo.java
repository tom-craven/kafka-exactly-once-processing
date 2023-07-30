package com.tom.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Foo {
    @NotNull
    String name;
    @NotNull
    Integer number;
}
