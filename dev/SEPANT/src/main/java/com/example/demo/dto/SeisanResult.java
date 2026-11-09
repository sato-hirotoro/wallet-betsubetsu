package com.example.demo.dto;

/**
 * 精算額の計算結果（処理設計書 4.3）。
 *
 * @param seppanTotal 折半対象の合計 S（区分01・03）
 * @param shunyuTotal 収入の合計 I（区分05）
 * @param seppanGaku  折半額（S − I を夫・妻に分けた額）
 * @param seppanPaid  折半対象の支払（区分01・03）
 * @param shunyu      収入の受取（区分05）
 * @param tatekae     立替（区分02・04）
 * @param toban       当番（区分06）
 * @param shiharai    支払額
 * @param futan       負担額
 * @param sagaku      差額（支払額 − 負担額）
 * @param direction   精算の向き
 * @param amount      精算額（差額（夫）の絶対値）
 */
public record SeisanResult(
		int seppanTotal,
		int shunyuTotal,
		UserAmount seppanGaku,
		UserAmount seppanPaid,
		UserAmount shunyu,
		UserAmount tatekae,
		UserAmount toban,
		UserAmount shiharai,
		UserAmount futan,
		UserAmount sagaku,
		Direction direction,
		int amount) {

	/** S − I */
	public int seppanTaisho() {
		return seppanTotal - shunyuTotal;
	}
}
