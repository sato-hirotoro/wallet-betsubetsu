package com.example.demo.entity;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 変動費明細（t_hendohi_meisai） */
@Data
@NoArgsConstructor
@Table("t_hendohi_meisai")
public class HendohiMeisai {

	@Id
	private Integer meisaiId;
	private String taishoYymm;
	private Integer userId;
	private String himokuCd;
	private LocalDate shiharaiDd;
	private Integer kingaku;
	private String biko;
}
