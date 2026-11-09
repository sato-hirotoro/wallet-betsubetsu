package com.example.demo.dto;

/**
 * 精算の向き。
 */
public enum Direction {

	/** 妻→夫（妻が夫に払う） */
	TSUMA_TO_OTTO("夫◀妻"),
	/** 夫→妻（夫が妻に払う） */
	OTTO_TO_TSUMA("夫▶妻"),
	/** 精算なし */
	NONE("精算なし");

	private final String label;

	Direction(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
