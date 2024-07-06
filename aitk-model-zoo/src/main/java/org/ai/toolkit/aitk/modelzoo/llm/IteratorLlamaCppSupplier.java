package org.ai.toolkit.aitk.modelzoo.llm;

import ai.djl.ndarray.BytesSupplier;
import de.kherud.llama.LlamaOutput;

import java.nio.ByteBuffer;
import java.util.Iterator;

public class IteratorLlamaCppSupplier implements BytesSupplier, Iterator<LlamaOutput> {

    private Iterator<LlamaOutput> sources;

    public IteratorLlamaCppSupplier(Iterator<LlamaOutput> sources) {
        this.sources = sources;
    }

    @Override
    public boolean hasNext() {
        return sources.hasNext();
    }

    @Override
    public LlamaOutput next() {
        return sources.next();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(new byte[]{});
    }
}