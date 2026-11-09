package com.example.demo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.common.Kubun;
import com.example.demo.common.UserIds;
import com.example.demo.common.YmUtil;
import com.example.demo.dto.HomeView;
import com.example.demo.dto.HomeView.CellRow;
import com.example.demo.dto.HomeView.SetteiRow;
import com.example.demo.dto.HomeView.TobanRow;
import com.example.demo.entity.HendohiMeisai;
import com.example.demo.entity.Himoku;
import com.example.demo.entity.KoteiSettei;
import com.example.demo.repository.HendohiMeisaiRepository;
import com.example.demo.repository.KoteiSetteiRepository;

/**
 * ホーム表示（F-03・F-05）。
 */
@Service
@Transactional(readOnly = true)
public class HomeService {

	private final MasterService masterService;
	private final HendohiMeisaiRepository hendohiMeisaiRepository;
	private final KoteiSetteiRepository koteiSetteiRepository;
	private final SeisanService seisanService;
	private final MessageSource messageSource;

	public HomeService(MasterService masterService, HendohiMeisaiRepository hendohiMeisaiRepository,
			KoteiSetteiRepository koteiSetteiRepository, SeisanService seisanService, MessageSource messageSource) {
		this.masterService = masterService;
		this.hendohiMeisaiRepository = hendohiMeisaiRepository;
		this.koteiSetteiRepository = koteiSetteiRepository;
		this.seisanService = seisanService;
		this.messageSource = messageSource;
	}

	public HomeView getHome(String ym) {
		List<Himoku> himokuList = masterService.himokuList();
		Map<Integer, String> userNames = masterService.userNames();
		List<HendohiMeisai> meisaiList = hendohiMeisaiRepository.findByTaishoYymm(ym);
		List<KoteiSettei> setteiList = koteiSetteiRepository.findValid(ym).stream()
				.sorted(Comparator.comparing(KoteiSettei::getKaishiMm))
				.toList();

		List<CellRow> seppanRows = new ArrayList<>();
		List<CellRow> tatekaeRows = new ArrayList<>();
		List<SetteiRow> koteiRows = new ArrayList<>();
		List<SetteiRow> shunyuRows = new ArrayList<>();
		List<TobanRow> tobanRows = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		for (Himoku h : himokuList) {
			Kubun kubun = h.getKubunEnum();
			switch (kubun) {
			case HENDO_SEPPAN, HENDO_TATEKAE -> {
				int otto = 0;
				int tsuma = 0;
				for (HendohiMeisai m : meisaiList) {
					if (m.getHimokuCd().equals(h.getHimokuCd())) {
						if (m.getUserId() == UserIds.OTTO) otto += m.getKingaku();
						else tsuma += m.getKingaku();
					}
				}
				CellRow row = new CellRow(h.getHimokuCd(), h.getHimokuName(), otto, tsuma);
				(kubun == Kubun.HENDO_SEPPAN ? seppanRows : tatekaeRows).add(row);
			}
			case KOTEI_SEPPAN, KOTEI_TATEKAE, SHUNYU -> {
				List<KoteiSettei> valid = filter(setteiList, h);
				for (KoteiSettei s : valid) {
					SetteiRow row = new SetteiRow(h.getHimokuName(), userNames.get(s.getUserId()), s.getKingaku());
					(kubun == Kubun.SHUNYU ? shunyuRows : koteiRows).add(row);
				}
				if (valid.isEmpty() && kubun != Kubun.SHUNYU) {
					warnings.add(message("W002", h.getHimokuName()));
				}
			}
			case TOBAN -> {
				List<KoteiSettei> valid = filter(setteiList, h);
				boolean otto = valid.stream().anyMatch(s -> s.getUserId() == UserIds.OTTO);
				boolean tsuma = valid.stream().anyMatch(s -> s.getUserId() == UserIds.TSUMA);
				tobanRows.add(new TobanRow(h.getHimokuName(), otto, tsuma));
				if (valid.isEmpty()) {
					warnings.add(message("W001", h.getHimokuName()));
				}
			}
			}
		}

		return new HomeView(ym, YmUtil.prev(ym), YmUtil.next(ym), seisanService.isSettled(ym),
				seisanService.calculate(ym), seppanRows, tatekaeRows, koteiRows, shunyuRows, tobanRows, warnings);
	}

	private static List<KoteiSettei> filter(List<KoteiSettei> list, Himoku h) {
		return list.stream().filter(s -> s.getHimokuCd().equals(h.getHimokuCd())).toList();
	}

	private String message(String code, Object... args) {
		return messageSource.getMessage(code, args, Locale.JAPAN);
	}
}
