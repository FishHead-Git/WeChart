package com.yujutg.upload.entity;

import lombok.Data;

@Data
public class PageDTO {

    private Integer pageNo;
    private Integer pageSize;
    private String pageCondition;
    private String orderBy;
    private Boolean Asc;

}
