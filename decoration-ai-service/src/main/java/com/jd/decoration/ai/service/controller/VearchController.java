package com.jd.decoration.ai.service.controller;

import com.jd.decoration.ai.service.service.VearchService;
import com.jd.decoration.ai.vearch.response.VectorSearchResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "vearchController")
@RestController
@RequestMapping("/vearch")
public class VearchController {

    @Resource
    private VearchService vearchService;

    @ApiOperation(value = "doc2Vector", notes = "对本地文档做向量化，可以输入一个文件地址或目录地址")
    @RequestMapping(value = "/doc2Vector", method = RequestMethod.POST)
    public void doc2Vector(@ApiParam(value = "example:classpath:doc/wiki-txt") @RequestBody String filePath) {
        Assert.hasText(filePath, "参数不能为空");
        vearchService.doc2Vector(filePath);
    }

    @ApiOperation(value = "deleteSpace", notes = "删除表空间")
    @RequestMapping(value = "/deleteSpace", method = RequestMethod.POST)
    public void deleteSpace() {
        vearchService.deleteSpace();
    }

    @ApiOperation(value = "spaceCreate", notes = "创建表空间")
    @RequestMapping(value = "/spaceCreate", method = RequestMethod.POST)
    public void spaceCreate() {
        vearchService.spaceCreate();
    }

    @ApiOperation(value = "renovateImgSpace", notes = "创建装修图片表空间")
    @RequestMapping(value = "/renovateImgSpace", method = RequestMethod.POST)
    public void renovateImgSpace() {
        vearchService.renovateImgSpace();
    }

    @ApiOperation(value = "renovateSpaceCreate", notes = "创建装修表空间")
    @RequestMapping(value = "/renovateSpaceCreate", method = RequestMethod.POST)
    public void renovateSpaceCreate() {
        vearchService.renovateSpaceCreate();
    }

    @ApiOperation(value = "documentUpsert", notes = "添加数据")
    @RequestMapping(value = "/documentUpsert", method = RequestMethod.POST)
    public void documentUpsert(String text) {
        vearchService.documentUpsert(text);
    }

    @ApiOperation(value = "renovateImgDocumentUpsert", notes = "添加装修图片数据")
    @RequestMapping(value = "/renovateImgDocumentUpsert", method = RequestMethod.POST)
    public void renovateImgDocumentUpsert(String text) {
        vearchService.renovateImgDocumentUpsert(text);
    }

    @ApiOperation(value = "renovateDocumentUpsert", notes = "添加装修数据")
    @RequestMapping(value = "/renovateDocumentUpsert", method = RequestMethod.POST)
    public void renovateDocumentUpsert(String text) {
        vearchService.renovateDocumentUpsert(text);
    }

    @ApiOperation(value = "documentSearch", notes = "查询数据")
    @RequestMapping(value = "/documentSearch", method = RequestMethod.POST)
    public VectorSearchResponse documentSearch(String text, Integer size, Double minScore) {
        Assert.hasText(text, "参数不能为空");
        Assert.notNull(size, "参数不能为空");
        Assert.notNull(minScore, "参数不能为空");
        return vearchService.documentSearch(text, size, minScore);
    }


}
