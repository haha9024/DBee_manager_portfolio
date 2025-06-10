package com.manager.dbee.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manager.dbee.dao.policy.PolicyDAO;
import com.manager.dbee.dto.Policy;

@Service
public class PolicyReadService {

    private final PolicyDAO policyDAO;

    public PolicyReadService(PolicyDAO policyDAO) {
        this.policyDAO = policyDAO;
    }

    @Transactional(transactionManager="policyTransactionManager")
    public Policy getPolicyById(String p_id) {
        return policyDAO.selectPolicyById(p_id);
    }

    @Transactional(transactionManager="policyTransactionManager")
    public List<Policy> getAllPolicies() {
        return policyDAO.selectAllPolicies();
    }

    @Transactional(transactionManager="policyTransactionManager")
    public List<Policy> searchPoliciesAdvanced(Map<String, Object> filters) {
        return policyDAO.searchPoliciesAdvanced(filters);
    }

    // 필요하면 저장/수정 메서드도 추가
}

