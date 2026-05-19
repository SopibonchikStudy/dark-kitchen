package edu.rutmiit.demo.orderservice.dto;

import java.util.List;

public record CookingTimeEstimation(
        int totalSeconds,
        List<ItemTime> items
) {}