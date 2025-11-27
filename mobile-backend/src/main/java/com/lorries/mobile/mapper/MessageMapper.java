package com.lorries.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.mobile.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 消息Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 获取未读消息数量
     */
    @Select("SELECT COUNT(*) FROM message WHERE user_id = #{userId} AND status = 'unread'")
    Long countUnread(@Param("userId") Integer userId);

    /**
     * 标记所有消息已读
     */
    @Update("UPDATE message SET status = 'read', read_at = NOW() WHERE user_id = #{userId} AND status = 'unread'")
    void markAllAsRead(@Param("userId") Integer userId);
}
