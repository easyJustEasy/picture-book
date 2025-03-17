package com.zhuzhu.picturebook.dao;

import cn.hutool.core.map.MapUtil;
import com.zhuzhu.picturebook.dto.GenerateResultDTO;
import com.zhuzhu.picturebook.dto.PageResult;
import com.zhuzhu.picturebook.dto.QueryByPageRequestDTO;
import com.zhuzhu.picturebook.dto.SystemMapDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@Slf4j
public class SystemMapDao {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void save(SystemMapDTO dto) {
        String sql = """
                INSERT INTO 'system_map'
                 ('key',
                 'value',
                 'remark'
                 ) VALUES (?, ?, ?);
                """;
        //获取插入数据的自增主键
        KeyHolder holder = new GeneratedKeyHolder();
        this.jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, dto.getKey());
            ps.setString(2, dto.getValue());
            ps.setString(3, dto.getRemark());
            return ps;
        }, holder);
        int id = Objects.requireNonNull(holder.getKey()).intValue();
        log.info("插入数据：{}", id);
        dto.setId(id);
    }

    public PageResult<SystemMapDTO> queryByPage(QueryByPageRequestDTO requestDTO) {
        PageResult<SystemMapDTO> pageResult = new PageResult<>();
        pageResult.setPage(requestDTO.getPage());
        pageResult.setPageSize(requestDTO.getPageSize());
        pageResult.setList(Collections.emptyList());
        int total = this.jdbcTemplate.queryForObject("select count(*) from system_map", Integer.class);
        pageResult.setTotal(total);
        pageResult.setPages(Double.valueOf(Math.ceil((double) total / requestDTO.getPageSize())).intValue());
        if (requestDTO.getPage() > pageResult.getPages()) {
            return pageResult;
        }
        //分页查询
        if (total <= requestDTO.getPageSize()) {
            List<Map<String, Object>> list = this.jdbcTemplate.queryForList("select id,key,value,remark from system_map");
            pageResult.setList(convertList(list));
        } else {
            List<Map<String, Object>> list = this.jdbcTemplate.queryForList(String.format("SELECT id,key,value,remark  FROM system_map LIMIT (%s * %s), %s", requestDTO.getPage() - 1, requestDTO.getPageSize(), requestDTO.getPageSize()));
            pageResult.setList(convertList(list));
        }
        return pageResult;
    }

    private List<SystemMapDTO> convertList(List<Map<String, Object>> list) {
        return list.stream().map(this::convertSingle).toList();
    }

    private SystemMapDTO convertSingle(Map<String, Object> stringObjectMap) {
        SystemMapDTO dto = new SystemMapDTO();
        dto.setKey(MapUtil.getStr(stringObjectMap, "key"));
        dto.setValue(MapUtil.getStr(stringObjectMap, "value"));
        dto.setRemark(MapUtil.getStr(stringObjectMap, "remark"));
        dto.setId(MapUtil.getInt(stringObjectMap, "id"));
        return dto;
    }

    public void update(GenerateResultDTO dto, String uuid) {
        int ret = this.jdbcTemplate.update("update system_map set key=?, value=?,remark=? where id=?;",
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

    public SystemMapDTO queryById(Integer id) {
        List<Map<String, Object>> list = this.jdbcTemplate.queryForList("select id,key,value,remark from system_map where id=?", id);
        if (list.isEmpty()) {
            return null;
        }
        return convertSingle(list.get(0));
    }
    public SystemMapDTO queryByKey(String key) {
        List<Map<String, Object>> list = this.jdbcTemplate.queryForList("select id,key,value,remark from system_map where key=?", key);
        if (list.isEmpty()) {
            return null;
        }
        return convertSingle(list.get(0));
    }
    public void initDb() {
        jdbcTemplate.execute("drop table system_map;");
        // 1、首先创建数据表
        String ddl = """
                    CREATE TABLE `system_map` (
                        id integer PRIMARY KEY autoincrement,
                        key TEXT,
                            value TEXT,
                            remark TEXT
                    );
                    CREATE UNIQUE INDEX system_map_key_IDX ON system_map ("key");
                """;

        jdbcTemplate.execute(ddl);

    }
}
