package com.njuguna.dailyselfie.server.repository;

import com.njuguna.dailyselfie.server.entity.Session;

import org.springframework.data.repository.CrudRepository;

public interface SessionRepository extends CrudRepository<Session, String> {

}