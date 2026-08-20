package org.example.ecms.service;

import org.example.ecms.entity.LoginLog;
import org.example.ecms.entity.OperationLog;
import org.example.ecms.mapper.LoginLogMapper;
import org.example.ecms.mapper.OperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {

    @Autowired
    private OperationLogMapper operationLogMapper;
    @Autowired
    private LoginLogMapper loginLogMapper;

    public List<OperationLog> listOperationLogs() {
        return operationLogMapper.selectAll();
    }

    public List<LoginLog> listLoginLogs() {
        return loginLogMapper.selectAll();
    }
}
