package com.manager.dbee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {

	@GetMapping("/")
	public String welcomepage() {
		
		// 나중에 로그인 로직 추가하면 주석 풀 예정인 아래 코드
		// return "forward:/login";
		
		return "welcome";
	}
}
