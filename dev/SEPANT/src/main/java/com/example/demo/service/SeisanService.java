package com.example.demo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.common.Kubun;
import com.example.demo.common.UserIds;
import com.example.demo.dto.ResultView;
import com.example.demo.dto.SeisanInput;
import com.example.demo.dto.SeisanInput.Shiharai;
import com.example.demo.dto.SeisanResult;
import com.example.demo.entity.HendohiMeisai;
import com.example.demo.entity.Himoku;
import com.example.demo.entity.KoteiSettei;
import com.example.demo.entity.Seisan;
import com.example.demo.exception.SeisanzumiException;
import com.example.demo.repository.HendohiMeisaiRepository;
import com.example.demo.repository.KoteiSetteiRepository;
import com.example.demo.repository.SeisanRepository;
import com.example.demo.service.calc.SeisanCalculator;

/**
 * 精算額の計算と精算（F-05・F-06・F-07）。
 */
@Service
@Transactional(readOnly = true)
public class SeisanService {

	private final MasterService masterService;
	private final HendohiMeisaiRepository hendohiMeisaiRepository;
	private final KoteiSetteiRepository koteiSetteiRepository;
	private final SeisanRepository seisanRepository;
	private final JdbcAggregateTemplate jdbcAggregateTemplate;
	private final SeisanCalculator seisanCalculator;

	public SeisanService(MasterService masterService, HendohiMeisaiRepository hendohiMeisaiRepository,
			KoteiSetteiRepository koteiSetteiRepository, SeisanRepository seisanRepository,
			JdbcAggregateTemplate jdbcAggregateTemplate, SeisanCalculator seisanCalculator) {
		this.masterService = masterService;
		this.hendohiMeisaiRepository = hendohiMeisaiRepository;
		this.koteiSetteiRepository = koteiSetteiRepository;
		this.seisanRepository = seisanRepository;
		this.jdbcAggregateTemplate = jdbcAggregateTemplate;
		this.seisanCalculator = seisanCalculator;
	}

	/** 対象月のデータを読み、精算額を計算する。 */
	public SeisanResult calculate(String ym) {
		return seisanCalculator.calculate(buildInput(ym, masterService.himokuMap()));
	}

	/** 計算結果（IMG-04）の表示データ。 */
	public ResultView getResultView(String ym) {
		Map<String, Himoku> himokuMap = masterService.himokuMap();
		List<HendohiMeisai> meisaiList = hendohiMeisaiRepository.findByTaishoYymm(ym);
		List<KoteiSettei> setteiList = koteiSetteiRepository.findValid(ym);

		List<ResultView.Row> rows = new ArrayList<>();
		for (Himoku h : himokuMap.values()) {
			Kubun kubun = h.getKubunEnum();
			if (!kubun.isSeppan() && !kubun.isTatekae()) {
				continue;
			}
			int otto = 0;
			int tsuma = 0;
			if (kubun.isHendohi()) {
				for (HendohiMeisai m : meisaiList) {
					if (m.getHimokuCd().equals(h.getHimokuCd())) {
						if (m.getUserId() == UserIds.OTTO) otto += m.getKingaku();
						else tsuma += m.getKingaku();
					}
				}
			} else {
				for (KoteiSettei s : setteiList) {
					if (s.getHimokuCd().equals(h.getHimokuCd())) {
						if (s.getUserId() == UserIds.OTTO) otto += s.getKingaku();
						else tsuma += s.getKingaku();
					}
				}
			}
			rows.add(new ResultView.Row(h.getHimokuName(), kubun.getLabel(), otto, tsuma));
		}

		SeisanResult result = seisanCalculator.calculate(toInput(himokuMap, meisaiList, setteiList));
		return new ResultView(ym, isSettled(ym), rows, result);
	}

	public boolean isSettled(String ym) {
		return seisanRepository.existsById(ym);
	}

	/** 精算済みチェックの後、t_seisan に登録する。 */
	@Transactional
	public void settle(String ym) {
		checkNotSettled(ym);
		jdbcAggregateTemplate.insert(new Seisan(ym, LocalDate.now()));
	}

	/** 精算済みの月であれば SeisanzumiException を投げる。 */
	public void checkNotSettled(String ym) {
		if (isSettled(ym)) {
			throw new SeisanzumiException(ym);
		}
	}

	private SeisanInput buildInput(String ym, Map<String, Himoku> himokuMap) {
		return toInput(himokuMap, hendohiMeisaiRepository.findByTaishoYymm(ym), koteiSetteiRepository.findValid(ym));
	}

	private static SeisanInput toInput(Map<String, Himoku> himokuMap, List<HendohiMeisai> meisaiList,
			List<KoteiSettei> setteiList) {
		List<Shiharai> list = new ArrayList<>();
		for (HendohiMeisai m : meisaiList) {
			list.add(new Shiharai(m.getUserId(), himokuMap.get(m.getHimokuCd()).getKubunEnum(), m.getKingaku()));
		}
		for (KoteiSettei s : setteiList) {
			list.add(new Shiharai(s.getUserId(), himokuMap.get(s.getHimokuCd()).getKubunEnum(), s.getKingaku()));
		}
		return new SeisanInput(list);
	}
}
