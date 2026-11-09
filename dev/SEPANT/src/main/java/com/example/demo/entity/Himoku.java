package com.example.demo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import com.example.demo.common.Kubun;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 費目（m_himoku） */
@Data
@NoArgsConstructor
@Table("m_himoku")
public class Himoku {

	@Id
	private String himokuCd;
	private String himokuName;
	private String kubun;
	private Integer hyojiJun;

	@Transient
	public Kubun getKubunEnum() {
		return Kubun.fromCode(kubun);
	}
}
