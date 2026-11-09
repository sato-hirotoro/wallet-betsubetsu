package com.example.demo.entity;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 精算（t_seisan）。行があれば精算済み。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("t_seisan")
public class Seisan {

	@Id
	private String taishoYymm;
	private LocalDate seisanDd;
}
