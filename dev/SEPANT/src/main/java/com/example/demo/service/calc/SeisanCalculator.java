package com.example.demo.service.calc;

import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.example.demo.common.Kubun;
import com.example.demo.common.UserIds;
import com.example.demo.dto.Direction;
import com.example.demo.dto.SeisanInput;
import com.example.demo.dto.SeisanInput.Shiharai;
import com.example.demo.dto.SeisanResult;
import com.example.demo.dto.UserAmount;

/**
 * 精算額の計算（処理設計書 4章）。DBに依存しない純粋な計算クラス。
 */
@Component
public class SeisanCalculator {

	public SeisanResult calculate(SeisanInput input) {
		// 利用者ごとの区分別の合計
		UserAmount seppanPaid = sumByUser(input, Kubun::isSeppan);
		UserAmount tatekae = sumByUser(input, Kubun::isTatekae);
		UserAmount shunyu = sumByUser(input, k -> k == Kubun.SHUNYU);
		UserAmount toban = sumByUser(input, k -> k == Kubun.TOBAN);

		// 1・2 折半対象の合計 S、収入の合計 I
		int s = seppanPaid.total();
		int i = shunyu.total();

		// 3・4 折半額（妻は切り捨て、端数は夫が持つ）
		int taisho = s - i;
		int tsumaSeppan = Math.floorDiv(taisho, 2);
		int ottoSeppan = taisho - tsumaSeppan;
		UserAmount seppanGaku = new UserAmount(ottoSeppan, tsumaSeppan);

		// 5 支払額 = 自分の区分01・03 + 自分の区分02・04 − 自分の区分05
		UserAmount shiharai = new UserAmount(
				seppanPaid.otto() + tatekae.otto() - shunyu.otto(),
				seppanPaid.tsuma() + tatekae.tsuma() - shunyu.tsuma());

		// 6 負担額 = 自分の折半額 + 相手の区分02・04 − 自分の区分06 + 相手の区分06
		UserAmount futan = new UserAmount(
				ottoSeppan + tatekae.tsuma() - toban.otto() + toban.tsuma(),
				tsumaSeppan + tatekae.otto() - toban.tsuma() + toban.otto());

		// 7 差額
		UserAmount sagaku = new UserAmount(
				shiharai.otto() - futan.otto(),
				shiharai.tsuma() - futan.tsuma());

		// 8・9 精算の向き・精算額
		int ottoSagaku = sagaku.otto();
		Direction direction = ottoSagaku > 0 ? Direction.TSUMA_TO_OTTO
				: ottoSagaku < 0 ? Direction.OTTO_TO_TSUMA
				: Direction.NONE;

		return new SeisanResult(s, i, seppanGaku, seppanPaid, shunyu, tatekae, toban,
				shiharai, futan, sagaku, direction, Math.abs(ottoSagaku));
	}

	private static UserAmount sumByUser(SeisanInput input, Predicate<Kubun> filter) {
		int otto = 0;
		int tsuma = 0;
		for (Shiharai s : input.shiharaiList()) {
			if (!filter.test(s.kubun())) {
				continue;
			}
			if (s.userId() == UserIds.OTTO) {
				otto += s.kingaku();
			} else if (s.userId() == UserIds.TSUMA) {
				tsuma += s.kingaku();
			}
		}
		return new UserAmount(otto, tsuma);
	}
}
