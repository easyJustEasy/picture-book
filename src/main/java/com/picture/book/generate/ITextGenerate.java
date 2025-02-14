package com.picture.book.generate;

import com.picture.book.dto.Story;

public interface ITextGenerate {
    Story generate(String system, String prompt) throws Exception;
}
