package com.ngc0202.tokenmarket;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface PluginSerializable<T> {
    Factory<T> getSerializationFactory();

    public interface Factory<T> {
        void serialize(T x, DataOutput output, int version) throws IOException;

        T deserialize(DataInput intput, int version, TokenMarket plugin) throws IOException;
    }
}
