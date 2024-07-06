package org.ai.toolkit.aitk.modelzoo.paddle;

import ai.djl.modality.Classifications;
import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.paddlepaddle.zoo.cv.imageclassification.PpImageClassificationTranslatorFactory;
import ai.djl.repository.zoo.Criteria;
import org.ai.toolkit.aitk.modelzoo.AbstractBaseModelDefinition;
import org.ai.toolkit.aitk.modelzoo.bean.ModelBasicInfo;
import org.ai.toolkit.aitk.modelzoo.bean.Param;
import org.ai.toolkit.aitk.modelzoo.constant.EngineEnum;
import org.ai.toolkit.aitk.modelzoo.constant.FileExtension;
import org.ai.toolkit.aitk.modelzoo.constant.ModelTypeEnum;
import org.ai.toolkit.aitk.modelzoo.util.FileUtil;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class MaskDetection extends AbstractBaseModelDefinition<Image, DetectedObjects> {

    @Override
    public String getId() {
        return "cv/face/maskDetection";
    }

    @Override
    public ModelBasicInfo getModelBasicInfo() {
        return new ModelBasicInfo("口罩人脸识别", "paddle", "/modelimage/model.jpg", "请输入一张图片");
    }

    @Override
    public Image postProcessBeforeModel(Input input) throws Exception {
        Param param = getRequestParams().get(0);
        byte[] bytes = input.getAsBytes(param.getName());
        return ImageFactory.getInstance().fromInputStream(new ByteArrayInputStream(bytes));
    }

    @Override
    public Output postProcessAfterModel(Input input, DetectedObjects modelOutput) throws Exception {
        Image img = postProcessBeforeModel(input);
        List<DetectedObjects.DetectedObject> faces = modelOutput.items();
        List<String> names = new ArrayList<>();
        List<Double> prob = new ArrayList<>();
        List<BoundingBox> rect = new ArrayList<>();
        for (DetectedObjects.DetectedObject face : faces) {
            Image subImg = getSubImage(img, face.getBoundingBox());
            Classifications classifications = inferenceExecutor.asyncExecute(getId(), subImg, 1);
            names.add(classifications.best().getClassName());
            prob.add(face.getProbability());
            rect.add(face.getBoundingBox());
        }
        img.drawBoundingBoxes(new DetectedObjects(names, prob, rect));
        String path = FileUtil.saveImage(img, FileExtension.PNG);
        Output output = new Output();
        output.add("url", path);
        return output;
    }

    @Override
    public List<Criteria> getCriteriaList() {
        Path modelUrl = getModelPath("/cv/face/paddle/mask_classification");
        Criteria<Image, Classifications> classifier =
                Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optModelPath(modelUrl)
                        .optTranslatorFactory(new PpImageClassificationTranslatorFactory())
                        .optOption("enableONNXRuntime", "true")
                        .optOption("enableOrtOptimization", "true")
                        .build();

        Criteria<Image, DetectedObjects> detectFaces =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optArtifactId("ai.djl.paddlepaddle:face_detection")
                        .optFilter("flavor", "server")
                        .optOption("enableONNXRuntime", "true")
                        .optOption("enableOrtOptimization", "true")
                        .build();
        return Arrays.asList(detectFaces, classifier);
    }

    @Override
    public List<EngineEnum> getEngineList() {
        return Arrays.asList(EngineEnum.PaddlePaddle, EngineEnum.PaddlePaddle);
    }

    @Override
    public ModelTypeEnum getModelType() {
        return ModelTypeEnum.CV;
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
        return params;
    }

    private Image getSubImage(Image img, BoundingBox box) {
        Rectangle rect = box.getBounds();
        int width = img.getWidth();
        int height = img.getHeight();
        int[] squareBox =
                extendSquare(
                        rect.getX() * width,
                        rect.getY() * height,
                        rect.getWidth() * width,
                        rect.getHeight() * height,
                        0.18);
        return img.getSubImage(squareBox[0], squareBox[1], squareBox[2], squareBox[2]);
    }

    private int[] extendSquare(
            double xmin, double ymin, double width, double height, double percentage) {
        double centerx = xmin + width / 2;
        double centery = ymin + height / 2;
        double maxDist = Math.max(width / 2, height / 2) * (1 + percentage);
        return new int[]{
                (int) (centerx - maxDist), (int) (centery - maxDist), (int) (2 * maxDist)
        };
    }
}
