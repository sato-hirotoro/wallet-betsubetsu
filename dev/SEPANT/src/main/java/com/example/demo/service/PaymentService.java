package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.HendohiMeisai;
import com.example.demo.entity.Himoku;
import com.example.demo.entity.User;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.form.MeisaiRow;
import com.example.demo.form.PaymentForm;
import com.example.demo.repository.HendohiMeisaiRepository;
import com.example.demo.repository.HimokuRepository;
import com.example.demo.repository.UserRepository;

/**
 * 支払い登録（F-02）。
 */
@Service
@Transactional(readOnly = true)
public class PaymentService {

	/** セル（年月・支払った人・費目）の表示用の情報。 */
	public record Cell(User user, Himoku himoku) {
	}

	private final UserRepository userRepository;
	private final HimokuRepository himokuRepository;
	private final HendohiMeisaiRepository hendohiMeisaiRepository;
	private final SeisanService seisanService;

	public PaymentService(UserRepository userRepository, HimokuRepository himokuRepository,
			HendohiMeisaiRepository hendohiMeisaiRepository, SeisanService seisanService) {
		this.userRepository = userRepository;
		this.himokuRepository = himokuRepository;
		this.hendohiMeisaiRepository = hendohiMeisaiRepository;
		this.seisanService = seisanService;
	}

	/** 利用者・費目の存在と、費目の区分が 01・02 であることをチェックする。 */
	public Cell getCell(Integer userId, String himokuCd) {
		if (userId == null || himokuCd == null) {
			throw new InvalidRequestException("利用者または費目が指定されていません");
		}
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new InvalidRequestException("存在しない利用者: " + userId));
		Himoku himoku = himokuRepository.findById(himokuCd)
				.orElseThrow(() -> new InvalidRequestException("存在しない費目: " + himokuCd));
		if (!himoku.getKubunEnum().isHendohi()) {
			throw new InvalidRequestException("変動費の費目ではありません: " + himokuCd);
		}
		return new Cell(user, himoku);
	}

	/** セルの明細を支払日順に返す。 */
	public List<HendohiMeisai> getMeisai(String ym, Integer userId, String himokuCd) {
		return hendohiMeisaiRepository.findCell(ym, userId, himokuCd);
	}

	/** 精算済みチェックの後、セルの明細を置き換える。 */
	@Transactional
	public void replace(PaymentForm form) {
		seisanService.checkNotSettled(form.getYm());
		getCell(form.getUserId(), form.getHimokuCd());

		hendohiMeisaiRepository.deleteCell(form.getYm(), form.getUserId(), form.getHimokuCd());

		List<HendohiMeisai> entities = form.getMeisaiList().stream()
				.map(row -> toEntity(form, row))
				.toList();
		hendohiMeisaiRepository.saveAll(entities);
	}

	private static HendohiMeisai toEntity(PaymentForm form, MeisaiRow row) {
		HendohiMeisai e = new HendohiMeisai();
		e.setTaishoYymm(form.getYm());
		e.setUserId(form.getUserId());
		e.setHimokuCd(form.getHimokuCd());
		e.setShiharaiDd(row.getShiharaiDd());
		e.setKingaku(row.getKingaku());
		e.setBiko(blankToNull(row.getBiko()));
		return e;
	}

	static String blankToNull(String s) {
		return s == null || s.isBlank() ? null : s;
	}
}
