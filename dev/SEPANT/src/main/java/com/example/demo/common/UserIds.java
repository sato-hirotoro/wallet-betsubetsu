package com.example.demo.common;

/**
 * 利用者ID（m_user.user_id）。DB設計書 4.1 の通り、1:夫、2:妻 で固定とする。
 */
public final class UserIds {

	public static final int OTTO = 1;
	public static final int TSUMA = 2;

	private UserIds() {
	}

	/** 相手の利用者IDを返す。 */
	public static int other(int userId) {
		return userId == OTTO ? TSUMA : OTTO;
	}
}
