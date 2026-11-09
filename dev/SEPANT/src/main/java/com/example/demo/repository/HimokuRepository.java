package com.example.demo.repository;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Himoku;

@Repository
public interface HimokuRepository extends ListCrudRepository<Himoku, String> {

	List<Himoku> findAllByOrderByHyojiJun();
}
