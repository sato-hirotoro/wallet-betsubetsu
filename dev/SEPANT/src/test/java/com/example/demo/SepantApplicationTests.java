package com.example.demo;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 画面と処理の結合テスト（H2・MySQL互換モード）。処理設計書 4.4 の計算例を画面から登録して確認する。
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SepantApplicationTests {

	private static final String YM = "202606";

	@Autowired
	private MockMvc mvc;

	@Test
	@Order(1)
	void ルートはホームへリダイレクト() throws Exception {
		mvc.perform(get("/")).andExpect(redirectedUrl("/home"));
	}

	@Test
	@Order(2)
	void ホーム_データなしでも表示でき警告が出る() throws Exception {
		mvc.perform(get("/home").param("ym", YM))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("2026.6")))
				.andExpect(content().string(containsString("皿洗いの担当が未設定です。")))
				.andExpect(content().string(containsString("家賃の金額が未設定です。")));
	}

	@Test
	@Order(3)
	void 不正な年月はエラー画面() throws Exception {
		mvc.perform(get("/home").param("ym", "202613"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("不正なリクエストです。")));
	}

	@Test
	@Order(4)
	void 固定費設定_入力チェック() throws Exception {
		mvc.perform(post("/settei").param("register", "")
				.param("ym", YM)
				.param("koteiList[0].himokuCd", "005")
				.param("koteiList[0].userId", "1")
				.param("koteiList[0].kingaku", "0")
				.param("koteiList[0].kaishiMm", "2026-06")
				.param("koteiList[0].shuryoMm", "2026-04")
				.param("koteiList[1].himokuCd", "001")
				.param("koteiList[1].userId", "1")
				.param("koteiList[1].kingaku", "1")
				.param("koteiList[1].kaishiMm", "2026-04")
				.param("koteiList[1].shuryoMm", "2027-03"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("固定費 1行目:金額は1以上の整数で入力してください。")))
				.andExpect(content().string(containsString("固定費 1行目:終了月は開始月以降を入力してください。")))
				.andExpect(content().string(containsString("固定費 2行目:不正なリクエストです。")));
	}

	@Test
	@Order(5)
	void 計算例を登録して精算まで() throws Exception {
		// 固定費・収入・当番
		mvc.perform(post("/settei").param("register", "")
				.param("ym", YM)
				.param("koteiList[0].himokuCd", "005").param("koteiList[0].userId", "1")
				.param("koteiList[0].kingaku", "80000")
				.param("koteiList[0].kaishiMm", "2026-04").param("koteiList[0].shuryoMm", "2027-03")
				.param("koteiList[1].himokuCd", "006").param("koteiList[1].userId", "1")
				.param("koteiList[1].kingaku", "1100")
				.param("koteiList[1].kaishiMm", "2026-04").param("koteiList[1].shuryoMm", "2027-03")
				.param("koteiList[2].himokuCd", "007").param("koteiList[2].userId", "1")
				.param("koteiList[2].kingaku", "5000")
				.param("koteiList[2].kaishiMm", "2026-04").param("koteiList[2].shuryoMm", "2027-03")
				.param("koteiList[3].himokuCd", "008").param("koteiList[3].userId", "2")
				.param("koteiList[3].kingaku", "10000")
				.param("koteiList[3].kaishiMm", "2026-01").param("koteiList[3].shuryoMm", "2026-12")
				.param("koteiList[3].biko", "妻の立替")
				.param("shunyuList[0].himokuCd", "009").param("shunyuList[0].userId", "1")
				.param("shunyuList[0].kingaku", "20000")
				.param("shunyuList[0].kaishiMm", "2026-04").param("shunyuList[0].shuryoMm", "2027-03")
				.param("tobanList[0].himokuCd", "010").param("tobanList[0].userId", "1")
				.param("tobanList[0].kingaku", "2000")
				.param("tobanList[0].kaishiMm", "2026-04").param("tobanList[0].shuryoMm", "2026-09")
				.param("tobanList[1].himokuCd", "011").param("tobanList[1].userId", "2")
				.param("tobanList[1].kingaku", "2000")
				.param("tobanList[1].kaishiMm", "2026-04").param("tobanList[1].shuryoMm", "2026-09"))
				.andExpect(redirectedUrl("/complete?type=settei&ym=" + YM));

		mvc.perform(get("/settei").param("ym", YM))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("妻の立替")))
				.andExpect(content().string(containsString("2027.3")));

		// 変動費
		payment(1, "001", "2026-06-10", "20000");
		payment(2, "001", "2026-06-15", "15000");
		payment(2, "002", "2026-06-20", "5001");
		payment(1, "004", "2026-06-25", "3000");

		// 支払日のチェック
		mvc.perform(post("/payment").param("register", "")
				.param("ym", YM).param("userId", "1").param("himokuCd", "003")
				.param("meisaiList[0].shiharaiDd", "2026-07-01")
				.param("meisaiList[0].kingaku", ""))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("1行目:支払日は2026.6の日付を入力してください。")))
				.andExpect(content().string(containsString("1行目:金額を入力してください。")));

		// 支払い登録画面の表示
		mvc.perform(get("/payment").param("ym", YM).param("userId", "1").param("himokuCd", "001"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("value=\"20000\"")));

		// ホーム・計算結果
		mvc.perform(get("/home").param("ym", YM))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("夫◀妻")))
				.andExpect(content().string(containsString("¥26,049")))
				.andExpect(content().string(containsString("未精算")));

		mvc.perform(get("/result").param("ym", YM))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("¥53,051")))
				.andExpect(content().string(containsString("¥63,051")));

		mvc.perform(get("/seisan/confirm").param("ym", YM))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("夫◀妻 26,049円精算済にします。よろしいですか。")));

		// 精算
		mvc.perform(post("/seisan").param("ym", YM))
				.andExpect(redirectedUrl("/complete?type=seisan&ym=" + YM));
		mvc.perform(get("/complete").param("type", "seisan").param("ym", YM))
				.andExpect(content().string(containsString("精算が完了しました。")));
		mvc.perform(get("/home").param("ym", YM))
				.andExpect(content().string(containsString("精算済")));

		// 精算済みの月は変更できない
		mvc.perform(post("/seisan").param("ym", YM))
				.andExpect(redirectedUrl("/home?ym=" + YM))
				.andExpect(flash().attribute("errorMessage", "精算済みの月は変更できません。"));
		mvc.perform(get("/payment").param("ym", YM).param("userId", "1").param("himokuCd", "001"))
				.andExpect(redirectedUrl("/home?ym=" + YM));
	}

	@Test
	@Order(6)
	void 行の追加と削除() throws Exception {
		mvc.perform(post("/payment").param("addRow", "")
				.param("ym", "202607").param("userId", "1").param("himokuCd", "001")
				.param("meisaiList[0].shiharaiDd", "2026-07-01").param("meisaiList[0].kingaku", "100"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("meisaiList[1].kingaku")));
	}

	@Test
	@Order(7)
	void 完了画面_不明なtypeはエラー() throws Exception {
		mvc.perform(get("/complete").param("type", "xxx").param("ym", YM))
				.andExpect(status().isBadRequest());
	}

	private void payment(int userId, String himokuCd, String date, String kingaku) throws Exception {
		mvc.perform(post("/payment").param("register", "")
				.param("ym", YM).param("userId", String.valueOf(userId)).param("himokuCd", himokuCd)
				.param("meisaiList[0].shiharaiDd", date)
				.param("meisaiList[0].kingaku", kingaku))
				.andExpect(redirectedUrl("/complete?type=payment&ym=" + YM));
	}
}
