package com.example.demo.repository;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Seisan;

@Repository
public interface SeisanRepository extends ListCrudRepository<Seisan, String> {
}
