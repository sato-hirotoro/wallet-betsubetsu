package com.example.demo.form;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import lombok.Data;

/** 固定費設定（IMG-03）のフォーム。 */
@Data
public class SetteiForm {

	public static final String KOTEI = "koteiList";
	public static final String SHUNYU = "shunyuList";
	public static final String TOBAN = "tobanList";

	private String ym;

	@Valid
	private List<SetteiRow> koteiList = new ArrayList<>();

	@Valid
	private List<SetteiRow> shunyuList = new ArrayList<>();

	@Valid
	private List<SetteiRow> tobanList = new ArrayList<>();

	/** リスト名からリストを返す。不明なリスト名なら null。 */
	public List<SetteiRow> listOf(String name) {
		return switch (name) {
		case KOTEI -> koteiList;
		case SHUNYU -> shunyuList;
		case TOBAN -> tobanList;
		default -> null;
		};
	}
}
