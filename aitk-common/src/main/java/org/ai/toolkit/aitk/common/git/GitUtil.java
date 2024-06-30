package org.ai.toolkit.aitk.common.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

import static org.ai.toolkit.aitk.common.constant.PathConstants.ATTACHMENT_PARENT_PATH;
import static org.ai.toolkit.aitk.common.constant.PathConstants.ATTACHMENT_PATH;

public class GitUtil {

    private static final String GITEE_URL = "https://gitee.com/AI_Toolkit/modelzoo.git";

    private static final String GITEE_MODEL_PATH = ATTACHMENT_PARENT_PATH + File.separator + ATTACHMENT_PATH + File.separator + "models" + File.separator + "gitee";

    public static void gitClone(GitEnum gitEnum) throws GitAPIException {
        Git.cloneRepository()
                .setURI(GITEE_URL)
                .setDirectory(new File(GITEE_MODEL_PATH))
                .setCloneAllBranches(true)
                .call();
    }

    public static void gitPull(GitEnum gitEnum) throws IOException, GitAPIException {
        Git.open(new File(GITEE_MODEL_PATH)).pull().call();
    }

    public static String getModelBasePath(GitEnum gitEnum){
        if (GitEnum.GITEE.equals(gitEnum)){
            return GITEE_MODEL_PATH;
        }else if (GitEnum.GITHUB.equals(gitEnum)){

        }
        return null;
    }
}
