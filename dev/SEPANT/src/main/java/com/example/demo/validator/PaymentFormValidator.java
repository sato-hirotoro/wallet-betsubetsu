package com.example.demo.validator;

import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.example.demo.common.YmUtil;
import com.example.demo.form.MeisaiRow;
import com.example.demo.form.PaymentForm;

/**
 * 支払い登録の相関チェック。支払日が対象年月の日付であること（E101）。
 */
@Component
public class PaymentFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return PaymentForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		PaymentForm form = (PaymentForm) target;
		if (form.getYm() == null || !YmUtil.YYYYMM.matcher(form.getYm()).matches()) {
			return; // 年月の形式はコントローラーでチェックする
		}
		YearMonth ym = YmUtil.parse(form.getYm());
		List<MeisaiRow> rows = form.getMeisaiList();
		for (int i = 0; i < rows.size(); i++) {
			MeisaiRow row = rows.get(i);
			String field = "meisaiList[" + i + "].shiharaiDd";
			if (row.getShiharaiDd() == null || errors.hasFieldErrors(field)) {
				continue;
			}
			if (!YearMonth.from(row.getShiharaiDd()).equals(ym)) {
				errors.rejectValue(field, "E101", new Object[] { YmUtil.toDisplay(form.getYm()) }, null);
			}
		}
	}
}
