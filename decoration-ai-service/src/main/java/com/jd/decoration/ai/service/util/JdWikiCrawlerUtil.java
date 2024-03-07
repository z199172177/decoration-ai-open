package com.jd.decoration.ai.service.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class JdWikiCrawlerUtil {
    private static final String WIKI_LIST_URL = "https://home-zxzs-gw.jd.com/api/chapter/queryChapterInfoList?clientVersion=%7B%22appId%22%3A%22wxb44ea6abdb545439%22%2C%22envVersion%22%3A%22release%22%2C%22version%22%3A%22%22%7D";
    private static final String WIKI_DETAIL_URL = "https://home-zxzs-gw.jd.com/api/question/queryQuestionList?pageNo=1&pageSize=100&renovationId=";

    /**
     * 下载百科数据到本地目录
     */
    public static void downWebData(String fileDirPath) {
        Integer totalContentLength = 0;
        log.info("JdWikiCrawlerUtil#downWebData2Local url:{}", WIKI_LIST_URL);
        HttpRequest request = new HttpRequest(WIKI_LIST_URL);
        request.setMethod(Method.GET);

        HttpResponse response = request.execute();
        String body = response.body();

        JSONObject jsonObj = JSONUtil.parseObj(body);
        JSONArray listAry = jsonObj.getJSONArray("data");

        Iterator<Object> listIterator = listAry.stream().iterator();
        while (listIterator.hasNext()) {
            Object next = listIterator.next();
            JSONObject nextObj = JSONUtil.parseObj(next);
            String level1ChapterName = nextObj.getStr("chapterName");
            log.info("JdWikiCrawlerUtil#downWebData2Local level1ChapterName:{}", level1ChapterName);

            JSONArray childrenAry = nextObj.getJSONArray("children");
            Iterator<Object> childrenIterator = childrenAry.stream().iterator();
            while (childrenIterator.hasNext()) {
                Object nextChildren = childrenIterator.next();
                JSONObject nextChildrenObj = JSONUtil.parseObj(nextChildren);
                String level2ChapterName = nextChildrenObj.getStr("chapterName");
                log.info("JdWikiCrawlerUtil#downWebData2Local level1-level2: {} - {}", level1ChapterName, level2ChapterName);

                JSONArray renovationAry = nextChildrenObj.getJSONArray("renovationAndChapterVO");
                Iterator<Object> renovationIterator = renovationAry.stream().iterator();
                while (renovationIterator.hasNext()) {
                    Object nextRenovation = renovationIterator.next();
                    JSONObject nextRenovationObj = JSONUtil.parseObj(nextRenovation);

                    String id = nextRenovationObj.getStr("id");
                    log.info("JdWikiCrawlerUtil#downWebData2Local id:{}", id);

                    String level3RenovationName = nextRenovationObj.getStr("renovationName");
                    String fileName = level1ChapterName + "-" + level2ChapterName + "-" + level3RenovationName + ".html";
                    log.info("JdWikiCrawlerUtil#downWebData2Local fileName:{}", fileName);

                    List<String> contentList = getWikiContent(fileName, id);
                    log.info("JdWikiCrawlerUtil#downWebData2Local contentList:{}", contentList.size());

                    File file = new File(fileDirPath + "/" + fileName);
                    FileUtil.writeLines(contentList, file, Charset.defaultCharset());

                    log.info("JdWikiCrawlerUtil#downWebData2Local save:{} done.", fileName);
                }
            }
        }
        log.info("JdWikiCrawlerUtil#downWebData2Local totalContentLength:{}", totalContentLength);
    }

    /**
     * 获取文档详情
     */
    public static List<String> getWikiContent(String title, String id) {
        HttpRequest request = new HttpRequest(WIKI_DETAIL_URL + id);
        request.setMethod(Method.GET);

        HttpResponse response = request.execute();
        String body = response.body();
        JSONObject jsonObj = JSONUtil.parseObj(body);
        JSONObject dataObj = jsonObj.getJSONObject("data");

        JSONArray contentAry = dataObj.getJSONArray("content");
        Iterator<Object> contentIterator = contentAry.stream().iterator();

        List<String> contentList = new ArrayList<>();
        contentList.add(title);
        while (contentIterator.hasNext()) {
            Object next = contentIterator.next();
            JSONObject nextObj = JSONUtil.parseObj(next);
            String context = nextObj.getStr("context");
            if (StrUtil.isBlank(context)) {
                continue;
            }
            contentList.add(context);
        }

        if (contentList.size() <= 1) {
            return new ArrayList<>();
        }

        return contentList;
    }

    /**
     * 将文档转换成txt文件，会过滤html标签。
     */
    public static void doc2Txt(String docDirPath, String txtDirPath) {
        File[] files = getFileAry(docDirPath);
        for (File file : files) {
            try {
                String fileName = FileUtil.mainName(file);
                String content = FileUtil.readUtf8String(file);
                content = HtmlUtil.cleanHtmlTag(content);
                if (StrUtil.equals(content, fileName)) {
                    continue;
                }

                FileUtil.writeUtf8String(content, txtDirPath + "/" + fileName + ".txt");
            } catch (Exception e) {
                log.error("error.msg:{}", e.getMessage(), e);

            }
        }
    }

    public static File[] getFileAry(String path) {
        File file = getFile(path);
        if (file == null) {
            log.error("找不到文件");
            return new File[0];
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (ArrayUtil.isEmpty(files)) {
                log.error("文件目录是空的");
                return new File[0];
            }
            return files;
        }

        return new File[]{file};
    }

    /**
     * 获取要加载的文件
     */
    private static File getFile(String filePath) {
        try {
            return ResourceUtils.getFile(filePath);
        } catch (Exception e) {
            log.error("WikiServiceImpl#getFile errMsg:{}", e.getMessage(), e);
            return null;
        }
    }
}
