package com.lisacbot.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "bot_config")
public class BotConfigEntity {

    @Id
    @Column(name = "config_key", nullable = false, length = 100)
    private String key;

    @Column(name = "config_value", nullable = false, length = 255)
    private String value;

    protected BotConfigEntity() {}

    public BotConfigEntity(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
