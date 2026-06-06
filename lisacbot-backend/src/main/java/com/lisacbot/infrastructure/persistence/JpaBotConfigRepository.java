package com.lisacbot.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBotConfigRepository extends JpaRepository<BotConfigEntity, String> {}
