package com.manager.dbee.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.manager.dbee.dto.Policy;
import com.manager.dbee.service.AdminService;
import com.manager.dbee.service.PolicyInsertService;
import com.manager.dbee.service.PolicyReadService;

@Controller
@RequestMapping("/registerpolicy")
public class PolicyInsertController {

	@Autowired
	private AdminService adminService;

	@Autowired
	private final PolicyInsertService policyInsertService;

	@Autowired
	private final PolicyReadService policyReadService; // ✅ 기존 서비스 주입받기

	public PolicyInsertController(PolicyInsertService policyInsertService, PolicyReadService policyReadService) {
		this.policyInsertService = policyInsertService;
		this.policyReadService = policyReadService;

	}

	// 1. 페이지 처음 열 때 - 필드명만 테이블 헤드에 있고 필드값은 비어 있는 정책 테이블 나옴
	// int page는 현재 페이지를 받으려 추가한 코드(0616)
	@GetMapping
	public String viewPolicyPage(
			@RequestParam(value = "page", defaultValue = "1") int page, 
			@RequestParam(value = "typeFilter", required = false) String typeFilter,
			@RequestParam(value = "orderBy", required = false, defaultValue="p_id") String orderBy,
			@RequestParam(value = "direction", required = false) String direction,
			Model model) {

		// 테스트 용
		System.out.println("🔄 GET /registerpolicy called");

		// 페이지네이션이 없었을 때의 코드
		//List<Policy> policies = policyReadService.getAllPolicies(); // ✅ 이거 그대로 사용
		//model.addAttribute("policies", policies); // 🔑 View에서 th:each="policy : ${policies}"로 활용

		// 유효성 체크 및 기본값 세팅
		if(!"created_at".equals(orderBy) && !"updated_at".equals(orderBy) && !"p_id".equals(orderBy)) {
			orderBy = "p_id";
		}
		
	    if (!"asc".equalsIgnoreCase(direction) && !"desc".equalsIgnoreCase(direction)) {
	        direction = null;
	    }
		
			
		// 페이지네이션을 적용한 코드
		int viewCnt = 5;
		int offset = (page - 1) * viewCnt;
		
		// 06.17 추가
		List<Policy> policies;
		int cnt;
		
		// 필터 없으면 기존 방식(전체 방식 + 페이지네이션)
		if(typeFilter == null || typeFilter.isEmpty()) {			
			policies = policyReadService.getPoliciesWithPagination(viewCnt, offset);
			cnt = policyReadService.getPoliciesCnt(); //getPoliciesCnt
		
		} 
		// 필터 있으면 필터 + 정렬 + 페이지네이션
		else {
			policies = policyReadService.getPoliciesWithFilterAndPagination
					(typeFilter, orderBy, direction, viewCnt, offset);
			cnt = policyReadService.getPoliciesCntWithFilter(typeFilter);
		}
		
		// 기존 방식
		// int totalPages = (cnt/viewCnt) + ((cnt%viewCnt == 0) ? 0 : 1);

		// 새 방식(실수 나눗셈을 한 후, 올림해서 정수로 변환) -> 2.3은 3이 되고, 3.0은 3 그대로
		int totalPages = (int)Math.ceil((double) cnt / viewCnt);
		
		model.addAttribute("policies", policies);
		model.addAttribute("currentPage", page);	// 현재 페이지 넘겨주기
		model.addAttribute("totalPages", totalPages);	// 전체 페이지 수 넘겨주기
		model.addAttribute("typeFilter", typeFilter);	// 드롭다운 상태 유지용
		model.addAttribute("orderBy", orderBy);
		model.addAttribute("direction", direction);
		
		return "registerpolicy";
	}

	// ✅ 2. 정책 다중 등록 (등록 버튼 눌렀을 때)
	@PostMapping
	@ResponseBody
	public String submitPolicies(@RequestBody List<Policy> policies) {
		try {
			for (Policy policy : policies) {

				// created_by, updated_by 유효성 검사
				if (!adminService.existsAdmin(policy.getCreated_by())
						|| !policy.getUpdated_by().equals(policy.getCreated_by())) {
					return "ERROR: 존재하지 않는 관리자를 등록했거나 수정자와 등록자가 다릅니다";
				}
			}
			
			// 한 번에 다중 등록 서비스 호출(트랜잭션 범위 유지)
			policyInsertService.registerPolicies(policies);

			return "SUCCESS";

		} catch (Exception e) {
			return "ERROR: " + e.getMessage();
		}
	}

}
