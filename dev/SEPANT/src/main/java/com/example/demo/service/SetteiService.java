package com.example.demo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.common.Kubun;
import com.example.demo.common.YmUtil;
import com.example.demo.entity.Himoku;
import com.example.demo.entity.KoteiSettei;
import com.example.demo.form.SetteiForm;
import com.example.demo.form.SetteiRow;
import com.example.demo.repository.KoteiSetteiRepository;

/**
 * 固定設定登録（F-04）。
 */
@Service
@Transactional(readOnly = true)
public class SetteiService {

	private final MasterService masterService;
	private final KoteiSetteiRepository koteiSetteiRepository;

	public SetteiService(MasterService masterService, KoteiSetteiRepository koteiSetteiRepository) {
		this.masterService = masterService;
		this.koteiSetteiRepository = koteiSetteiRepository;
	}

	/** 全ての固定設定を、区分ごとのセクションに分け、費目の表示順・開始月順で返す。 */
	public SetteiForm getAll(String ym) {
		Map<String, Himoku> himokuMap = masterService.himokuMap();
		List<KoteiSettei> all = new ArrayList<>(koteiSetteiRepository.findAll());
		all.sort(Comparator
				.comparing((KoteiSettei s) -> himokuMap.get(s.getHimokuCd()).getHyojiJun())
				.thenComparing(KoteiSettei::getKaishiMm));

		SetteiForm form = new SetteiForm();
		form.setYm(ym);
		for (KoteiSettei s : all) {
			Kubun kubun = himokuMap.get(s.getHimokuCd()).getKubunEnum();
			SetteiRow row = new SetteiRow(s.getHimokuCd(), s.getUserId(), s.getKingaku(),
					YmUtil.toHyphen(s.getKaishiMm()), YmUtil.toHyphen(s.getShuryoMm()), s.getBiko());
			switch (kubun) {
			case SHUNYU -> form.getShunyuList().add(row);
			case TOBAN -> form.getTobanList().add(row);
			default -> form.getKoteiList().add(row);
			}
		}
		return form;
	}

	/** 固定設定を全て置き換える。 */
	@Transactional
	public void replaceAll(SetteiForm form) {
		koteiSetteiRepository.deleteAll();

		List<KoteiSettei> entities = new ArrayList<>();
		for (List<SetteiRow> rows : List.of(form.getKoteiList(), form.getShunyuList(), form.getTobanList())) {
			for (SetteiRow row : rows) {
				KoteiSettei e = new KoteiSettei();
				e.setHimokuCd(row.getHimokuCd());
				e.setUserId(row.getUserId());
				e.setKingaku(row.getKingaku());
				e.setKaishiMm(YmUtil.fromHyphen(row.getKaishiMm()));
				e.setShuryoMm(YmUtil.fromHyphen(row.getShuryoMm()));
				e.setBiko(PaymentService.blankToNull(row.getBiko()));
				entities.add(e);
			}
		}
		koteiSetteiRepository.saveAll(entities);
	}
}
