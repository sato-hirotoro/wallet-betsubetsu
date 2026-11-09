package com.example.demo.common;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import com.example.demo.exception.InvalidRequestException;

/**
 * 年月の変換。
 * <ul>
 * <li>DB・パラメータ: {@code YYYYMM}（例: 202606）</li>
 * <li>画面の入力欄: {@code YYYY-MM}（例: 2026-06）</li>
 * <li>表示: {@code YYYY.M}（例: 2026.6）</li>
 * </ul>
 */
public final class YmUtil {

	/** 画面設計書 5.1 の年月パラメータの形式。 */
	public static final Pattern YYYYMM = Pattern.compile("^[0-9]{4}(0[1-9]|1[0-2])$");
	/** 固定費設定の入力欄の形式。 */
	public static final String YYYY_MM_REGEXP = "^[0-9]{4}-(0[1-9]|1[0-2])$";

	private static final DateTimeFormatter FMT_YYYYMM = DateTimeFormatter.ofPattern("uuuuMM");
	private static final DateTimeFormatter FMT_YYYY_MM = DateTimeFormatter.ofPattern("uuuu-MM");

	private YmUtil() {
	}

	/** {@code YYYYMM} を YearMonth に変換する。形式が不正なら InvalidRequestException。 */
	public static YearMonth parse(String yyyymm) {
		if (yyyymm == null || !YYYYMM.matcher(yyyymm).matches()) {
			throw new InvalidRequestException("年月の形式が不正です: " + yyyymm);
		}
		return YearMonth.parse(yyyymm, FMT_YYYYMM);
	}

	/** パラメータの年月を検証する。省略時は当月を返す。 */
	public static String normalize(String yyyymm) {
		if (yyyymm == null || yyyymm.isBlank()) {
			return toYyyymm(YearMonth.now());
		}
		parse(yyyymm);
		return yyyymm;
	}

	public static String toYyyymm(YearMonth ym) {
		return ym.format(FMT_YYYYMM);
	}

	/** {@code YYYYMM} → {@code YYYY-MM} */
	public static String toHyphen(String yyyymm) {
		return parse(yyyymm).format(FMT_YYYY_MM);
	}

	/** {@code YYYY-MM} → {@code YYYYMM} */
	public static String fromHyphen(String yyyyHyphenMm) {
		return YearMonth.parse(yyyyHyphenMm, FMT_YYYY_MM).format(FMT_YYYYMM);
	}

	/** {@code YYYYMM} → {@code YYYY.M} */
	public static String toDisplay(String yyyymm) {
		YearMonth ym = parse(yyyymm);
		return ym.getYear() + "." + ym.getMonthValue();
	}

	public static String prev(String yyyymm) {
		return toYyyymm(parse(yyyymm).minusMonths(1));
	}

	public static String next(String yyyymm) {
		return toYyyymm(parse(yyyymm).plusMonths(1));
	}
}
