package com.yujutg.upload.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.yujutg.upload.utils.Base64TypeHandle;
import com.yujutg.upload.utils.PasswordEncoderUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author Fishhead
 * @since 2022-04-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(autoResultMap = true)
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type=IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     */
    private String username;
    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    @TableField(typeHandler = Base64TypeHandle.class)
    private String password;

    /**
     * 金额
     */
    private BigDecimal money;

    /**
     * 收藏菜品
     */
    private String collect;

    /**
     * 头像
     */
    private String img;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDeleted;


}
