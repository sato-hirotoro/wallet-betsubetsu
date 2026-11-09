package com.example.demo.common;

import java.util.Arrays;

/**
 * 費目の区分（m_himoku.kubun）。DB設計書 6.1 と同じコード値を持つ。
 */
public enum Kubun {

	/** 折半（変動費） */
	HENDO_SEPPAN("01", "折半（変動費）"),
	/** 立替（変動費） */
	HENDO_TATEKAE("02", "立替（変動費）"),
	/** 折半（固定費） */
	KOTEI_SEPPAN("03", "折半（固定費）"),
	/** 立替（固定費） */
	KOTEI_TATEKAE("04", "立替（固定費）"),
	/** 収入 */
	SHUNYU("05", "収入"),
	/** 当番 */
	TOBAN("06", "当番");

	private final String code;
	private final String label;

	Kubun(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	/** 変動費明細（t_hendohi_meisai）に登録する区分か。 */
	public boolean isHendohi() {
		return this == HENDO_SEPPAN || this == HENDO_TATEKAE;
	}

	/** 固定設定（t_kotei_settei）に登録する区分か。 */
	public boolean isKotei() {
		return !isHendohi();
	}

	/** 折半の対象か（01・03）。 */
	public boolean isSeppan() {
		return this == HENDO_SEPPAN || this == KOTEI_SEPPAN;
	}

	/** 立替か（02・04）。 */
	public boolean isTatekae() {
		return this == HENDO_TATEKAE || this == KOTEI_TATEKAE;
	}

	public static Kubun fromCode(String code) {
		return Arrays.stream(values())
				.filter(k -> k.code.equals(code))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("不明な区分: " + code));
	}
}
