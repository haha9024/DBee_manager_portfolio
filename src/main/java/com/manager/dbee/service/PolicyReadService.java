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

	@Transactional(transactionManager = "policyTransactionManager")
	public List<Policy> getPoliciesWithPagination(int pageLimit, int pageOffset) {
		return policyDAO.selectSomePolicies(pageLimit, pageOffset);
	}

	@Transactional(transactionManager = "policyTransactionManager")
	public int getPoliciesCnt() {
		return policyDAO.getPoliciesCnt();
	}

	@Transactional(transactionManager = "policyTransactionManager")
	public List<Policy> getPoliciesWithFilterAndPagination(String typeFilter, String orderBy, String direction,
			int viewCnt, int offset) {
		return policyDAO.selectPoliciesWithFilterAndPagination(typeFilter, orderBy, direction, viewCnt, offset);
	}

	@Transactional(transactionManager = "policyTransactionManager")
	public int getPoliciesCntWithFilter(String typeFilter) {
		return policyDAO.countPoliciesWithFilter(typeFilter);
	}

	@Transactional(transactionManager = "policyTransactionManager")
	public Policy getPolicyById(String p_id) {
		return policyDAO.selectPolicyById(p_id);
	}

	@Transactional(transactionManager = "policyTransactionManager")
	public List<Policy> getAllPolicies() {
		return policyDAO.selectAllPolicies();
	}

	@Transactional(transactionManager = "policyTransactionManager")
	public List<Policy> searchPoliciesAdvanced(Map<String, Object> filters) {
		return policyDAO.searchPoliciesAdvanced(filters);
	}

}
