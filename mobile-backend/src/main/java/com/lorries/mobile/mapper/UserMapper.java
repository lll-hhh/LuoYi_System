package com.lorries.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.mobile.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM \"user\" WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Select("SELECT * FROM \"user\" WHERE phone = #{phone}")
    User findByPhone(@Param("phone") String phone);
}
