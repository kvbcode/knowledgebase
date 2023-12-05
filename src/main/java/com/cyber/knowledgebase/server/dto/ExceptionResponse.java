package com.cyber.knowledgebase.server.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
public class ExceptionResponse {
    final ZonedDateTime dataTime;
    final UUID uid;
    final String message;

    public ExceptionResponse(String message) {
        dataTime = ZonedDateTime.now();
        uid = UUID.randomUUID();
        this.message = message;
    }
}
