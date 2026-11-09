package com.example.demo.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.example.demo.common.YmUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 固定費設定（IMG-03）の1行。年月は画面の形式（YYYY-MM）で持つ。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetteiRow {

	@NotBlank
	private String himokuCd;

	@NotNull
	private Integer userId;

	@NotNull
	@Min(1)
	private Integer kingaku;

	@NotBlank
	@Pattern(regexp = YmUtil.YYYY_MM_REGEXP)
	private String kaishiMm;

	@NotBlank
	@Pattern(regexp = YmUtil.YYYY_MM_REGEXP)
	private String shuryoMm;

	@Size(max = 50)
	private String biko;
}
