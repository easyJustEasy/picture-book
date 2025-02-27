package org.example.picturebook.generate.text;

import org.example.picturebook.dto.Story;

public interface ITextGenerate {
   String generate(String system, String prompt) throws Exception ;
}
