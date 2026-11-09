package com.example.demo.exception;

/**
 * 精算済みの月を変更しようとしたときの業務例外。ホームへリダイレクトして E701 を表示する。
 */
public class SeisanzumiException extends RuntimeException {

	private final String ym;

	public SeisanzumiException(String ym) {
		super("精算済みの月です: " + ym);
		this.ym = ym;
	}

	public String getYm() {
		return ym;
	}
}
