package org.ai.toolkit.aitk.service.impl;

import org.ai.toolkit.aitk.service.vo.UserVO;
import org.ai.toolkit.aitk.service.UserService;
import org.springframework.stereotype.Service;

@Service("userService")
public class UserServiceImpl implements UserService {

    @Override
    public UserVO getSelf() {
        UserVO userVO = new UserVO();
        userVO.setDisplayName("匿名用户");
        userVO.setId("1");
        userVO.setAvatar("/image/self.jpg");
        return userVO;
    }
}
