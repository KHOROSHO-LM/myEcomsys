package org.example.ecms.mapper;

import org.example.ecms.entity.OperationLog;

import java.util.List;

public interface OperationLogMapper {
    List<OperationLog> selectAll();
}
