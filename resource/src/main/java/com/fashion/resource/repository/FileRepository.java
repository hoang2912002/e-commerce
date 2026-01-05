package com.fashion.resource.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fashion.resource.entity.File;
@Repository
public interface FileRepository extends MongoRepository<File,String>{
    Optional<File> findByName(String name);
}
