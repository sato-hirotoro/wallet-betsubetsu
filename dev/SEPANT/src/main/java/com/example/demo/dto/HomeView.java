package com.example.demo.dto;

import java.util.List;

/**
 * ホーム（IMG-01）の表示データ。
 */
public record HomeView(
		String ym,
		String prevYm,
		String nextYm,
		boolean settled,
		SeisanResult result,
		List<CellRow> seppanRows,
		List<CellRow> tatekaeRows,
		List<SetteiRow> koteiRows,
		List<SetteiRow> shunyuRows,
		List<TobanRow> tobanRows,
		List<String> warnings) {

	/** 変動費の行（費目×夫・妻の合計金額）。 */
	public record CellRow(String himokuCd, String himokuName, int otto, int tsuma) {
	}

	/** 固定費・収入の行（費目・利用者・金額）。 */
	public record SetteiRow(String himokuName, String userName, int kingaku) {
	}

	/** 当番の行（担当者に○）。 */
	public record TobanRow(String himokuName, boolean otto, boolean tsuma) {
	}
}
