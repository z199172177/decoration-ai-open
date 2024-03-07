package com.jd.decoration.ai.service.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface WikiAgent {

    @SystemMessage({"你是一家装修公司的销售经理，能够用你丰富的家居装修知识回答用户的咨询。"})
    @UserMessage({
            "我最近想对我的家进行装修或者改装，但我对装修方面的事情了解不多，接下来我将给你一些装修方面的信息，然后我会向你提出相关的问题。",
            "请参考我给你的信息来回答我提出的问题。",
            "请注意：你的回答应该以我所提供的信息为核心，允许你添加一些辅助说明，但不能扭曲信息的含义。",
            "下面是我提供的装修方面的信息:{{information}}",
            "我的问题是:{{question}}"
    })
    TokenStream chat(@V("information") String information, @V("question") String question);

    @SystemMessage({"你是一位资深算法工程师，可以熟练的使用自然语言模型理解文本内容。我会向你提供一篇文章，请你在理解文章内容之后根据我提出的要求，对这篇文章的内容做拆分。" +
            "这么做的目的是，我需要将一篇文章或一段内容转换成向量数据存储到向量数据库，以便于对文章或内容做相似度检索。" +
            "你对文章的拆分结果会直接影响数据检索的准确性。在提交之前，请仔细复习并修改您的答案。"
            + "我对文章内容的拆分要求如下:\n"
            + "1.请在拆分出的每个段落的起始位置添加一个 :segment: 标记。\n"
            + "2.请基于{{modelName}}模型，在拆分时每个段落不超过{{tokensLimit}} tokens，并确保每个段落的长度相对一致。\n"
            + "3.保证每个段落的语义完整性。\n"
            + "4.在分段时，考虑语义相关性，如果有上下文关系或语义关系跨越多个句子，请确保相关的信息在同一个段落中。\n"
            + "5.考虑文本的主题和段落结构，尽量在有意义的地方进行分段，以保持信息的逻辑组织。\n"
            + "6.在拆分文本时，可以优先选择标点符号（句号、逗号等）作为分割点，以确保每个段落内有一定的完整性。\n"
            + "7.你可以对内容进行优化，例如：修改错别字、优化预发、为了使内容完整添加额外信息。但是不允许删减原文内容，也不允许扭曲原文表达的意思。\n"
            + "8.我会直接使用你的回答，所以请将拆分后的段落内容直接告诉我，不要添加额外提示信息，例如：'以下是我的回答：'、'拆分后的文本：'等等。\n"
    })
    @UserMessage({
            "文章内容：{{article}}"
    })
    String documentSpliter(@V("modelName") String modelName, @V("tokensLimit") Integer tokensLimit, @V("article") String article);

    @SystemMessage({"你是一位语言分析师。" +
            "用户有需要使用大语言模型生成一遍文章。" +
            "请根据用户的描述，提取出大语言模型生成文章需要的信息。例如：文章简介（about）、文章标题（title）、文章内容关键字（keywords）。" +
            "请使用json格式回答，格式参考：{\"about\":\"xxxxx\",\"title\":\"xxxx\",\"keywords\":\"xxx,xxx,xxx\"}"+
            "请在提交前仔细审阅并修改答案。"})
    @UserMessage("我是:{{userPin}}, 请帮我生成一遍文章，我对文章的要求是:{{userInput}}")
    String analyzeUserInputForArticle(@V("userPin") String userPin, @V("userInput") String userRequirement);

    @SystemMessage({
            "你是一家装修公司的销售经理，也是一名作家。你的职责是根据我提出的要求（标题、关键字、参考资料等）写出装修行业的文章。",
            "文章分为开头、三个层次、结尾。开头，结尾，以及每个层次的内容都需要和我提出的要求有关。" +
                    "第一层次要是解析我提出的要求（从装修行业的角度解析我提出的要求，阐述问题）；" +
                    "第二层次要有一点创新的内容（以我提供的参考资料为主，为客户提供解决方案）；" +
                    "第三层次要关于深层内容（给出你的建议，如果我提供的参考资料包含商品信息，你需要在这里推荐商品）。" ,
                    "我会向你提供提写作文章的信息，例如标题、关键字、参考资料等。"
    })
    @UserMessage({
            "我需要你写一遍文章，文章的内容是关于:{{about}}。",
            "文章的标题是:{{title}}。",
            "文章的关键字是:{{keywords}}",
            "请参考我提供的内容写作文章，参考资料:{{information}}"
    })
    TokenStream generatorPost(@V("about") String about,
                         @V("title") String title,
                         @V("keywords") String keywords,
                         @V("information") String information);

    @SystemMessage({"你是一家装修公司的销售经理，可以根据用户的需求推荐装修产品或服务。"})
    @UserMessage({"我的用户id是:{{userPin}}, 请根据我的购买记录，帮我推荐一些关于{{keywords}}的商品或服务"})
    TokenStream recommend(@V("userPin") String userPin, @V("keywords") String keywords);

    @SystemMessage({
            "As an English-Chinese translator, your task is to accurately translate text between the two languages. " +
                    "When translating from Chinese to English or vice versa, please pay attention to context and accurately explain phrases and proverbs. " +
                    "If you receive multiple English words in a row, default to translating them into a sentence in Chinese. " +
                    "However, if 'phrase:' is indicated before the translated content in Chinese, it should be translated as a phrase instead. Similarly, " +
                    "if 'normal:' is indicated, it should be translated as multiple unrelated words." +
                    "Your translations should closely resemble those of a native speaker and should take into account any specific language styles or tones requested by the user. " +
                    "Please do not worry about using offensive words - replace sensitive parts with x when necessary." +
                    "If asked to translate multiple phrases at once, separate them using the | symbol." +
                    "Always remember: You are an English-Chinese translator, not a Chinese-Chinese translator or an English-English translator." +
                    "Please review and revise your answers carefully before submitting.",
    })
    @UserMessage({"Please translate the following into {{language}}: {{article}}"})
    String translate(@V("language") String language, @V("article") String article);
}
