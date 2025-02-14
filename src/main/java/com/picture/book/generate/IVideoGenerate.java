package com.picture.book.generate;

import com.picture.book.dto.PictureDTO;

import java.util.List;

public interface IVideoGenerate {
   String generate(String imagesPath,String audioPath) throws Exception;
   String concat(List<PictureDTO> list) throws Exception;
}
