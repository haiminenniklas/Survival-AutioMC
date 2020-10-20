package me.tr.survival.main.other;

import org.bukkit.inventory.meta.tags.ItemTagAdapterContext;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.nio.ByteBuffer;

public class IntegerItemTagType implements ItemTagType<byte[], Integer> {

    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public Class<Integer> getComplexType() {
        return Integer.class;
    }

    @Override
    public byte[] toPrimitive( Integer integer,  ItemTagAdapterContext itemTagAdapterContext) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE / 8);
        buffer.putInt(integer);
        return buffer.array();
    }

    @Override
    public Integer fromPrimitive( byte[] bytes,  ItemTagAdapterContext itemTagAdapterContext) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getInt();
    }
}
