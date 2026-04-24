package com.qiumengzhou.tpaqmz.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModSavedData extends SavedData {
    public final Map<UUID, BackPosition> lastPos = new HashMap<>();
    public final Map<UUID, BackPosition> deathPos = new HashMap<>();
    public int globalCooldown = 30;
    public boolean allowDangerTp = false;

    public ModSavedData() {}

    // 读取 NBT
    public static ModSavedData load(CompoundTag nbt) {
        ModSavedData data = new ModSavedData();
        loadMap(nbt.getList("LastPosList", Tag.TAG_COMPOUND), data.lastPos);
        loadMap(nbt.getList("DeathPosList", Tag.TAG_COMPOUND), data.deathPos);
        if (nbt.contains("GlobalCooldown", Tag.TAG_INT)) {
            data.globalCooldown = nbt.getInt("GlobalCooldown");
        }
        if (nbt.contains("AllowDangerTeleport", Tag.TAG_BYTE)) {
            data.allowDangerTp = nbt.getBoolean("AllowDangerTp");
        }
        return data;
    }

    // 写入 NBT
    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("LastPosList", saveMap(lastPos));
        nbt.put("DeathPosList", saveMap(deathPos));
        nbt.putInt("GlobalCooldown", globalCooldown);
        nbt.putBoolean("AllowDangerTp", allowDangerTp);
        return nbt;
    }

    // 将内存数据转化为 NBT
    private static ListTag saveMap(Map<UUID, BackPosition> map) {
        ListTag list = new ListTag();
        map.forEach((uuid, pos) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("UUID", uuid);
            entry.putString("Dim", pos.dimension().location().toString());
            entry.putDouble("X", pos.x());
            entry.putDouble("Y", pos.y());
            entry.putDouble("Z", pos.z());
            entry.putFloat("Yaw", pos.yaw());
            entry.putFloat("Pitch", pos.pitch());
            list.add(entry);
        });
        return list;
    }

    // 将 NBT 转化为内存对象
    private static void loadMap(ListTag list, Map<UUID, BackPosition> map) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            BackPosition pos = new BackPosition(
                    ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(entry.getString("Dim"))),
                    entry.getDouble("X"), entry.getDouble("Y"), entry.getDouble("Z"),
                    entry.getFloat("Yaw"), entry.getFloat("Pitch")
            );
            map.put(entry.getUUID("UUID"), pos);
        }
    }

    // 获取实例的静态方法
    public static ModSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                ModSavedData::load, ModSavedData::new, "tpaqmz"
        );
    }
}