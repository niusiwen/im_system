package com.nsw.im.service.message.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nsw.im.service.message.dao.ImMessageBodyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

//@Repository
@Mapper
public interface ImMessageBodyMapper extends BaseMapper<ImMessageBodyEntity> {
}
