package com.example.demo.validator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.example.demo.common.Kubun;
import com.example.demo.common.YmUtil;
import com.example.demo.entity.Himoku;
import com.example.demo.form.SetteiForm;
import com.example.demo.form.SetteiRow;
import com.example.demo.repository.HimokuRepository;

/**
 * 固定費設定の相関チェック。
 * <ul>
 * <li>費目がセクションの区分であること（E901）</li>
 * <li>終了月が開始月以降であること（E102）</li>
 * <li>同じ費目で期間が重なる行がないこと（E103、処理設計書 5.4）</li>
 * </ul>
 */
@Component
public class SetteiFormValidator implements Validator {

	/** セクションごとに登録できる区分。 */
	public static final Map<String, Set<Kubun>> SECTION_KUBUN = Map.of(
			SetteiForm.KOTEI, Set.of(Kubun.KOTEI_SEPPAN, Kubun.KOTEI_TATEKAE),
			SetteiForm.SHUNYU, Set.of(Kubun.SHUNYU),
			SetteiForm.TOBAN, Set.of(Kubun.TOBAN));

	private final HimokuRepository himokuRepository;

	public SetteiFormValidator(HimokuRepository himokuRepository) {
		this.himokuRepository = himokuRepository;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return SetteiForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		SetteiForm form = (SetteiForm) target;
		Map<String, Himoku> himokuMap = himokuRepository.findAll().stream()
				.collect(Collectors.toMap(Himoku::getHimokuCd, Function.identity()));

		// 期間の重複チェックの対象（形式が正しい行のみ）
		List<SetteiRow> validRows = new ArrayList<>();

		for (String listName : List.of(SetteiForm.KOTEI, SetteiForm.SHUNYU, SetteiForm.TOBAN)) {
			List<SetteiRow> rows = form.listOf(listName);
			for (int i = 0; i < rows.size(); i++) {
				SetteiRow row = rows.get(i);
				String prefix = listName + "[" + i + "].";

				// 費目がセクションの区分であること
				if (row.getHimokuCd() != null && !row.getHimokuCd().isBlank()) {
					Himoku himoku = himokuMap.get(row.getHimokuCd());
					if (himoku == null || !SECTION_KUBUN.get(listName).contains(himoku.getKubunEnum())) {
						errors.rejectValue(prefix + "himokuCd", "E901");
						continue;
					}
				}

				// 終了月が開始月以降であること
				boolean kaishiOk = isYm(row.getKaishiMm()) && !errors.hasFieldErrors(prefix + "kaishiMm");
				boolean shuryoOk = isYm(row.getShuryoMm()) && !errors.hasFieldErrors(prefix + "shuryoMm");
				if (kaishiOk && shuryoOk) {
					if (row.getShuryoMm().compareTo(row.getKaishiMm()) < 0) {
						errors.rejectValue(prefix + "shuryoMm", "E102");
					} else if (row.getHimokuCd() != null && himokuMap.containsKey(row.getHimokuCd())) {
						validRows.add(row);
					}
				}
			}
		}

		// 期間の重複チェック（全セクションを通して、費目ごと）
		Map<String, List<SetteiRow>> byHimoku = validRows.stream()
				.collect(Collectors.groupingBy(SetteiRow::getHimokuCd, LinkedHashMap::new, Collectors.toList()));
		byHimoku.forEach((himokuCd, rows) -> {
			List<SetteiRow> sorted = rows.stream()
					.sorted(Comparator.comparing(SetteiRow::getKaishiMm))
					.toList();
			for (int i = 1; i < sorted.size(); i++) {
				if (sorted.get(i - 1).getShuryoMm().compareTo(sorted.get(i).getKaishiMm()) >= 0) {
					errors.reject("E103", new Object[] { himokuMap.get(himokuCd).getHimokuName() }, null);
					break;
				}
			}
		});
	}

	private static boolean isYm(String value) {
		return value != null && value.matches(YmUtil.YYYY_MM_REGEXP);
	}
}
