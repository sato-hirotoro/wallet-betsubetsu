package com.example.demo.form;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 支払い登録（IMG-02）の1行。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeisaiRow {

	@NotNull
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate shiharaiDd;

	@NotNull
	@Min(1)
	private Integer kingaku;

	@Size(max = 50)
	private String biko;
}
