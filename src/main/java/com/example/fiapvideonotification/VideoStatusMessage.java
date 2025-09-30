package com.example.fiapvideonotification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
public class VideoStatusMessage {
    private UUID videoId;
    private String videoName;
    private String customerEmail;
    private String videoStatus;
}
