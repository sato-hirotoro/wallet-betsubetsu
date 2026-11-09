package com.example.demo.service.calc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.demo.common.Kubun;
import com.example.demo.dto.Direction;
import com.example.demo.dto.SeisanInput;
import com.example.demo.dto.SeisanInput.Shiharai;
import com.example.demo.dto.SeisanResult;

/**
 * SeisanCalculator の単体テスト（処理設計書 4.5）。
 */
class SeisanCalculatorTest {

	private static final int OTTO = 1;
	private static final int TSUMA = 2;

	private final SeisanCalculator calculator = new SeisanCalculator();

	private static SeisanResult calc(SeisanCalculator c, Shiharai... list) {
		return c.calculate(new SeisanInput(List.of(list)));
	}

	private static Shiharai s(int user, Kubun kubun, int kingaku) {
		return new Shiharai(user, kubun, kingaku);
	}

	@Test
	void 折半のみ_夫だけが払った場合は妻から夫へ半額() {
		SeisanResult r = calc(calculator, s(OTTO, Kubun.HENDO_SEPPAN, 10_000));
		assertThat(r.direction()).isEqualTo(Direction.TSUMA_TO_OTTO);
		assertThat(r.amount()).isEqualTo(5_000);
	}

	@Test
	void 端数_奇数のとき夫の折半額が1円多い() {
		SeisanResult r = calc(calculator, s(TSUMA, Kubun.HENDO_SEPPAN, 10_001));
		assertThat(r.seppanGaku().otto()).isEqualTo(5_001);
		assertThat(r.seppanGaku().tsuma()).isEqualTo(5_000);
	}

	@Test
	void 端数_マイナスでも妻の折半額が夫以下() {
		SeisanResult r = calc(calculator, s(OTTO, Kubun.SHUNYU, 1_001));
		assertThat(r.seppanGaku().tsuma()).isLessThanOrEqualTo(r.seppanGaku().otto());
		assertThat(r.seppanGaku().total()).isEqualTo(-1_001);
	}

	@Test
	void 収入_受け取った人の支払額から引かれる() {
		SeisanResult r = calc(calculator,
				s(OTTO, Kubun.KOTEI_SEPPAN, 80_000),
				s(OTTO, Kubun.SHUNYU, 20_000));
		assertThat(r.shiharai().otto()).isEqualTo(60_000);
		assertThat(r.direction()).isEqualTo(Direction.TSUMA_TO_OTTO);
		assertThat(r.amount()).isEqualTo(30_000);
	}

	@Test
	void 立替_全額が相手の負担額に加わる() {
		SeisanResult r = calc(calculator, s(OTTO, Kubun.HENDO_TATEKAE, 3_000));
		assertThat(r.futan().tsuma()).isEqualTo(3_000);
		assertThat(r.direction()).isEqualTo(Direction.TSUMA_TO_OTTO);
		assertThat(r.amount()).isEqualTo(3_000);
	}

	@Test
	void 当番_2000円の当番で精算額が2000円動く() {
		SeisanResult base = calc(calculator, s(OTTO, Kubun.HENDO_SEPPAN, 10_000));
		SeisanResult withToban = calc(calculator,
				s(OTTO, Kubun.HENDO_SEPPAN, 10_000),
				s(OTTO, Kubun.TOBAN, 2_000));
		// 精算額は 5,000 → 7,000 で2,000動き、負担額の差は4,000開く
		assertThat(withToban.amount() - base.amount()).isEqualTo(2_000);
		assertThat(withToban.futan().tsuma() - withToban.futan().otto()).isEqualTo(4_000);
	}

	@Test
	void 差額0_精算なし() {
		SeisanResult r = calc(calculator,
				s(OTTO, Kubun.HENDO_SEPPAN, 5_000),
				s(TSUMA, Kubun.HENDO_SEPPAN, 5_000));
		assertThat(r.direction()).isEqualTo(Direction.NONE);
		assertThat(r.amount()).isZero();
	}

	@Test
	void 合計0_夫と妻の差額の合計は常に0() {
		SeisanResult r = calc(calculator,
				s(OTTO, Kubun.HENDO_SEPPAN, 12_345),
				s(TSUMA, Kubun.KOTEI_SEPPAN, 7_777),
				s(TSUMA, Kubun.HENDO_TATEKAE, 1_111),
				s(OTTO, Kubun.KOTEI_TATEKAE, 2_222),
				s(TSUMA, Kubun.SHUNYU, 3_333),
				s(OTTO, Kubun.TOBAN, 500),
				s(TSUMA, Kubun.TOBAN, 1_500));
		assertThat(r.sagaku().otto() + r.sagaku().tsuma()).isZero();
	}

	@Test
	void 計算例_処理設計書4_4() {
		SeisanResult r = calc(calculator,
				s(OTTO, Kubun.HENDO_SEPPAN, 20_000),
				s(TSUMA, Kubun.HENDO_SEPPAN, 15_000),
				s(TSUMA, Kubun.HENDO_SEPPAN, 5_001),
				s(OTTO, Kubun.KOTEI_SEPPAN, 80_000),
				s(OTTO, Kubun.KOTEI_SEPPAN, 1_100),
				s(OTTO, Kubun.KOTEI_SEPPAN, 5_000),
				s(OTTO, Kubun.HENDO_TATEKAE, 3_000),
				s(TSUMA, Kubun.KOTEI_TATEKAE, 10_000),
				s(OTTO, Kubun.SHUNYU, 20_000),
				s(OTTO, Kubun.TOBAN, 2_000),
				s(TSUMA, Kubun.TOBAN, 2_000));

		assertThat(r.seppanTotal()).isEqualTo(126_101);
		assertThat(r.shunyuTotal()).isEqualTo(20_000);
		assertThat(r.seppanGaku().otto()).isEqualTo(53_051);
		assertThat(r.seppanGaku().tsuma()).isEqualTo(53_050);
		assertThat(r.shiharai().otto()).isEqualTo(89_100);
		assertThat(r.shiharai().tsuma()).isEqualTo(30_001);
		assertThat(r.futan().otto()).isEqualTo(63_051);
		assertThat(r.futan().tsuma()).isEqualTo(56_050);
		assertThat(r.direction()).isEqualTo(Direction.TSUMA_TO_OTTO);
		assertThat(r.amount()).isEqualTo(26_049);
	}
}
