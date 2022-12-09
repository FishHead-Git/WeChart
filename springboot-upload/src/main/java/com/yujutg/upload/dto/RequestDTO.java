package com.yujutg.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestDTO implements Serializable {
    private String fromUser;
    private String toUserId;
    private String contentText;
}
