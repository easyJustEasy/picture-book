package com.picture.book.dto;

import lombok.Data;

@Data
public class QueryByPageRequestDTO {
    private int page = 1;
    private int pageSize = 10;

}
