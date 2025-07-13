package com.manager.dbee.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.manager.dbee.dao.policy.PolicyDAO;
import com.manager.dbee.dto.Policy;
import com.manager.dbee.enums.DangerLevel;

@Service
public class PolicyInsertService {

	@Autowired
	private PolicyDAO policyDAO;

	@Autowired
	private AdminService adminService;

	@Autowired
	private PolicyReloadService policyReloadService;

	@Transactional(transactionManager = "policyTransactionManager")
	public int registerPolicy(Policy policy) {

		// 예: 필수 필드 검증, 기본값 세팅 등

		validateId(policy);
		validateName(policy);
		validateStatus(policy);
		validateAdmin(policy);
		validateActions(policy);
		/* validateId(policy); */

		Timestamp now = new Timestamp(System.currentTimeMillis());
		policy.setCreated_at(now);
		policy.setUpdated_at(now);

		int inserted = policyDAO.insertPolicy(policy);
		
		// 트랜잭션 커밋 이후에 reload 신호 보내기
		// 커밋 직후에만 실행되도록 등록
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				policyReloadService.sendReloadSignal();
			}
		});
		
		return inserted;
		// 이전 버전
		//return policyDAO.insertPolicy(policy);
	}

	@Transactional(transactionManager = "policyTransactionManager")
	public void registerPolicies(List<Policy> policies) {
		for (Policy policy : policies) {
			// 개별 정책 검증
			validateId(policy);
			validateName(policy);
			validateStatus(policy);
			validateAdmin(policy);
			validateActions(policy);

			Timestamp now = new Timestamp(System.currentTimeMillis());
			policy.setCreated_at(now);
			policy.setUpdated_at(now);

			policyDAO.insertPolicy(policy);
		}

		// 일괄 등록이 끝난 뒤 한번만 reload 신호 전송
		// 롤백되면 호출되지 않도록
	    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
	        @Override
	        public void afterCommit() {
	            policyReloadService.sendReloadSignal();
	        }
	    });
	}

	@Transactional(transactionManager = "policyTransactionManager")
	public List<Policy> getPoliciesWithFilterAndPagination(String typeFilter, String orderBy, String direction,
			int viewCnt, int offset) {
		return policyDAO.selectPoliciesWithFilterAndPagination(typeFilter, orderBy, direction, viewCnt, offset);
	}

	@Transactional(transactionManager = "policyTransactionManager")
	public int getPoliciesCntWithFilter(String typeFilter) {
		return policyDAO.countPoliciesWithFilter(typeFilter);// countPoliciesWithFilter
	}

	private void validateId(Policy policy) {

		String pId = policy.getP_id();

		// 테스트용
		System.out.println("정책 ID 중복 검사 시작: " + pId);

		if (pId == null || pId.isEmpty()) {
			throw new IllegalArgumentException("정책 ID는 필수입니다");
		}

		if (!pId.matches("^[PB][0-9]{3,5}$")) {
			throw new IllegalArgumentException("정책 ID 형식이 올바르지 않습니다. 예: P001, B123");
		}

		Policy existing = policyDAO.selectPolicyById(pId);

		// 테스트용
		System.out.println("select 결과: " + existing);

		if (existing != null) {
			throw new IllegalArgumentException("이미 존재하는 정책 ID입니다: " + pId);
		}
	}

	private void validateName(Policy policy) {

		String name = policy.getName();

		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("정책 이름은 필수입니다.");
		}

		if (policyDAO.selectPolicyByName(name) != null) {
			throw new IllegalArgumentException("이미 존재하는 정책 이름입니다: " + name);
		}
	}

	private void validateStatus(Policy policy) {

		if (policy.getStatus() == null) {
			throw new IllegalArgumentException("정책 상태는 필수입니다.");
		}
	}

	private void validateActions(Policy policy) {

		String actions = policy.getAction_to_take();

		if (actions == null || actions.trim().isEmpty()) {
			throw new IllegalArgumentException("조치는 필수입니다.");
		}

		if (!(actions.contains("BLOCK_LOG") || actions.contains("PERMIT_LOG") || actions.contains("QUARANTINE_LOG"))) {
			throw new IllegalArgumentException("조치에는 최소 하나의 로그(BLOCK_LOG, PERMIT_LOG, QUARANTINE_LOG)가 포함되어야 합니다.");
		}

		DangerLevel level = policy.getThreat_level();

		if (level != null && (level == DangerLevel.HIGH || level == DangerLevel.CRITICAL)
				&& (!actions.contains("ALERT_SITE") || !actions.contains("ALERT_SMS"))) {

			throw new IllegalArgumentException("위험도가 HIGH 또는 CRITICAL이면 " + "ALERT_SITE와 ALERT_SMS 조치는 필수입니다.");
		}
	}

	private void validateAdmin(Policy policy) {

		String creator = policy.getCreated_by();
		String updator = policy.getUpdated_by();

		if (creator == null || creator.isEmpty()) {
			throw new IllegalArgumentException("생성자는 필수입니다.");
		}

		if (!adminService.existsAdmin(creator)) {
			throw new IllegalArgumentException("존재하지 않는 관리자 번호입니다: " + creator);
		}

		if (updator == null || updator.isEmpty()) {
			throw new IllegalArgumentException("수정자는 필수입니다.");
		}

		if (!creator.equals(updator)) {
			throw new IllegalArgumentException("생성자와 수정자는 동일해야 합니다.");
		}
	}
}
