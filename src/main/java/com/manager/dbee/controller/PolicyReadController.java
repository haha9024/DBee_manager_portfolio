package com.manager.dbee.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.manager.dbee.dto.Policy;
import com.manager.dbee.service.PolicyReadService;

@Controller
@RequestMapping("/readpolicy2")
public class PolicyReadController {

    private final PolicyReadService policyReadService;

    public PolicyReadController(PolicyReadService policyReadService) {
        this.policyReadService = policyReadService;
    }

    // 1. 페이지 처음 열 때 - 전체 정책 조회
    @GetMapping
    public String viewPolicyPage(Model model) {
        List<Policy> policies = policyReadService.getAllPolicies();

        model.addAttribute("filters", new HashMap<>()); // null 방지
        model.addAttribute("policies", policies);
        return "readpolicy2"; // Thymeleaf 템플릿
    }

    // 2. 필터 조건 검색 (AJAX 요청)
    @PostMapping("/search")
    @ResponseBody
    public List<Policy> searchPolicies(@RequestBody Map<String, Object> filters) {
        // 빈 필터 Map이 오더라도 XML에서 잘 처리됨
        return policyReadService.searchPoliciesAdvanced(filters);
    }
}
