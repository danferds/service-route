package com.sr.serviceroute.dto;

import jakarta.validation.constraints.Min;

import java.util.UUID;

public record ReorderItem(UUID id, @Min(1) int seq) {
}
