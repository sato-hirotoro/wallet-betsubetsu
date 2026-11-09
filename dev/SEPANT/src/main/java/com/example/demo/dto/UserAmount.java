package com.example.demo.dto;

import com.example.demo.common.UserIds;

/**
 * 夫・妻それぞれの金額。
 */
public record UserAmount(int otto, int tsuma) {

	public int of(int userId) {
		return userId == UserIds.OTTO ? otto : tsuma;
	}

	public int total() {
		return otto + tsuma;
	}
}
