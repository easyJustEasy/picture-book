package org.example.picturebook.dto;

import lombok.Data;

import java.util.List;
@Data
public class PageResult {
    private List<GenerateResultDTO> list;
    private int total;
    private int page;
    private int pageSize;
    private int pages;
}
