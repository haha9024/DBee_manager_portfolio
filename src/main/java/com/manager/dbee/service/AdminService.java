package com.manager.dbee.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manager.dbee.dao.policy.AdminDAO;

@Service
public class AdminService {

	@Autowired
    private AdminDAO adminDAO;

	// Security_Administrator 테이블에 admin_no 존재 여부 체크 메서드
	@Transactional(transactionManager = "policyTransactionManager")
    public boolean existsAdmin(String adminNo) {
        return adminDAO.existsAdmin_no(adminNo) > 0;
    }
}
