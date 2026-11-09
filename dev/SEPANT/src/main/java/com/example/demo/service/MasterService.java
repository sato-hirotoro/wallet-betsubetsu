package com.example.demo.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.common.Kubun;
import com.example.demo.entity.Himoku;
import com.example.demo.entity.User;
import com.example.demo.repository.HimokuRepository;
import com.example.demo.repository.UserRepository;

/**
 * マスタ（利用者・費目）の参照。
 */
@Service
@Transactional(readOnly = true)
public class MasterService {

	private final UserRepository userRepository;
	private final HimokuRepository himokuRepository;

	public MasterService(UserRepository userRepository, HimokuRepository himokuRepository) {
		this.userRepository = userRepository;
		this.himokuRepository = himokuRepository;
	}

	/** 利用者ID → 利用者名（ID順） */
	public Map<Integer, String> userNames() {
		Map<Integer, String> map = new LinkedHashMap<>();
		userRepository.findAll().stream()
				.sorted((a, b) -> a.getUserId().compareTo(b.getUserId()))
				.forEach(u -> map.put(u.getUserId(), u.getUserName()));
		return map;
	}

	public List<User> users() {
		return userRepository.findAll().stream()
				.sorted((a, b) -> a.getUserId().compareTo(b.getUserId()))
				.toList();
	}

	/** 費目（表示順） */
	public List<Himoku> himokuList() {
		return himokuRepository.findAllByOrderByHyojiJun();
	}

	/** 指定の区分の費目（表示順） */
	public List<Himoku> himokuList(Set<Kubun> kubuns) {
		return himokuList().stream().filter(h -> kubuns.contains(h.getKubunEnum())).toList();
	}

	/** 費目CD → 費目（表示順） */
	public Map<String, Himoku> himokuMap() {
		Map<String, Himoku> map = new LinkedHashMap<>();
		himokuList().forEach(h -> map.put(h.getHimokuCd(), h));
		return map;
	}
}
