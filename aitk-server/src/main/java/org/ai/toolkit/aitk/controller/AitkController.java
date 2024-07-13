package org.ai.toolkit.aitk.controller;

import ai.djl.modality.Input;
import ai.djl.ndarray.BytesSupplier;
import com.google.gson.Gson;
import org.ai.toolkit.aitk.common.errorcode.AitkErrorCode;
import org.ai.toolkit.aitk.common.exception.AitkException;
import org.ai.toolkit.aitk.modelzoo.bean.Param;
import org.ai.toolkit.aitk.modelzoo.constant.IOTypeEnum;
import org.ai.toolkit.aitk.modelzoo.llm.IteratorLlamaCppSupplier;
import org.ai.toolkit.aitk.service.ModelService;
import org.ai.toolkit.aitk.service.vo.ModelNodeDataVO;
import org.ai.toolkit.aitk.service.vo.ModelParamVO;
import org.ai.toolkit.aitk.vo.ResultVO;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RequestMapping("/aitk")
@RestController
public class AitkController {

    @Resource
    private ModelService modelService;

    @RequestMapping("/getModelTreeData")
    public ResultVO<List<ModelNodeDataVO>> getModelTreeData() {
        return ResultVO.createSuccessResultVO(modelService.getModelTreeData());
    }


//    @RequestMapping("handleMessage")
//    public void handleMessage(MultipartHttpServletRequest request) throws IOException {
//        String modelId = request.getParameter(MODEL_ID_KEY);
//        String userId = request.getParameter(USER_ID_KEY);
//        if (!StringUtils.hasLength(modelId)) {
//            throw new AitkException(AitkErrorCode.PARAMETER_VALIDATION_ERROR, "missing modelId parameter");
//        }
//        Map<String, MultipartFile> multipartFileMap = request.getFileMap();
//        ModelParamVO modelParamVO = modelService.getModelParamVO(modelId);
//        List<Param> requestParams = modelParamVO.getRequestParams();
//        Input input = new Input();
//        for (Param param : requestParams) {
//            if (IOTypeEnum.TEXT.equals(param.getFileExtension().getFileType())) {
//                input.add(param.getName(), request.getParameter(param.getName()));
//            } else {
//                input.add(param.getName(), multipartFileMap.get(param.getName()).getBytes());
//            }
//        }
//        modelService.predict(modelId, input, (throwable, output) -> {
//            if (!Objects.isNull(throwable)) {
//                LOGGER.error("Model execution encountered an exception", throwable);
//                return;
//            }
//            List<Param> responseParams = modelParamVO.getResponseParams();
//            for (Param param : responseParams) {
//                if (IOTypeEnum.STREAM.equals(param.getFileExtension().getFileType())) {
//                    BytesSupplier bytesSupplier = output.get(param.getName());
//                    if (bytesSupplier instanceof IteratorLlamaCppSupplier) {
//                        IteratorLlamaCppSupplier iteratorLlamaCppSupplier = (IteratorLlamaCppSupplier) bytesSupplier;
//                        while (iteratorLlamaCppSupplier.hasNext()){
//                            MessageVO messageVO = new MessageVO();
//                            messageVO.setId(UUID.randomUUID().toString());
//                            messageVO.setContent(iteratorLlamaCppSupplier.next().text);
//                            messageVO.setType("text");
//                            messageVO.setToContactId(modelId);
//                            messageVO.setFromUser(createUser(modelId));
//                            Gson gson = new Gson();
//                            webSocketManager.sendMessage(userId, gson.toJson(messageVO));
//                        }
//                    }
//                } else {
//                    MessageVO messageVO = new MessageVO();
//                    messageVO.setId(UUID.randomUUID().toString());
//                    messageVO.setContent(output.getAsString(param.getName()));
//                    messageVO.setType(param.getFileExtension().getFileType().name().toLowerCase());
//                    messageVO.setToContactId(modelId);
//                    messageVO.setFromUser(createUser(modelId));
//                    Gson gson = new Gson();
//                    webSocketManager.sendMessage(userId, gson.toJson(messageVO));
//                }
//            }
//        });
//    }

}
