package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.HendohiMeisai;

@Repository
public interface HendohiMeisaiRepository extends ListCrudRepository<HendohiMeisai, Integer> {

	List<HendohiMeisai> findByTaishoYymm(String taishoYymm);

	/** セルの明細の取得（処理設計書 6.1） */
	@Query("""
			SELECT * FROM t_hendohi_meisai
			 WHERE taisho_yymm = :ym AND user_id = :userId AND himoku_cd = :himokuCd
			 ORDER BY shiharai_dd, meisai_id
			""")
	List<HendohiMeisai> findCell(@Param("ym") String ym, @Param("userId") Integer userId,
			@Param("himokuCd") String himokuCd);

	/** セルの明細の削除（処理設計書 6.2） */
	@Modifying
	@Query("""
			DELETE FROM t_hendohi_meisai
			 WHERE taisho_yymm = :ym AND user_id = :userId AND himoku_cd = :himokuCd
			""")
	int deleteCell(@Param("ym") String ym, @Param("userId") Integer userId,
			@Param("himokuCd") String himokuCd);
}
