package com.example.demo.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.common.YmUtil;
import com.example.demo.entity.Himoku;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.form.SetteiForm;
import com.example.demo.form.SetteiRow;
import com.example.demo.service.MasterService;
import com.example.demo.service.SetteiService;
import com.example.demo.validator.SetteiFormValidator;

/**
 * IMG-03 固定費設定。
 */
@Controller
@RequestMapping("/settei")
public class SetteiController {

	private static final Map<String, String> SECTION_NAMES = Map.of(
			SetteiForm.KOTEI, "固定費",
			SetteiForm.SHUNYU, "収入",
			SetteiForm.TOBAN, "当番");

	private final SetteiService setteiService;
	private final MasterService masterService;
	private final SetteiFormValidator setteiFormValidator;
	private final RowErrorMessages rowErrorMessages;

	public SetteiController(SetteiService setteiService, MasterService masterService,
			SetteiFormValidator setteiFormValidator, RowErrorMessages rowErrorMessages) {
		this.setteiService = setteiService;
		this.masterService = masterService;
		this.setteiFormValidator = setteiFormValidator;
		this.rowErrorMessages = rowErrorMessages;
	}

	@InitBinder("setteiForm")
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(setteiFormValidator);
	}

	@GetMapping
	public String show(@RequestParam(required = false) String ym, Model model) {
		String target = YmUtil.normalize(ym);
		return render(setteiService.getAll(target), List.of(), model);
	}

	@PostMapping(params = "addRow")
	public String addRow(@ModelAttribute SetteiForm form, @RequestParam("addRow") String listName, Model model) {
		List<SetteiRow> list = form.listOf(listName);
		if (list == null) {
			throw new InvalidRequestException("不明なリスト: " + listName);
		}
		list.add(new SetteiRow());
		return render(form, List.of(), model);
	}

	@PostMapping(params = "removeRow")
	public String removeRow(@ModelAttribute SetteiForm form, @RequestParam("removeRow") String target, Model model) {
		String[] parts = target.split(":");
		List<SetteiRow> list = parts.length == 2 ? form.listOf(parts[0]) : null;
		if (list == null || !parts[1].matches("\\d+")) {
			throw new InvalidRequestException("不正な行の指定: " + target);
		}
		int index = Integer.parseInt(parts[1]);
		if (index < list.size()) {
			list.remove(index);
		}
		return render(form, List.of(), model);
	}

	@PostMapping(params = "register")
	public String register(@Validated @ModelAttribute SetteiForm form, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return render(form, rowErrorMessages.build(result, SECTION_NAMES), model);
		}
		setteiService.replaceAll(form);
		return "redirect:/complete?type=settei&ym=" + form.getYm();
	}

	private String render(SetteiForm form, List<String> errors, Model model) {
		form.setYm(YmUtil.normalize(form.getYm()));
		Map<String, String> himokuNames = new LinkedHashMap<>();
		for (Himoku h : masterService.himokuList()) {
			himokuNames.put(h.getHimokuCd(), h.getHimokuName());
		}
		model.addAttribute("setteiForm", form);
		model.addAttribute("himokuNames", himokuNames);
		model.addAttribute("userNames", masterService.userNames());
		model.addAttribute("users", masterService.users());
		model.addAttribute("koteiHimoku",
				masterService.himokuList(SetteiFormValidator.SECTION_KUBUN.get(SetteiForm.KOTEI)));
		model.addAttribute("shunyuHimoku",
				masterService.himokuList(SetteiFormValidator.SECTION_KUBUN.get(SetteiForm.SHUNYU)));
		model.addAttribute("tobanHimoku",
				masterService.himokuList(SetteiFormValidator.SECTION_KUBUN.get(SetteiForm.TOBAN)));
		model.addAttribute("errors", errors);
		return "settei";
	}
}
