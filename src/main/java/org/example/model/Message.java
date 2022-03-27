package org.example.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
public class Message {
    private Integer id;
    private Integer userId;
    private String userNickname;
    private String content;
    private Integer channelId;
    private java.util.Date sendTime;


    

    public void getSendTime(Date date) {
    }

    public void getUserId(Integer userId) {
    }
}
