package com.szprimary.english.core.progress;

/** 内存实现，用于测试和首次运行的兜底。 */
public final class MemoryProgressStore implements ProgressStore {
    private String json = "";

    public MemoryProgressStore() {
    }

    public MemoryProgressStore(String initial) {
        this.json = initial;
    }

    @Override
    public String read() {
        return json;
    }

    @Override
    public void write(String value) {
        this.json = value;
    }
}
