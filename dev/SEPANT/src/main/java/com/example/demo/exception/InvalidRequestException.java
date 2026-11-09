package com.example.demo.exception;

/**
 * 不正なパラメータ（年月の形式、存在しない利用者・費目など）。エラー画面（E901）を表示する。
 */
public class InvalidRequestException extends RuntimeException {

	public InvalidRequestException(String message) {
		super(message);
	}
}
