package com.example.demo.controller;

import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.common.YmUtil;
import com.example.demo.dto.Direction;
import com.example.demo.dto.SeisanResult;
import com.example.demo.service.SeisanService;

/**
 * IMG-05 精算確認、精算処理。
 */
@Controller
public class SeisanController {

	private final SeisanService seisanService;
	private final MessageSource messageSource;

	public SeisanController(SeisanService seisanService, MessageSource messageSource) {
		this.seisanService = seisanService;
		this.messageSource = messageSource;
	}

	@GetMapping("/seisan/confirm")
	public String confirm(@RequestParam String ym, Model model) {
		YmUtil.parse(ym);
		seisanService.checkNotSettled(ym);
		SeisanResult result = seisanService.calculate(ym);

		String amount = NumberFormat.getIntegerInstance(Locale.JAPAN).format(result.amount());
		String target = result.direction() == Direction.NONE ? amount : result.direction().getLabel() + " " + amount;
		model.addAttribute("ym", ym);
		model.addAttribute("result", result);
		model.addAttribute("confirmMessage", messageSource.getMessage("Q001", new Object[] { target }, Locale.JAPAN));
		return "confirm";
	}

	@PostMapping("/seisan")
	public String settle(@RequestParam String ym) {
		YmUtil.parse(ym);
		seisanService.settle(ym);
		return "redirect:/complete?type=seisan&ym=" + ym;
	}
}
