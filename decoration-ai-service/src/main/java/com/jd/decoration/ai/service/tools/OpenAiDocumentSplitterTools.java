package com.jd.decoration.ai.service.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.jd.decoration.ai.service.dto.OriginGptResponse;
import com.jd.decoration.ai.service.dto.ZhipuRequest;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class OpenAiDocumentSplitterTools {

    private static final Gson gson = new Gson();
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final String zhipu4Url = "";
    private static final String apiKey = "";
    private static final HttpPost httpPost = new HttpPost(zhipu4Url);

    @Tool("parse the content of the image to get the description of the image")
    public String getDescriptionOfImageByImageUrl(String imageUrl) {
        if (StrUtil.isBlank(imageUrl)) {
            return "error: imageUrl is blank.";
        }
        ZhipuRequest.Content contentText = new ZhipuRequest.Content();
        contentText.setType("text");
        contentText.setText("请识别并提取出图片中的文字。如果能够提取到图片上的文字，请直接返回图片的文字内容，不要添加任何额外信息。如果提取不到图片上的文字，请直接返回：图片无法识别。");

        ZhipuRequest.Content contentImage = new ZhipuRequest.Content();
        contentImage.setType("image_url");
        contentImage.setImageUrl(new ZhipuRequest.ImageUrl(imageUrl));

        List<ZhipuRequest.Content> contentList = new ArrayList<>();
        contentList.add(contentText);
        contentList.add(contentImage);

        ZhipuRequest.Messages message = new ZhipuRequest.Messages();
        message.setRole("user");
        message.setContent(contentList);

        List<ZhipuRequest.Messages> messageList = new ArrayList<>();
        messageList.add(message);

        ZhipuRequest zhipuRequest = new ZhipuRequest();
        zhipuRequest.setErp("");
        zhipuRequest.setModel("glm-4v");
        zhipuRequest.setMessages(messageList);
        String jsonStr = gson.toJson(zhipuRequest);

        httpPost.setHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Authorization", apiKey);

        // 设置参数---设置消息实体 也就是携带的数据
        StringEntity entity = new StringEntity(jsonStr, StandardCharsets.UTF_8);
        entity.setContentEncoding(StandardCharsets.UTF_8.name());
        entity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        httpPost.setEntity(entity);

        StringBuilder result = new StringBuilder();
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost);) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(httpResponse.getEntity().getContent())){
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!StrUtil.startWith(line, "data: ")) {
                            continue;
                        }

                        line = line.substring(6);
                        if (StrUtil.isBlank(line) || !JSONUtil.isTypeJSONObject(line) || StrUtil.equalsIgnoreCase(line, "[DONE]")) {
                            continue;
                        }

                        OriginGptResponse originGptResponse = gson.fromJson(line, OriginGptResponse.class);
                        for (OriginGptResponse.Choices choice : originGptResponse.getChoices()) {
                            result.append(choice.getDelta().getContent());
                        }
                    }
                    log.info("descriptionImageByImageUrl imgUrl:{}, description:{}", imageUrl, result);
                }
            }
        } catch (IOException e) {
            log.error("postStream->error:{}", e.getMessage(), e);
        }

        String resultString = result.toString();
        if (StrUtil.isEmpty(resultString) || StrUtil.contains(resultString, "图片无法识别")) {
            return "";
        }
        return resultString;
    }

}
