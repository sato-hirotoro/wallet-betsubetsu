package com.example.demo.controller;

import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.common.YmUtil;
import com.example.demo.exception.InvalidRequestException;

/**
 * IMG-06 完了。
 */
@Controller
public class CompleteController {

	private static final Map<String, String> PROCESS_NAMES = Map.of(
			"payment", "支払いの登録",
			"settei", "固定費の設定",
			"seisan", "精算");

	private final MessageSource messageSource;

	public CompleteController(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@GetMapping("/complete")
	public String show(@RequestParam String type, @RequestParam String ym, Model model) {
		YmUtil.parse(ym);
		String name = PROCESS_NAMES.get(type);
		if (name == null) {
			throw new InvalidRequestException("不明な処理: " + type);
		}
		model.addAttribute("ym", ym);
		model.addAttribute("completeMessage", messageSource.getMessage("I001", new Object[] { name }, Locale.JAPAN));
		return "complete";
	}
}
