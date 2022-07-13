package com.yujutg.mapper;

import com.yujutg.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Fishhead
 * @since 2020-09-23
 */
@Mapper
@Repository
public interface UserMapper extends BaseMapper<User> {

}
