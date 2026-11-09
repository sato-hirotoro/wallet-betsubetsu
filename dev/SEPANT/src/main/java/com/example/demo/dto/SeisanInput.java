package com.example.demo.dto;

import java.util.List;

import com.example.demo.common.Kubun;

/**
 * 精算額の計算の入力（処理設計書 4.2）。
 *
 * @param shiharaiList 支払い（利用者・区分・金額）の一覧
 */
public record SeisanInput(List<Shiharai> shiharaiList) {

	/**
	 * 1件の支払い。
	 *
	 * @param userId 区分01〜04は支払った人、05は受け取った人、06は担当者
	 */
	public record Shiharai(int userId, Kubun kubun, int kingaku) {
	}
}
