package com.orientapp.dto;

import lombok.Data;

@Data
public class ResultFormDto {
    private Long registrationId;
    private Long eventId;
    private String finishTime; // format HH:mm:ss z input type="time" step="1"
    private boolean dnf;
}
