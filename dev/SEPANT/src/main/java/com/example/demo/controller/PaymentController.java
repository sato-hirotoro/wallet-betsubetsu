package com.example.demo.controller;

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
import com.example.demo.entity.HendohiMeisai;
import com.example.demo.form.MeisaiRow;
import com.example.demo.form.PaymentForm;
import com.example.demo.service.PaymentService;
import com.example.demo.service.SeisanService;
import com.example.demo.validator.PaymentFormValidator;

/**
 * IMG-02 支払い登録。
 */
@Controller
@RequestMapping("/payment")
public class PaymentController {

	private final PaymentService paymentService;
	private final SeisanService seisanService;
	private final PaymentFormValidator paymentFormValidator;
	private final RowErrorMessages rowErrorMessages;

	public PaymentController(PaymentService paymentService, SeisanService seisanService,
			PaymentFormValidator paymentFormValidator, RowErrorMessages rowErrorMessages) {
		this.paymentService = paymentService;
		this.seisanService = seisanService;
		this.paymentFormValidator = paymentFormValidator;
		this.rowErrorMessages = rowErrorMessages;
	}

	@InitBinder("paymentForm")
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(paymentFormValidator);
	}

	@GetMapping
	public String show(@RequestParam String ym, @RequestParam Integer userId, @RequestParam String himokuCd,
			Model model) {
		YmUtil.parse(ym);
		paymentService.getCell(userId, himokuCd);
		seisanService.checkNotSettled(ym);

		PaymentForm form = new PaymentForm();
		form.setYm(ym);
		form.setUserId(userId);
		form.setHimokuCd(himokuCd);
		for (HendohiMeisai m : paymentService.getMeisai(ym, userId, himokuCd)) {
			form.getMeisaiList().add(new MeisaiRow(m.getShiharaiDd(), m.getKingaku(), m.getBiko()));
		}
		if (form.getMeisaiList().isEmpty()) {
			form.getMeisaiList().add(new MeisaiRow());
		}
		return render(form, List.of(), model);
	}

	@PostMapping(params = "addRow")
	public String addRow(@ModelAttribute PaymentForm form, Model model) {
		form.getMeisaiList().add(new MeisaiRow());
		return render(form, List.of(), model);
	}

	@PostMapping(params = "removeRow")
	public String removeRow(@ModelAttribute PaymentForm form, @RequestParam("removeRow") int index, Model model) {
		if (index >= 0 && index < form.getMeisaiList().size()) {
			form.getMeisaiList().remove(index);
		}
		return render(form, List.of(), model);
	}

	@PostMapping(params = "register")
	public String register(@Validated @ModelAttribute PaymentForm form, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return render(form, rowErrorMessages.build(result, Map.of()), model);
		}
		paymentService.replace(form);
		return "redirect:/complete?type=payment&ym=" + form.getYm();
	}

	private String render(PaymentForm form, List<String> errors, Model model) {
		var ym = YmUtil.parse(form.getYm());
		seisanService.checkNotSettled(form.getYm());
		model.addAttribute("paymentForm", form);
		model.addAttribute("cell", paymentService.getCell(form.getUserId(), form.getHimokuCd()));
		model.addAttribute("minDate", ym.atDay(1));
		model.addAttribute("maxDate", ym.atEndOfMonth());
		model.addAttribute("errors", errors);
		return "payment";
	}
}
