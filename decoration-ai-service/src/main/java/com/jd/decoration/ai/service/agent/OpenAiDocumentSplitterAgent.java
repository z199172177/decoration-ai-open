package com.jd.decoration.ai.service.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface OpenAiDocumentSplitterAgent {

    @SystemMessage({
            //你是一位向量数据库工程师。你的任务是将各种类型的数据转换成向量数据。这么做的目的是为了实现数据的相似度检索功能。
            "You are a vector database engineer. Your task is to convert various types of data into vector data. The purpose of doing so is to implement a similarity retrieval function for the data"
    })
    @UserMessage({
            //作为一个向量数据库工程师，请使用你的专业知识，将我给出的HTML类型原始数据拆分成段落。 我会提出一些数据拆分的要求，请按照我的要求将数据拆分成段落。
            "As a Vector Database Engineer, please use your expertise to split the raw data of {{fileType}} type given by me into paragraphs. I will make some data splitting requirements, please follow my requirements to split the data into paragraphs \n"
                    //原始数据
                    + "original data: \n {{information}} \n"
                    //数据拆分要求
                    + "Data splitting requirements: \n"
                    // 原始数据含有图片url时，请解析图片内容获取关于图片的描述。请将获取到的图片描述加以完善，组织成一句通顺完整的话，然后将图片url和图片描述合并到一起，中间使用字符 -- 连接，格式是：'图片url'--'图片描述' 。每一张图片和这张图片的描述应该存储到一个单独的段落中。
                    //+ "1. When the raw data contains an image url, please parse the content of the image to get a description about the image. Please refine the obtained image description, organize it into a smooth and complete sentence, and then merge the image url and the image description together, using the character -- in between, in the format of: 'image url'--'image description'. Each image and the description of this image should be stored in a separate paragraph. \n"
                    // 原始数据含有图片url时，请解析图片内容获取关于图片的描述。请将你获取到的图片描述信息加以完善，然后将图片描述信息拆分成段落。如果没有获取到图片描述，或描述信息无效，请返一些空格字符或空字符串。
                    + "1. If the raw data contains an image url, please parse the image content to get a description of the image. Please refine the image description information you get, and then split the image description information into paragraphs. If you don't get the description, or the description is invalid, please return some space characters or empty strings. \n"
                    // 如果是HTML类型的数据，请在拆分后的段落里过滤HTML标签，只保留文本或图片地址。
                    + "2. If the data is of HTML type, please filter the HTML tags in the split paragraph and keep only the text or image address. \n"
                    // 我最终会将数据存储到向量数据库，并使用这些数据做相似度检索。所以在拆分数据时请尽可能保证相似的数据被拆分到同一段落。
                    + "3. I will eventually store the data into a vector database and use that data to do similarity searches. So when splitting the data please make sure that similar data is split into the same paragraph if possible. \n"
                    // 保证每个段落的语义完整性。
                    + "4. Ensure semantic integrity of each paragraph \n"
                    // 请参考gpt3.5模型的tokens计算规则，拆分后的每个段落tokens数量不超过 2000 tokens。
                    + "5. Please refer to the tokens calculation rules of the {{modelName}} model, the number of tokens per paragraph after splitting is not more than {{tokensLimit}} tokens. \n"
                    // 请尽可能的保证每个段落长度一致，允许你可以增加一些冗余内容来保证段落长度一致
                    + "6. Please make sure that each paragraph is as long as possible, allowing you to add some redundancy to keep the paragraphs consistent. \n"
                    // 在分段时，考虑语义相关性，如果有上下文关系或语义关系跨越多个句子，请确保相关的信息在同一个段落中。
                    + "7. When segmenting, consider semantic relevance, and if there is a contextual or semantic relationship that spans more than one sentence, make sure the relevant information is in the same paragraph. \n"
                    // 考虑文本的主题和段落结构，尽量在有意义的地方进行分段，以保持信息的逻辑组织。
                    + "8. Consider the subject matter and paragraph structure of the text and try to segment where it makes sense to keep the information logically organized. \n"
                    // 在拆分文本时，可以优先选择标点符号（句号、逗号等）作为分割点，以确保每个段落内有一定的完整性。
                    + "9. When splitting text, you can prioritize punctuation (periods, commas, etc.) as a split point to ensure some integrity within each paragraph. \n"
                    // 你可以对内容进行优化，例如：修改错别字、优化预发、为了使内容完整添加额外信息。但是不允许删减原文内容，也不允许扭曲原文表达的意思。
                    + "10. You are allowed to optimize the content, e.g. by fixing typos, optimizing pre-release, adding extra information in order to make the content complete. However, it is not allowed to delete the original content or distort the meaning expressed in the original text. \n"
                    // 请在每个段落的起始位置添加一个 15个字符的标记，这个标记是 :segment-start:
                    + "11. Please add a 15 character tag to the start of each paragraph, this tag is :segment-start:  \n"
                    // 请在每个段落的结束位置添加一个 13个字符的标记，这个标记是 :segment-end:
                    + "12. Please add a 13-character tag to the end of each paragraph, which is :segment-end: \n"
                    // 我将直接使用你的回答进一步逻辑处理，所以你的回答应该只包含数据拆分结果，没有其他信息。
                    + "13. I will use your answer directly for further logical processing, so your answer should contain only the data splitting results and no other information. \n"
    })
    String split(@V("fileType") String fileType, @V("information") String information, @V("modelName") String modelName, @V("tokensLimit") Integer tokensLimit);



}
