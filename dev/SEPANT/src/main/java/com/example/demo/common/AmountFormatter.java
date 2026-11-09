package com.example.demo.common;

import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * 金額の表示形式（{@code ¥12,000}、{@code △¥2,000}）。
 * Thymeleaf からは {@code ${@amountFormatter.format(x)}} で呼ぶ。
 */
@Component("amountFormatter")
public class AmountFormatter {

	public String format(Integer amount) {
		if (amount == null) {
			return "";
		}
		String body = "¥" + NumberFormat.getIntegerInstance(Locale.JAPAN).format(Math.abs((long) amount));
		return amount < 0 ? "△" + body : body;
	}
}
