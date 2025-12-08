package com.sr.serviceroute.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReorderDTO(@Valid @NotEmpty List<ReorderItem> items) {
}
