package org.example.ecms.controller;

import org.example.ecms.common.Result;
import org.example.ecms.entity.LoginLog;
import org.example.ecms.entity.OperationLog;
import org.example.ecms.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/log")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping("/operation")
    public Result<List<OperationLog>> operation() {
        return Result.success(logService.listOperationLogs());
    }

    @GetMapping("/login")
    public Result<List<LoginLog>> login() {
        return Result.success(logService.listLoginLogs());
    }
}
