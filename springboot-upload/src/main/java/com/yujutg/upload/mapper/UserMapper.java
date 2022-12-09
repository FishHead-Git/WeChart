package com.yujutg.upload.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yujutg.upload.entity.Users;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface UserMapper extends BaseMapper<Users> {

    @Delete("truncate table users")
    public void truncateUser();

    int insertBatchByUser(List<Users> list);
}
