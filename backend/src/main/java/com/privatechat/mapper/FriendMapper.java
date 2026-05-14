package com.privatechat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.privatechat.entity.Friend;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FriendMapper extends BaseMapper<Friend> {
}
