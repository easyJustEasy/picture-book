package com.picture.book.dao;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import cn.hutool.core.map.MapUtil;
import com.picture.book.dto.GenerateResultDTO;
import com.picture.book.dto.PageResult;
import com.picture.book.dto.QueryByPageRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class BookDao {
    @Autowired
    JdbcTemplate jdbcTemplate;
    public void save(GenerateResultDTO dto, String uuid) {
        String sql = """
                INSERT INTO 'book'
                 ('role_name', 
                 'story_desc',
                 'video_url',
                 'error',
                 'batch_id',
                 'create_time',
                 'story_system_message',
                 'story_user_message',
                 'story_output_message',
                 'status'
                 ) VALUES (?, ?, ?,?, ?, ?,?,?,?,?);
                """;
        //获取插入数据的自增主键
        KeyHolder holder = new GeneratedKeyHolder();
        this.jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, dto.getRole());
            ps.setString(2, dto.getStoryDesc());
            ps.setString(3, dto.getVideoUrl());
            ps.setString(4, dto.getError());
            ps.setString(5, uuid);
            if(dto.getCreateTime()!=null){
                ps.setDate(6, new Date( dto.getCreateTime().getTime()));
            }else{
                ps.setDate(6, new Date(System.currentTimeMillis()));
            }
            ps.setString(7, dto.getStorySystemMessage());
            ps.setString(8,  dto.getStoryUserMessage());
            ps.setString(9,  dto.getStoryOutputMessage());
            ps.setString(10,  dto.getStatus());
            return ps;
        }, holder);
        int id = Objects.requireNonNull(holder.getKey()).intValue();
        log.info("插入数据：{}", id);
        dto.setId(id);
    }

    public PageResult queryByPage(QueryByPageRequestDTO requestDTO) {
        PageResult pageResult = new PageResult();
        pageResult.setPage(requestDTO.getPage());
        pageResult.setPageSize(requestDTO.getPageSize());
        pageResult.setList(Collections.emptyList());
        int total = this.jdbcTemplate.queryForObject("select count(*) from book", Integer.class);
        pageResult.setTotal(total);
        pageResult.setPages(Double.valueOf(Math.ceil((double) total /requestDTO.getPageSize())).intValue());
        if(requestDTO.getPage()>pageResult.getPages()){
            return pageResult;
        }
        //分页查询
        if (total<=requestDTO.getPageSize()) {
           List<Map<String, Object>> list =  this.jdbcTemplate.queryForList("select id, role_name, story_desc,video_url,error,batch_id,create_time,story_system_message,story_user_message,story_output_message,status from book");
           pageResult.setList(convertList(list));
        }else{
            List<Map<String, Object>> list =this.jdbcTemplate.queryForList(String.format("SELECT id, role_name, story_desc,video_url,error,batch_id,create_time,story_system_message,story_user_message,story_output_message,status FROM book LIMIT (%s * %s), %s",requestDTO.getPage(),requestDTO.getPageSize(),requestDTO.getPageSize()));
            pageResult.setList(convertList(list));
        }
        return pageResult;
    }

    private List<GenerateResultDTO> convertList(List<Map<String, Object>> list) {
       return list.stream().map(this::convertSingle).toList();
    }

    private GenerateResultDTO convertSingle(Map<String, Object> stringObjectMap) {
        GenerateResultDTO dto = new GenerateResultDTO();
        dto.setRole(MapUtil.getStr(stringObjectMap,"role_name"));
        dto.setStoryDesc(MapUtil.getStr(stringObjectMap,"story_desc"));
        dto.setCreateTime(MapUtil.getDate(stringObjectMap,"create_time"));
        dto.setVideoUrl(MapUtil.getStr(stringObjectMap,"video_url"));
        dto.setError(MapUtil.getStr(stringObjectMap,"error"));
        dto.setBatchId(MapUtil.getStr(stringObjectMap,"batch_id"));
        dto.setId(MapUtil.getInt(stringObjectMap,"id"));
        dto.setStorySystemMessage(MapUtil.getStr(stringObjectMap,"story_system_message"));
        dto.setStoryUserMessage(MapUtil.getStr(stringObjectMap,"story_user_message"));
        dto.setStoryOutputMessage(MapUtil.getStr(stringObjectMap,"story_output_message"));
        dto.setStatus(MapUtil.getStr(stringObjectMap,"status"));
        return dto;
    }

    public void update(GenerateResultDTO dto, String uuid) {
        int ret = this.jdbcTemplate.update("update book set role_name=?, story_desc=?,video_url=?,error=?,batch_id=?,create_time=?,story_system_message=?,story_user_message=?,story_output_message=?,status=? where id=?;",
                dto.getRole(),
                dto.getStoryDesc(),
                dto.getVideoUrl(),
                dto.getError(),
                uuid,
                dto.getCreateTime(),
                dto.getStorySystemMessage(),
                dto.getStoryUserMessage(),
                dto.getStoryOutputMessage(),
                dto.getStatus(),
                dto.getId()
        );
        log.info("更新数据：{}", ret);
    }
    public GenerateResultDTO queryById(Integer id) {
        List<Map<String, Object>> list = this.jdbcTemplate.queryForList("select id, role_name, story_desc,video_url,error,batch_id,create_time,story_system_message,story_user_message,story_output_message,status from book where id=?", id);
        if(list.isEmpty()){
            return null;
        }
        return convertSingle(list.get(0));
    }
}
