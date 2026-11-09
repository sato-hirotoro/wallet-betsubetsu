package com.example.demo.dto;

import java.util.List;

/**
 * 計算結果（IMG-04）の表示データ。
 *
 * @param rows 区分01〜04の費目ごとの支払額
 */
public record ResultView(String ym, boolean settled, List<Row> rows, SeisanResult result) {

	public record Row(String himokuName, String kubunLabel, int otto, int tsuma) {
	}
}
