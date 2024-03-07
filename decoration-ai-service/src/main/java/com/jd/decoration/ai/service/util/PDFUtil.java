package com.jd.decoration.ai.service.util;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.jd.decoration.ai.service.tools.ImageConvertBase64;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.fit.pdfdom.PDFDomTree;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.Assert;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class PDFUtil {

    public static void main(String[] args) {
        String bookName = "Renovation, do three things well enough";
//        String bookName = "Renovation, do three things well enough";
        String sourceFilePath = String.format("/Users/ext.zhoukun9/Downloads/books/renovateBooks/%s/%s.pdf", bookName, bookName);
        String targetFolderPath = String.format("/Users/ext.zhoukun9/Downloads/books/renovateBooks/%s/parse", bookName);
        String targetHtmlFolderPath = String.format("/Users/ext.zhoukun9/Downloads/books/renovateBooks/%s/parse/html", bookName);
//         convert2TxtAndSave(sourceFilePath, targetFolderPath, false);
//        convert2TxtAndSave(sourceFilePath, targetFolderPath, 1, Integer.MAX_VALUE, true, bookName + ".txt", false);
//         pdfConvert2Html(sourceFilePath, targetHtmlFolderPath, false);

//        htmlParm(targetHtmlFolderPath, "0_textOnly.txt", false, 11, 482);
        htmlParm(targetHtmlFolderPath, "0_textAndImg.txt", true, 11, 482);
//        htmlParm(targetHtmlFolderPath, "0_textAndImg.txt", true, 11, 482);
    }

    private static final String bodyTextFontSize = "16.500002pt";
    private static final String bodyTextFontSizeFilter = "12.375001pt";
    private static final String bodyContextWord = "。";
    private static final String bodyTextLeft = "109.9922pt";

    @SneakyThrows
    public static void htmlParm(String htmlFileFolder, String textFileName, Boolean saveImg, Integer beginPageIndex, Integer endPageIndex) {
        Assert.hasText(htmlFileFolder, "Param htmlFileFolder can't blank.");

        File targetFolder = FileUtil.newFile(htmlFileFolder);
        Assert.isTrue(targetFolder.isDirectory(), "targetFolder: " + targetFolder + " Must be a directory.");

        List<File> fileList = FileUtil.loopFiles(targetFolder);
        fileList = fileList.stream()
                .filter(item -> StrUtil.endWith(item.getName(), ".html"))
                .sorted(Comparator.comparing(item -> {
                    String mainName = FileUtil.mainName(item);
                    return Convert.toInt(mainName.split("_")[1]);
                })).collect(Collectors.toList());

        for (File file : fileList) {
            String fileName = file.getName();
            if (!StrUtil.endWith(fileName, ".html")) {
                continue;
            }
            Document doc = Jsoup.parse(FileUtil.readUtf8String(file));
            Element body = doc.body();
            Elements bodyChildrenElements = body.children();
            for (Element childrenElement : bodyChildrenElements) {
                String childrenTagName = childrenElement.tagName();
                String childrenClassName = childrenElement.className();
                String id = childrenElement.attr("id");
                if (!StrUtil.equals(childrenTagName, "div") || !StrUtil.equals(childrenClassName, "page")) {
                    continue;
                }
                if (StrUtil.isBlank(id)) {
                    continue;
                }
                List<String> idSplit = StrUtil.split(id, "_");
                if (idSplit.size() < 2) {
                    continue;
                }
                Integer pageNo = Convert.toInt(idSplit.get(1), null);
                if (pageNo == null) {
                    continue;
                }
                if (pageNo < beginPageIndex || pageNo > endPageIndex) {
                    continue;
                }

                Elements contentElements = childrenElement.children();
                for (Element contentEle : contentElements) {
                    //遍历标签
                    StringBuilder buffer = new StringBuilder();

                    String tagName = contentEle.tagName();
                    if (StrUtil.equals(tagName, "img") && saveImg) {
                        //保存图片
                        Attribute src = contentEle.attribute("src");
                        String imgBase64 = src.getValue();

                        String imageName = String.format("%s_%s.%s", id, System.currentTimeMillis(), ImgUtil.IMAGE_TYPE_JPEG);
                        String imageSavePath = String.format("%s/img/%s", htmlFileFolder, imageName);

                        ImageConvertBase64.toImage(imgBase64, new File(imageSavePath));
                        buffer.append("\n");
                        buffer.append("imgUrl:").append(imageSavePath);
                        buffer.append("\n");
                        FileUtil.appendUtf8String(buffer.toString(), htmlFileFolder + "/" + textFileName);
                        continue;
                    }

                    String className = contentEle.className();
                    if (StrUtil.equals(className, "r")) {
                        continue;
                    }

                    String text = contentEle.text();
                    if (StrUtil.equals(className, "p")) {
                        Attribute style = contentEle.attribute("style");
                        Map<String, String[]> styleMap = getStyleMap(style.getValue());
                        String[] fontSizeAry = styleMap.get("font-size");
                        if (ArrayUtil.contains(fontSizeAry, bodyTextFontSizeFilter)) {
                            continue;
                        }
                        if (!ArrayUtil.contains(fontSizeAry, bodyTextFontSize)) {
                            buffer.append("\n\n");
                            buffer.append("\n\n======page======\n\n");
                            buffer.append(text);
                            buffer.append("\n\n");
                            FileUtil.appendUtf8String(buffer.toString(), htmlFileFolder + "/" + textFileName);
                            continue;
                        }

                        String[] left = styleMap.get("left");
                        if (ArrayUtil.contains(left, bodyTextLeft)) {
                            buffer.append("\n\n");
                        }
                        buffer.append(text);
                    }

                    FileUtil.appendUtf8String(buffer.toString(), htmlFileFolder + "/" + textFileName);

                }
            }

        }

    }

    public static Map<String, String[]> getStyleMap(String styleStr) {
        Map<String, String[]> keymaps = new HashMap<>();
        // margin-top:-80px !important;color:#fcc;border-bottom:1px solid #ccc; background-color: #333; text-align:center
        String[] list = styleStr.split(":|;");
        for (int i = 0; i < list.length; i+=2) {
            keymaps.put(list[i].trim(),list[i+1].trim().split(" "));
        }
        return keymaps;
    }

    public static void pdfConvert2Html(String sourceFilePath, String targetFolderPath, boolean saveImage) {
        Assert.hasText(sourceFilePath, "Param pdfSourcePath can't blank.");
        Assert.hasText(targetFolderPath, "Param targetFolderPath can't blank.");

        File sourceFile = FileUtil.newFile(sourceFilePath);
        Assert.isTrue(sourceFile.isFile(), "sourceFilePath: " + sourceFilePath + " must be a pdf file.");

        File targetFolder = FileUtil.newFile(targetFolderPath);
        if (!FileUtil.exist(targetFolderPath)) {
            FileUtil.mkdir(targetFolderPath);
        }
        Assert.isTrue(targetFolder.isDirectory(), "targetFolder: " + targetFolder + " Must be a directory.");

        try (PDDocument document = PDDocument.load(sourceFile)) {
//        try (PDDocument document = Loader.loadPDF(sourceFile)) {

            int p = 1;

            PDFDomTree pdfDomTree = new PDFDomTree();
            for (PDPage page : document.getPages()) {
                pdfDomTree.setStartPage(p);
                pdfDomTree.setEndPage(p);
                pdfDomTree.setSortByPosition(true);

                String text = pdfDomTree.getText(document);
                Document htmlDoc = Jsoup.parse(text);
                Elements pageElement = htmlDoc.body().getElementsByClass("page");
                String pageElementId = pageElement.attr("id");
                FileUtil.writeUtf8String(text, targetFolderPath + "/" + pageElementId + ".html");

                p++;
            }


        } catch (Exception e) {
            log.info("convert2TxtAndSave exception msg:{}", e.getMessage(), e);
        }
    }

    public static void convert2TxtAndSave(String sourceFilePath, String targetFolderPath, boolean saveImage) {
        convert2TxtAndSave(sourceFilePath, targetFolderPath, 1, Integer.MAX_VALUE, false, "", saveImage);
    }

    public static void convert2TxtAndSave(String sourceFilePath, String targetFolderPath, Integer pageStart, Integer pageEnd, Boolean fileAppend, String appendFileName, boolean saveImage) {
        Assert.hasText(sourceFilePath, "Param pdfSourcePath can't blank.");
        Assert.hasText(targetFolderPath, "Param txtSavePath can't blank.");

        File sourceFile = FileUtil.newFile(sourceFilePath);
        Assert.isTrue(sourceFile.isFile(), "sourceFilePath: " + sourceFilePath + " must be a pdf file.");

        File targetFolder = FileUtil.newFile(targetFolderPath);
        Assert.isTrue(targetFolder.isDirectory(), "targetFolder: " + targetFolder + " Must be a directory.");

        try (PDDocument document = PDDocument.load(sourceFile)) {
//        try (PDDocument document = Loader.loadPDF(sourceFile)) {
            int p = 1;
            for (PDPage page : document.getPages()) {
                if (p < pageStart || p > pageEnd) {
                    p++;
                    continue;
                }

                if (saveImage) {
                    PDResources resources = page.getResources();
                    Iterable<COSName> cosNames = resources.getXObjectNames();
                    if (cosNames == null) {
                        continue;
                    }

                    int imageIndex = 0;
                    for (COSName cosName : cosNames) {
                        //判断给定名称的XObject资源是否为图像
                        if (resources.isImageXObject(cosName)) {
                            imageIndex++;

                            PDImageXObject pdImage = (PDImageXObject) resources.getXObject(cosName);
                            BufferedImage image = pdImage.getImage();

                            String imageName = String.format("p%s_img%s.%s", p, imageIndex, ImgUtil.IMAGE_TYPE_JPEG);
                            String imageSavePath = String.format("%s/imgs/%s", targetFolderPath, imageName);

                            boolean imgWriteRet = ImageIO.write(image, ImgUtil.IMAGE_TYPE_JPEG, FileUtil.newFile(imageSavePath));
                            log.info("convert2TxtAndSave image:{} save:{}", imageName, imgWriteRet);
                        }
                    }
                }

                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(p);
                stripper.setEndPage(p);
                stripper.setSortByPosition(true);
                String text = stripper.getText(document);

                String textName;
                String textSavePath;
                File textSaveFile;
                if (fileAppend && StrUtil.isNotBlank(appendFileName)) {
                    textName = appendFileName;
                    textSavePath = String.format("%s/%s", targetFolderPath, textName);
                    textSaveFile = FileUtil.newFile(textSavePath);
                    FileUtil.appendUtf8String(text, textSaveFile);
                } else {
                    textName = String.format("p%s.%s", p, "txt");
                    textSavePath = String.format("%s/%s", targetFolderPath, textName);
                    textSaveFile = FileUtil.newFile(textSavePath);
                    FileUtil.writeUtf8String(text, textSaveFile);
                }

                log.info("convert2TxtAndSave text:{} save:{}", textName, textSaveFile.exists());

                Writer writer = new FileWriter(textSavePath, true);
                stripper.writeText(document, writer);

                p++;
            }
        } catch (Exception e) {
            log.info("convert2TxtAndSave exception msg:{}", e.getMessage(), e);
        }
    }

}
