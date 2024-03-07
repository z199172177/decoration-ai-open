package com.jd.decoration.ai.service.rpc;

import com.jd.decoration.ai.api.WikiOutService;
import com.jd.decoration.ai.service.service.WikiService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("wikiOutService")
public class WikiOutServiceImpl implements WikiOutService {
    @Resource
    private WikiService wikiService;

    @Override
    public String chart(String question) {
        wikiService.chat(question, null);
        return "";
    }

    @Override
    public String generatorPost(String about, String title, String keywords) {
        wikiService.generatorPost(about, title, keywords, null);
        return "";
    }

    @Override
    public String recommend(String keywords) {
        wikiService.recommend(keywords, null);
        return null;
    }
}
