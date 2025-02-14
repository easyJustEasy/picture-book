package com.picture.book.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class GenerateRequestDTO {
   private Integer id;
   private String role;
   private String storyDesc;
}
