package org.ai.toolkit.aitk.modelzoo;

import org.ai.toolkit.aitk.common.git.GitEnum;
import org.ai.toolkit.aitk.common.git.GitUtil;
import org.ai.toolkit.aitk.modelzoo.executor.InferenceExecutor;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractBaseModelDefinition<P, Q> implements ModelDefinition<P, Q> {

    @Autowired
    protected InferenceExecutor inferenceExecutor;

    @Autowired
    protected ModelRepositoryType modelRepositoryType;

    protected GitEnum defaultGit() {
        return modelRepositoryType.getDefaultGitEnum();
    }

    protected Path getModelPath(String modelDir) {
        return Paths.get(GitUtil.getModelBasePath(defaultGit()), modelDir);
    }
}
