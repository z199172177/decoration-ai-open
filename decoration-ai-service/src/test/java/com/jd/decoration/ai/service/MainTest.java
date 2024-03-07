package com.jd.decoration.ai.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.jd.decoration.ai.service.agent.WikiAgent;
import com.jd.decoration.ai.service.dto.CustomStreamingResponseHandler;
import com.jd.decoration.ai.service.dto.OriginGptResponse;
import com.jd.decoration.ai.service.dto.ZhipuRequest;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiModelName;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.util.StringUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class MainTest {

    private static String sseUrl = "";
    private static String zhipu4Url = "";
    private static String url = "";
    private static String apiKey = "";

    private static Gson gson = new Gson();

    @Test
    public void embeddingTest() {
        OpenAiEmbeddingModel openAiEmbeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey("")
                .baseUrl("")
                .modelName(OpenAiModelName.TEXT_EMBEDDING_ADA_002)
                .build();
        Response<Embedding> text = openAiEmbeddingModel.embed("京东严选十佳商家推荐：九牧九牧官方旗舰店九牧（JOMOO） 花洒淋浴套装大置物喷枪自动除垢增压顶喷淋浴器 【王牌新款】琴雨钢琴按键36602¥1839京东严选十佳商家推荐：京赞京赞卫浴旗舰店京赞 恒温花洒暗装智能数显淋浴花洒套装入仓嵌入式浴室沐浴淋雨全铜家用白色枪灰色增压喷头花洒套装 全铜数显花洒套装【枪灰色】¥1699京赞 恒温花洒暗装智能数显淋浴花洒套装入仓嵌入式浴室沐浴淋雨全铜家用白色枪灰色增压喷头花洒套装 全铜数显花洒套装【雅白色】¥1699京赞 恒温花洒暗装智能数显淋浴花洒套装入仓嵌入式浴室沐浴淋雨全铜家用白色枪灰色增压喷头花洒套装 全铜数显花洒套装【耀黑色】¥1699京东严选十佳商家推荐：浪鲸浪鲸卫浴旗舰店浪鲸（SSWW） 花洒淋浴花洒套装置物花洒沐浴龙头易洁喷枪喷头增压花洒套装 SKQM016A-WH4-1¥919浪鲸（SSWW） 钢琴按键出水花洒套装淋浴花洒组合精铜花洒自洁顶喷隐藏下出水 钢琴四功SKQM010A-CP4-1¥1119浪鲸（SSWW） 卫浴淋浴花洒套装钢琴按键淋浴花洒家用卫生间雅白置物花洒 【数显恒温四功能¥1549京东严选十佳商家推荐：箭牌箭牌厨卫旗舰店箭牌（ARROW） 淋浴花洒套装 恒温花洒全套 增压喷头精铜龙头 钢琴按键式J10 【钢琴白】按键切换出水-冷热款¥1699京东严选十佳商家推荐：科勒科勒厨房卫浴旗舰店科勒（KOHLER） 恒温花洒可升降淋浴器花洒套装齐乐三出水恒温淋浴柱 【店铺优选旗舰款】21088T-B9-CP¥2260京东严选十佳商家推荐：汉斯格汉斯格雅官方旗舰店汉斯格雅（Hansgrohe） 德国原装双飞雨300mm顶喷恒温淋浴管花洒套装 大顶喷恒温龙头杆长1m¥7999汉斯格雅（Hansgrohe） 双飞雨恒温花洒240超大顶喷带下出水龙头淋浴管套装多功能淋浴器 旗舰升级款：26777手持升级境雨¥4952京东严选十佳商家推荐：欧琳欧琳官方旗舰店欧琳（OULIN） 淋浴花洒套装花洒三件套花洒全套增压花洒全铜主体增压顶喷花洒 CH305S三出水套装¥559");
        System.out.println("text.content() = " + text.content());
    }

    @Test
    public void chatTest() {
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey("")
                .baseUrl("")
                .modelName(OpenAiModelName.GPT_3_5_TURBO)
                .build();

        String generate = chatModel.generate("https://img11.360buyimg.com/nb/jfs/t1/10356/23/20877/78889/63a3d05aE14ddd495/ed8a57190d60f85d.jpg  请你描述一下这张图片的内容");
        System.out.println("generate = " + generate);
    }

    @Test
    public void glm4vForPdf() {
        String imgPath = "https://storage.360buyimg.com/3dmodel.jd.com/p17_img1.jpeg";
        String imgBeforePageContent = FileUtil.readUtf8String("/Users/ext.zhoukun9/Downloads/books/renovateBooks/Renovation, do three things well enough/imgAndTxt/p16.txt");
        String imgPageContent = FileUtil.readUtf8String("/Users/ext.zhoukun9/Downloads/books/renovateBooks/Renovation, do three things well enough/imgAndTxt/p17.txt");
        String imgAfterPageContent = FileUtil.readUtf8String("/Users/ext.zhoukun9/Downloads/books/renovateBooks/Renovation, do three things well enough/imgAndTxt/p18.txt");

        ZhipuRequest.Content contentText = new ZhipuRequest.Content();
        contentText.setType("text");
        contentText.setText("你是一个图片识别、打标机器人。" +
                "我从一本装修书籍中截取了一张图片，我会提供给你书籍中与这张图片相关内容" +
                "请你解析图片并根据我提供的内容，描述一下图片。\n" +
                "书籍中与这张图片相关的内容: " + imgBeforePageContent + "\n" + imgPageContent + "\n" + imgAfterPageContent + "\n");

        ZhipuRequest.Content contentImage = new ZhipuRequest.Content();
        contentImage.setType("image_url");
        contentImage.setImageUrl(new ZhipuRequest.ImageUrl(imgPath));

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

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(zhipu4Url);
        httpPost.setHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Authorization", apiKey);

        StringEntity entity = new StringEntity(jsonStr, StandardCharsets.UTF_8);
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost);) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(httpResponse.getEntity().getContent())){
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    String line;
                    StringBuilder result = new StringBuilder();
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
                    System.out.println("result.toString() = " + result.toString());
                }
            }

        } catch (IOException e) {
            log.error("postStream->error", e);
            e.printStackTrace();
        }
    }

    @Test
    public void glm4v() {
        String jsonStr = "{\"erp\":\"ext.zhoukun9\",\"messages\":[{\"role\":\"user\",\"content\":[{\"type\":\"text\",\"text\":\"请将图片中的文本内容识别并输出。\"},{\"type\":\"image_url\",\"image_url\":{\"url\":\"https://img11.360buyimg.com/nb/jfs/t1/177575/27/31006/47503/63a3d18fE1a82f1ca/17021e324585234f.jpg\"}}]}],\"model\":\"glm-4v\"}";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(zhipu4Url);
        httpPost.setHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Authorization", apiKey);

        StringEntity entity = new StringEntity(jsonStr, StandardCharsets.UTF_8);
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost);) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(httpResponse.getEntity().getContent())){
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    String line;
                    StringBuilder result = new StringBuilder();
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
                    System.out.println("result.toString() = " + result.toString());
                }
            }

        } catch (IOException e) {
            log.error("postStream->error", e);
            e.printStackTrace();
        }
    }

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final HttpPost httpPost = new HttpPost(zhipu4Url);

    @Test
    public void glm4vByObj() {
        String imgUrl = "https://img11.360buyimg.com/nb/jfs/t1/68074/27/23444/70043/63a3d18fEb5508798/c17e9195191650af.jpg";
        String ret = getDescriptionOfImageByImageUrl(imgUrl);
        System.out.println("ret = " + ret);
    }

    public String getDescriptionOfImageByImageUrl(String imageUrl) {
        if (StrUtil.isBlank(imageUrl)) {
            return "error: imageUrl is blank.";
        }
        ZhipuRequest.Content contentText = new ZhipuRequest.Content();
        contentText.setType("text");
        contentText.setText("请识别并提取出图片中的文字。");

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

        return result.toString();
    }

    @Test
    public void gptStream() {
        String jsonStr = "{\"messages\":[{\"content\":\"hello,write a java example,like hello world\",\"role\":\"user\"}],\"model\":\"gpt-3.5-turbo\"}";

        // 创建httpClient
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // 创建post请求方式实例
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Authorization", apiKey);
        httpPost.setHeader("accept", "text/event-stream");

        StringEntity entity = new StringEntity(jsonStr, Charset.forName("UTF-8"));
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);

        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (StringUtil.isEmpty(line)) {
                    continue;
                }
                if (line.startsWith("data: ")) {
                    line = line.substring(6);
                }
                System.out.println(new Date() + "---" + line);
            }
            System.out.println(new Date() + "-finish--");
        } catch (IOException e) {
            log.error("postStream->error", e);
            e.printStackTrace();
        }
    }

    @Test
    public void docSplit() {
        String doc = ":segment-start: https://img11.360buyimg.com/nb/jfs/t1/177575/27/31006/47503/63a3d18fE1a82f1ca/17021e324585234f.jpg--好的，图片中的文本内容是：如何选购手冲咖啡套装？\n" +
                "\n" +
                "手冲咖啡套装选购指南\n" +
                "\n" +
                "人数  尺寸   使用场景\n" +
                "\n" +
                "01号1-2人   300ML  家用\n" +
                "\n" +
                "02号2-4人   600ML  家用聚会\n" +
                "\n" +
                "清水滤波器5人以上   750ML以上  家用聚会:segment-end:\n" +
                ":segment-start: https://img11.360buyimg.com/nb/jfs/t1/170210/8/33496/49129/63a3d18fE52a96fef/f5aa2879541f2e0e.jpg--好的，图片中的文本内容是：\n" +
                "\n" +
                "为何要选择V60手冲套装\n" +
                "\n" +
                "三大特点\n" +
                "\n" +
                "V60型60°角设计\n" +
                "\n" +
                "K单孔设计\n" +
                "\n" +
                "纺锤纺设计\n" +
                "\n" +
                "经验告诉我们60°角冲出的咖啡口感更能突出咖啡本身的质感\n" +
                "\n" +
                "流速快，咖啡流量更集中，简单控制无脂奶\n" +
                "\n" +
                "使滤杯的排气效果更流畅；可作为水流加速的辅助，增加水流的路劲，来增加咖啡颗粒和水接触的时间；上缘还没又短短纺锤状骨，为避免水位生告而无法承受的重量，造成的阻塞。:segment-end:\n" +
                ":segment-start: https://img11.360buyimg.com/nb/jfs/t1/68074/27/23444/70043/63a3d18fEb5508798/c17e9195191650af.jpg--好的，图片中的文本内容是：\n" +
                "\n" +
                "01号套装使用场景\n" +
                "02号套装使用场景\n" +
                "03清水滤波滤泡:segment-end:\n";

        String segmentStartFlag = ":segment-start:";
        String segmentEndFlag = ":segment-end:";
        int segmentStartIndex = StrUtil.indexOf(doc, segmentStartFlag, 0, true);
        System.out.println("segmentStartIndex = " + segmentStartIndex);

        int segmentEndIndex = StrUtil.lastIndexOf(doc, segmentEndFlag, doc.length(),true);
        System.out.println("segmentEndIndex = " + segmentEndIndex);

        doc = StrUtil.sub(doc, segmentStartIndex, segmentEndIndex);
        System.out.println(doc);

        System.out.println("=========================");
        System.out.println("=========================");
        System.out.println("=========================");

        List<String> docSplit = StrUtil.split(doc, segmentStartFlag);
        docSplit.forEach(item -> {
            int itemSegmentEndIndex = StrUtil.lastIndexOf(item, segmentEndFlag, item.length(), true);
            item = StrUtil.sub(item, 0, itemSegmentEndIndex);
            System.out.println("item = " + item);
            System.out.println("=========================");
        });

    }

    public static void main(String[] args) {
        StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                .baseUrl("")
                .apiKey("")
                .modelName("gpt-3.5-turbo")
                .build();

        WikiAgent wikiAgent = AiServices.builder(WikiAgent.class)
                .streamingChatLanguageModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        CustomStreamingResponseHandler responseHandler = new CustomStreamingResponseHandler(null);
        wikiAgent.chat("现代风格：现代风格通常注重简洁、清爽的设计，以简单的线条、中性色调和功能性家具为特点。传统风格：传统卧室装修强调经典和优雅，常使用暖色调、花纹和复古风格的家具，营造温馨、舒适的氛围。乡村风格：乡村风格的卧室装修常使用自然材料，如木头和石材，以及淡雅的色彩，呈现出宁静、质朴的感觉。现代农舍风格：结合了现代和乡村风格的元素，强调舒适、实用和自然的设计。艺术装饰风格：艺术装饰风格注重精致的装饰品和独特的设计，可能包括华丽的家具、丰富的颜色和独特的艺术品。北欧风格：北欧风格追求简约、功能性和明亮感，使用淡色调、自然材料和简单的家具。亚洲风格：亚洲风格常包括东方元素，如和式床、屏风、竹制品等，以及深色调和简洁的设计。现代工业风格：借鉴工业建筑的元素，使用裸露的砖墙、金属家具和简洁的设计，展现出现代感和个性。海滨或度假风格：使用海洋色彩、沙滩元素和轻松的家具，营造出度假的氛围。豪华风格：豪华风格注重高品质的材料和精致的设计，可能包括大型床、华丽的吊灯和奢侈的家具。", "如何装修卧室")
                .onNext(responseHandler::onNext)
                .onComplete(responseHandler::onComplete)
                .onError(responseHandler::onError)
                .start();

    }
}
