package org.ai.toolkit.aitk.controller;

import ai.djl.modality.Input;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.ai.toolkit.aitk.common.errorcode.AitkErrorCode;
import org.ai.toolkit.aitk.common.exception.AitkException;
import org.ai.toolkit.aitk.modelzoo.bean.Param;
import org.ai.toolkit.aitk.modelzoo.constant.IOTypeEnum;
import org.ai.toolkit.aitk.service.vo.MessageVO;
import org.ai.toolkit.aitk.service.vo.ModelAsContactVO;
import org.ai.toolkit.aitk.service.vo.ModelParamVO;
import org.ai.toolkit.aitk.service.vo.UserVO;
import org.ai.toolkit.aitk.service.ModelService;
import org.ai.toolkit.aitk.service.UserService;
import org.ai.toolkit.aitk.vo.ResultVO;
import org.ai.toolkit.aitk.websocket.WebSocketManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RequestMapping("/im")
@RestController
public class ImController {

    private static Logger LOGGER = LoggerFactory.getLogger(ImController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private WebSocketManager webSocketManager;

    private static final String MODEL_ID_KEY = "modelId";

    private static final String USER_ID_KEY = "userId";

    @RequestMapping("getSelf")
    public ResultVO<UserVO> getSelf() {
        UserVO userVO = userService.getSelf();
        return ResultVO.createSuccessResultVO(userVO);
    }

    @RequestMapping("getModelList")
    public ResultVO<List<ModelAsContactVO>> getModelList() {
        return ResultVO.createSuccessResultVO(modelService.getAllModels());
    }

    @RequestMapping("pullMessage")
    public ResultVO<MessageVO> pullMessage(@RequestParam(value = "modelId", required = true) String modelId) {
        ModelParamVO modelParamVO = modelService.getModelParamVO(modelId);
        MessageVO messageVO = new MessageVO();
        messageVO.setId(UUID.randomUUID().toString());
        messageVO.setContent(modelParamVO.getModelBasicInfo().getPrompt());
        messageVO.setType("text");
        messageVO.setToContactId(modelId);
        messageVO.setFromUser(createUser(modelId));
        return ResultVO.createSuccessResultVO(messageVO);
    }

    @RequestMapping("handleMessage")
    public void handleMessage(MultipartHttpServletRequest request) throws IOException {
        String modelId = request.getParameter(MODEL_ID_KEY);
        String userId = request.getParameter(USER_ID_KEY);
        if (!StringUtils.hasLength(modelId)) {
            throw new AitkException(AitkErrorCode.PARAMETER_VALIDATION_ERROR, "missing modelId parameter");
        }
        Map<String, MultipartFile> multipartFileMap = request.getFileMap();
        ModelParamVO modelParamVO = modelService.getModelParamVO(modelId);
        List<Param> requestParams = modelParamVO.getRequestParams();
        Input input = new Input();
        for (Param param : requestParams) {
            if (IOTypeEnum.TEXT.equals(param.getFileExtension().getFileType())) {
                input.add(param.getName(), request.getParameter(param.getName()));
            } else {
                input.add(param.getName(), multipartFileMap.get(param.getName()).getBytes());
            }
        }
        modelService.predict(modelId, input, (throwable, output) -> {
            if (!Objects.isNull(throwable)) {
                LOGGER.error("Model execution encountered an exception", throwable);
                return;
            }
            List<Param> responseParams = modelParamVO.getResponseParams();
            for (Param param : responseParams) {
                if (IOTypeEnum.STREAM.equals(param.getFileExtension().getFileType())) {

                } else {
                    MessageVO messageVO = new MessageVO();
                    messageVO.setId(UUID.randomUUID().toString());
                    messageVO.setContent(output.getAsString(param.getName()));
                    messageVO.setType(param.getFileExtension().getFileType().name().toLowerCase());
                    messageVO.setToContactId(modelId);
                    messageVO.setFromUser(createUser(modelId));
                    Gson gson = new Gson();
                    webSocketManager.sendMessage(userId, gson.toJson(messageVO));
                }
            }
        });
    }

    private UserVO createUser(String modelId) {
        ModelAsContactVO modelAsContactVO = modelService.getModelAsContactVO(modelId);
        UserVO userVO = new UserVO();
        userVO.setId(modelAsContactVO.getId());
        userVO.setDisplayName(modelAsContactVO.getDisplayName());
        userVO.setAvatar(modelAsContactVO.getAvatar());
        return userVO;
    }

}
