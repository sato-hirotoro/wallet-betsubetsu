package com.example.demo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 利用者（m_user） */
@Data
@NoArgsConstructor
@Table("m_user")
public class User {

	@Id
	private Integer userId;
	private String userName;
}
