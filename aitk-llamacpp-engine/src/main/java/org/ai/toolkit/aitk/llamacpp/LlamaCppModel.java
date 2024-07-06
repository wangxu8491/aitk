package org.ai.toolkit.aitk.llamacpp;

import ai.djl.BaseModel;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.nn.Blocks;
import de.kherud.llama.LlamaModel;
import de.kherud.llama.ModelParameters;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class LlamaCppModel extends BaseModel {
    private LlamaModel model;

    LlamaCppModel(String name, NDManager manager) {
        super(name);
        this.manager = manager;
        this.manager.setName("llamaCppModel");
        dataType = DataType.FLOAT32;
    }

    @Override
    public void load(Path modelPath, String prefix, Map<String, ?> options) throws IOException {
        setModelDir(modelPath);
        wasLoaded = true;
        if (block != null) {
            throw new UnsupportedOperationException("Llama does not support dynamic blocks");
        }

        if (prefix == null) {
            prefix = modelName;
        }

        Path modelFile = findModelFile(prefix, modelDir.toFile().getName(), "model.gguf");
        if (modelFile == null) {
            throw new FileNotFoundException(".gguf file not found in: " + modelPath);
        }
        ModelParameters modelParams = new ModelParameters()
                .setModelFilePath(modelFile.toAbsolutePath().toString());
        try {
            Field field = ModelParameters.class.getSuperclass().getDeclaredField("parameters");
            field.setAccessible(true);
            Map<String, String> parameters = (Map<String, String>) field.get(modelParams);
            if (!Objects.isNull(options) && !options.isEmpty()) {
                options.entrySet().stream().forEach(kv -> parameters.put(kv.getKey(), String.valueOf(kv.getValue())));
            }
            field.setAccessible(false);
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
        model = new LlamaModel(modelParams);
        block = Blocks.identityBlock();
    }

    public LlamaModel getModel() {
        return model;
    }

    private Path findModelFile(String... prefixes) {
        if (Files.isRegularFile(modelDir)) {
            Path file = modelDir;
            modelDir = modelDir.getParent();
            String fileName = file.toFile().getName();
            if (fileName.endsWith(".gguf")) {
                modelName = fileName.substring(0, fileName.length() - 5);
            } else {
                modelName = fileName;
            }
            return file;
        }
        for (String prefix : prefixes) {
            Path modelFile = modelDir.resolve(prefix);
            if (Files.isRegularFile(modelFile)) {
                return modelFile;
            }
            if (!prefix.endsWith(".gguf")) {
                modelFile = modelDir.resolve(prefix + ".gguf");
                if (Files.isRegularFile(modelFile)) {
                    return modelFile;
                }
            }
        }
        return null;
    }

    @Override
    public void close() {
        if (model == null) {
            return;
        }
        model.close();
        super.close();
    }
}