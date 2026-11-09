package com.example.demo.common;

import org.springframework.stereotype.Component;

/**
 * Thymeleaf から年月を表示形式に変換するための Bean。
 * {@code ${@ymFormatter.display('202606')}} → {@code 2026.6}
 */
@Component("ymFormatter")
public class YmFormatter {

	/** {@code YYYYMM} または {@code YYYY-MM} を {@code YYYY.M} に変換する。不正な値はそのまま返す。 */
	public String display(String ym) {
		if (ym == null || ym.isBlank()) {
			return "";
		}
		String yyyymm = ym.replace("-", "");
		if (!YmUtil.YYYYMM.matcher(yyyymm).matches()) {
			return ym;
		}
		return YmUtil.toDisplay(yyyymm);
	}
}
