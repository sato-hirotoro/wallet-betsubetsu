package com.example.demo.form;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import lombok.Data;

/** 支払い登録（IMG-02）のフォーム。 */
@Data
public class PaymentForm {

	private String ym;
	private Integer userId;
	private String himokuCd;

	@Valid
	private List<MeisaiRow> meisaiList = new ArrayList<>();
}
