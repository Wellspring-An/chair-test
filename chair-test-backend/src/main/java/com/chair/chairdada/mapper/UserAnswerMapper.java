package com.chair.chairdada.mapper;

import com.chair.chairdada.model.dto.statistic.AppAnswerCountDTO;
import com.chair.chairdada.model.dto.statistic.AppAnswerResultCountDTO;
import com.chair.chairdada.model.entity.UserAnswer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 96125
 * @description 针对表【user_answer(用户答题记录)】的数据库操作Mapper
 * @createDate 2025-04-25 16:47:49
 * @Entity com.chair.chairdada.model.entity.UserAnswer
 */
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {

    @Select("select appId, count(userId) as answerCount from user_answer group by appId order by answerCount desc limit 10")
    List<AppAnswerCountDTO> doAppAnswerCount();

    @Select("select resultName, count(resultName) as resultCount from user_answer" +
            " where appId = #{appId}" +
            " group by resultName order by resultCount desc")
    List<AppAnswerResultCountDTO> doAppAnswerResultCount(Long appId);
}




