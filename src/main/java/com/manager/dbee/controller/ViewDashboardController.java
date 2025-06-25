package com.manager.dbee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewDashboardController {

	@GetMapping("/dashboard")
	public String dashboard() {
		return "dashboard"; // src/main/resources/templates/dashboard.html
	}
}
