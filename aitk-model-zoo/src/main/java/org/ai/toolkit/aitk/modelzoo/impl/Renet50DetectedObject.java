package org.ai.toolkit.aitk.modelzoo.impl;

import ai.djl.Application;
import ai.djl.engine.Engine;
import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.ai.toolkit.aitk.modelzoo.bean.ModelBasicInfo;
import org.ai.toolkit.aitk.modelzoo.bean.Param;
import org.ai.toolkit.aitk.modelzoo.constant.EngineEnum;
import org.ai.toolkit.aitk.modelzoo.constant.FileExtension;
import org.ai.toolkit.aitk.modelzoo.constant.ModelTypeEnum;
import org.ai.toolkit.aitk.modelzoo.ModelDefinition;
import org.ai.toolkit.aitk.modelzoo.util.FileUtil;
import org.springframework.stereotype.Component;

@Component
public class Renet50DetectedObject implements ModelDefinition<Image, DetectedObjects> {

    @Override
    public String getId() {
        return "cv/object_detection/resnet50";
    }

    @Override
    public ModelBasicInfo getModelBasicInfo() {
        return new ModelBasicInfo("目标检测", "SSD目标检测", "/modelimage/model.jpg", "请输入一张图片");
    }

    @Override
    public Image postProcessBeforeModel(Input input) throws Exception {
        Param param = getRequestParams().get(0);
        byte[] bytes = input.getAsBytes(param.getName());
        return ImageFactory.getInstance().fromInputStream(new ByteArrayInputStream(bytes));
    }

    @Override
    public Output postProcessAfterModel(Input input, DetectedObjects modelOutput) throws Exception {
        Image image = postProcessBeforeModel(input);
        image.drawBoundingBoxes(modelOutput);
        String path = FileUtil.saveImage(image, FileExtension.PNG);
        Output output = new Output();
        output.add("url", path);
        output.add("text", modelOutput.toJson());
        return output;
    }

    @Override
    public Criteria getCriteria() {
        Criteria<Image, DetectedObjects> criteria =
            Criteria.builder()
                .optApplication(Application.CV.OBJECT_DETECTION)
                .setTypes(Image.class, DetectedObjects.class)
                .optFilter("backbone", "resnet50")
                .optEngine(Engine.getDefaultEngineName())
                .optProgress(new ProgressBar())
                .build();
        return criteria;
    }

    @Override
    public EngineEnum getEngine() {
        return EngineEnum.MXNet;
    }

    @Override
    public ModelTypeEnum getModelType() {
        return ModelTypeEnum.IMAGE;
    }

    @Override
    public List<Param> getLoadModelParams() {
        return new ArrayList<>();
    }

    @Override
    public List<Param> getRequestParams() {
        List<Param> params = new ArrayList<>();
        params.add(new Param("file", FileExtension.PNG));
        return params;
    }

    @Override
    public List<Param> getResponseParams() {
        List<Param> params = new ArrayList<>();
        params.add(new Param("url", FileExtension.PNG));
        params.add(new Param("text", FileExtension.TEXT));
        return params;
    }
}
