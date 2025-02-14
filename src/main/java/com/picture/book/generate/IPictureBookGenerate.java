package com.picture.book.generate;


import com.picture.book.dto.PictureResultDTO;
import com.picture.book.dto.Story;

public interface IPictureBookGenerate {
     PictureResultDTO generate(String actors, Story story) throws Exception ;
}
