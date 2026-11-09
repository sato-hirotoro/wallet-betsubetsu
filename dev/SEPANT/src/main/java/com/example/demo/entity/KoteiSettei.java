package com.example.demo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 固定設定（t_kotei_settei） */
@Data
@NoArgsConstructor
@Table("t_kotei_settei")
public class KoteiSettei {

	@Id
	private Integer setteiId;
	private String himokuCd;
	/** 区分03・04は支払う人、05は受け取る人、06は担当者 */
	private Integer userId;
	private Integer kingaku;
	private String kaishiMm;
	private String shuryoMm;
	private String biko;
}
