package com.manager.dbee.dao.policy;

import java.util.List;
import java.util.Map;

import com.manager.dbee.dto.Policy;

public interface PolicyDAO {
	
    List<Policy> selectAllPolicies();

    Policy selectPolicyById(String p_id);

    List<Policy> searchPoliciesAdvanced(Map<String, Object> filters);

    // 필요하면 insert, update 메서드도 추가 가능
    // int insertPolicy(Policy policy);
    // int updatePolicy(Policy policy);
}
