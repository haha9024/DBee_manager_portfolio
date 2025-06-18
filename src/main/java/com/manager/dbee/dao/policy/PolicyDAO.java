package com.manager.dbee.dao.policy;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.manager.dbee.dto.Policy;

@Mapper
public interface PolicyDAO {
	
	// 정책 조회(select)
    public List<Policy> selectAllPolicies();
    
    // 페이지네이션을 적용한 정책 테이블의 항목 검색
    public List<Policy> selectSomePolicies(@Param("pageLimit") int pageLimit, @Param("pageOffset") int pageOffset);

    // 페이지네이션을 위한 정책 테이블의 전체 건수
    public int getPoliciesCnt();
    
    // 페이지네이션, 필터 적용
    public List<Policy> selectPoliciesWithFilterAndPagination(
    		@Param("typeFilter") String typeFilter,
    		@Param("orderBy") String orderBy,
    		@Param("direction") String direction,
    		@Param("viewCnt") int viewCnt,
    		@Param("offset") int offset
    		);
    
    // 선택 필터에 따라 개수 가져오기
    public int countPoliciesWithFilter(@Param("typeFilter") String typeFilter);
    
    public Policy selectPolicyById(String p_id);
    
    public Policy selectPolicyByName(String name);

    public List<Policy> searchPoliciesAdvanced(Map<String, Object> filters);
  
    
    // 정책 등록(insert)
    public int insertPolicy(Policy policy);
  
    
    // 필요하면 update 메서드도 추가 가능
    // int updatePolicy(Policy policy);
}
