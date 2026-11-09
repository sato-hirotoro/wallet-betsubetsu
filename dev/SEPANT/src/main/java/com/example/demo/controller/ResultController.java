package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.common.YmUtil;
import com.example.demo.service.SeisanService;

/**
 * IMG-04 計算結果。
 */
@Controller
public class ResultController {

	private final SeisanService seisanService;

	public ResultController(SeisanService seisanService) {
		this.seisanService = seisanService;
	}

	@GetMapping("/result")
	public String show(@RequestParam String ym, Model model) {
		YmUtil.parse(ym);
		model.addAttribute("view", seisanService.getResultView(ym));
		return "result";
	}
}
