package org.example.picturebook.dto;

import lombok.Data;

@Data
public class GenerateRequestDTO {
   private Integer id;
   private String role;
   private String storyDesc;
}
