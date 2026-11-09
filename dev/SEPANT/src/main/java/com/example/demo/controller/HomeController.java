package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.common.YmUtil;
import com.example.demo.service.HomeService;

/**
 * IMG-01 ホーム。
 */
@Controller
public class HomeController {

	private final HomeService homeService;

	public HomeController(HomeService homeService) {
		this.homeService = homeService;
	}

	@GetMapping("/")
	public String root() {
		return "redirect:/home";
	}

	@GetMapping("/home")
	public String home(@RequestParam(required = false) String ym, Model model) {
		String target = YmUtil.normalize(ym);
		model.addAttribute("view", homeService.getHome(target));
		return "home";
	}
}
