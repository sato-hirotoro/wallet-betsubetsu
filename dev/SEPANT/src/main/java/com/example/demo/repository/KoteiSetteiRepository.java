package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.KoteiSettei;

@Repository
public interface KoteiSetteiRepository extends ListCrudRepository<KoteiSettei, Integer> {

	/** 対象月に有効な固定設定（処理設計書 6.3） */
	@Query("""
			SELECT * FROM t_kotei_settei
			 WHERE kaishi_mm <= :ym AND shuryo_mm >= :ym
			""")
	List<KoteiSettei> findValid(@Param("ym") String ym);
}
