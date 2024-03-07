package com.jd.decoration.ai.service.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WikiTools {

    @Tool("get a record of the list of products purchased by a user")
    public String getProductListByUser(String userPin) {
        log.info("getProductListByUser userPin:{}", userPin);

        return "10月1日购买美的牌空调, 10月2日购买欧派牌抽油烟机, 10月3日购买美的空气净化器, 10月4日购买美的冰箱";
    }

    @Tool("use keywords to search the product list")
    public String searchProductListByKeywords(String keywords) {
        log.info("searchProductListByKeywords keywords:{}", keywords);

        StringBuffer buffer = new StringBuffer();
        buffer.append(String.format("商品id:%s, 商品名称:%s, 商品价格:%s 元", "24362609241", "百安居厨房翻新套餐 签约款 厨房间全包改造 环保家装 局部装修", "2000.00"));
        buffer.append(String.format("商品id:%s, 商品名称:%s, 商品价格:%s 元", "10059674308024", "旧房老房厨房改造整体局部改造翻新焕新装修设计橱柜台面柜门卫生间淋浴隔断更换上面服务预付款 厨房局部改造", " 30.00"));
        buffer.append(String.format("商品id:%s, 商品名称:%s, 商品价格:%s 元", "100077413090", "爱空间xJD厨卫局装套餐厨房卫生间局部改造武汉专属", "1.00"));
        buffer.append(String.format("商品id:%s, 商品名称:%s, 商品价格:%s 元", "10068912285524", "欧派橱柜定制 整体橱柜定做石英石台面吊柜厨房装修 星月夜 预付金", "1000.00"));
        buffer.append(String.format("商品id:%s, 商品名称:%s, 商品价格:%s 元", "100067146935", "奥克斯（AUX）鸳鸯锅 电火锅 多用途锅电煮锅电锅一体多功能锅家用电热锅火锅专用锅6L WW-M8287", "117.00"));
        buffer.append(String.format("商品id:%s, 商品名称:%s, 商品价格:%s 元", "10093932228303", "美的快捷微波炉 家用小型 360°转盘加热 旋钮操控 易洁内胆（M1-L213B） 白色 20L", "269.00"));
        return buffer.toString();
    }

}
