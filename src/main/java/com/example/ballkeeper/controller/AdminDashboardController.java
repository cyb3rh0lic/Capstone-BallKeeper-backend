package com.example.ballkeeper.controller;

import com.example.ballkeeper.api.dto.admin.DashboardResponse;
import com.example.ballkeeper.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public DashboardResponse getDashboardStats(@RequestParam Long adminId) {
        return adminDashboardService.getDashboardStats(adminId);
    }
}