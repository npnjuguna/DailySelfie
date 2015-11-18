package com.njuguna.dailyselfie.server.repository;

import com.njuguna.dailyselfie.server.entity.User;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {

}