package com.manager.dbee.dao.policy;

public interface AdminDAO {

	//  List<Policy> selectAllPolicies(); 
	// 	<select id="selectAllPolicies" resultType="com.manager.dbee.dto.Policy">
	//    SELECT * FROM Policy </select>
	
	// admin_no로 직원이 존재하는지 관리자테이블에서 찾아서 참, 거짓 반환?
	int existsAdmin_no(String admin_no);
}
